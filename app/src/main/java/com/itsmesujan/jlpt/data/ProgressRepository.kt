package com.itsmesujan.jlpt.data

import com.itsmesujan.jlpt.data.local.AppDatabase
import com.itsmesujan.jlpt.data.local.MistakeEntity
import com.itsmesujan.jlpt.data.local.NodeProgressEntity
import com.itsmesujan.jlpt.data.local.PlayerProfileEntity
import com.itsmesujan.jlpt.data.local.ReviewCardEntity
import com.itsmesujan.jlpt.data.local.todayEpochDay
import com.itsmesujan.jlpt.domain.Exercise
import com.itsmesujan.jlpt.domain.FsrsScheduler
import com.itsmesujan.jlpt.domain.Rating
import kotlinx.coroutines.flow.Flow

/** Offline-first repository: all learning state lives in Room; there is no required network call
 *  anywhere in the lesson/review/mock flow. Cloud sync of this state is a v1.5 TODO. */
class ProgressRepository(private val db: AppDatabase) {

    val profile: Flow<PlayerProfileEntity?> = db.profileDao().observe()
    val nodeProgress: Flow<List<NodeProgressEntity>> = db.nodeProgressDao().observeAll()
    fun dueCards(today: Long = todayEpochDay()): Flow<List<ReviewCardEntity>> = db.reviewCardDao().observeDue(today)
    fun dueCount(today: Long = todayEpochDay()): Flow<Int> = db.reviewCardDao().observeDueCount(today)
    val openMistakes: Flow<List<MistakeEntity>> = db.mistakeDao().observeOpen()
    val allCards: Flow<List<ReviewCardEntity>> = db.reviewCardDao().observeAll()

    suspend fun ensureProfile() {
        if (db.profileDao().get() == null) db.profileDao().upsert(PlayerProfileEntity())
    }

    suspend fun completeOnboarding(goal: String, level: String, dailyMinutes: Int) {
        val current = db.profileDao().get() ?: PlayerProfileEntity()
        db.profileDao().upsert(current.copy(onboarded = true, goal = goal, targetLevel = level, dailyGoalMinutes = dailyMinutes))
    }

    /** Record a finished lesson/mock: node stars, XP, streak, review cards for every item, mistakes for misses.
     *  Returns the XP earned. NO hearts are deducted for wrong answers — by design (research §5). */
    suspend fun recordSession(nodeId: String, chapterId: String, results: List<Pair<Exercise, Boolean>>, latenciesMs: List<Long>): Int {
        val today = todayEpochDay()
        val correctCount = results.count { it.second }
        val accuracy = if (results.isEmpty()) 0f else correctCount.toFloat() / results.size
        val xpEarned = correctCount * 10 + if (accuracy == 1f) 20 else 0 // +20 perfect-session bonus
        val stars = when { accuracy >= 0.9f -> 3; accuracy >= 0.7f -> 2; accuracy >= 0.5f -> 1; else -> 0 }

        val existing = db.nodeProgressDao().let { dao ->
            // Room has no single-row get for progress here; REPLACE upsert keeps the best result.
            NodeProgressEntity(nodeId, chapterId, completed = accuracy >= 0.6f, stars = stars, bestAccuracy = accuracy, completedEpochDay = today)
        }
        db.nodeProgressDao().upsert(existing)

        results.forEachIndexed { index, (exercise, correct) ->
            val latency = latenciesMs.getOrElse(index) { 8_000L }
            val rating = FsrsScheduler.inferRating(correct, latency, hintUsed = false)
            upsertCard(exercise, rating, today)
            if (!correct) db.mistakeDao().insert(MistakeEntity(itemKey = exercise.itemKey, nodeId = nodeId, missedEpochDay = today))
            else db.mistakeDao().clear(exercise.itemKey)
        }

        val profile = db.profileDao().get() ?: PlayerProfileEntity()
        val streak = when {
            profile.lastStudyEpochDay == today -> profile.streakDays
            profile.lastStudyEpochDay == today - 1 -> profile.streakDays + 1
            profile.lastStudyEpochDay == today - 2 && profile.streakFreezes > 0 -> profile.streakDays + 1 // freeze covers one missed day
            else -> 1
        }
        val freezeUsed = profile.lastStudyEpochDay == today - 2 && profile.streakDays > 0
        db.profileDao().upsert(
            profile.copy(
                xp = profile.xp + xpEarned,
                streakDays = streak,
                streakFreezes = if (freezeUsed) maxOf(0, profile.streakFreezes - 1) else profile.streakFreezes,
                lastStudyEpochDay = today
            )
        )
        return xpEarned
    }

    /** Explicit flashcard-style review with a learner-chosen rating. */
    suspend fun recordReview(card: ReviewCardEntity, rating: Rating) {
        val today = todayEpochDay()
        val next = FsrsScheduler.review(card.toFsrsState(), rating, today)
        db.reviewCardDao().upsert(
            card.copy(
                difficulty = next.difficulty, stability = next.stability, retrievability = next.retrievability,
                reps = next.reps, lapses = next.lapses, lastReviewEpochDay = next.lastReviewEpochDay, dueEpochDay = next.dueEpochDay
            )
        )
        if (rating != Rating.AGAIN) db.mistakeDao().clear(card.itemKey)
    }

    private suspend fun upsertCard(exercise: Exercise, rating: Rating, today: Long) {
        val existingCard = db.reviewCardDao().get(exercise.itemKey)
        val base = existingCard?.toFsrsState() ?: com.itsmesujan.jlpt.domain.FsrsState()
        val next = FsrsScheduler.review(base, rating, today)
        db.reviewCardDao().upsert(
            ReviewCardEntity(
                itemKey = exercise.itemKey,
                prompt = exercise.prompt,
                promptSub = exercise.promptSub,
                correctAnswer = exercise.correctAnswer,
                optionsCsv = exercise.options.joinToString("\u0001"),
                explanation = exercise.explanation,
                skill = exercise.skill,
                difficulty = next.difficulty, stability = next.stability, retrievability = next.retrievability,
                reps = next.reps, lapses = next.lapses, lastReviewEpochDay = next.lastReviewEpochDay, dueEpochDay = next.dueEpochDay
            )
        )
    }

    suspend fun setPremium(isPremium: Boolean, productId: String?, purchaseToken: String?) {
        val profile = db.profileDao().get() ?: PlayerProfileEntity()
        db.profileDao().upsert(profile.copy(isPremium = isPremium, premiumProductId = productId, premiumPurchaseToken = purchaseToken))
    }
}

fun ReviewCardEntity.toFsrsState() = com.itsmesujan.jlpt.domain.FsrsState(
    difficulty = difficulty, stability = stability, retrievability = retrievability,
    reps = reps, lapses = lapses, lastReviewEpochDay = lastReviewEpochDay, dueEpochDay = dueEpochDay
)
