package com.itsmesujan.jlpt.domain

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

/** Learner rating for a review. In lessons this is *inferred* from correctness + latency
 *  (see [FsrsScheduler.inferRating]); explicit 4-button rating is only shown in flashcard review. */
enum class Rating { AGAIN, HARD, GOOD, EASY }

/**
 * FSRS-style scheduler (simplified, documented approximation of FSRS-4.5/5 maths).
 *
 * Why FSRS and not SM-2: FSRS models Difficulty / Stability / Retrievability and schedules a
 * review when predicted recall falls to the target retention (default 90%). Published benchmarks
 * show the same retention with ~20-30% fewer reviews than SM-2 and no "ease hell".
 * This implementation keeps the D/S/R state per card and uses the standard power-law forgetting
 * curve R(t,S) = (1 + 19/81 * t/S)^-0.5. Parameters are fixed defaults; per-user optimisation
 * after ~1,000 reviews is a TODO (see README "Roadmap").
 */
data class FsrsState(
    val difficulty: Double = 5.0,   // 1..10
    val stability: Double = 0.0,    // days; 0 = new card
    val retrievability: Double = 1.0,
    val reps: Int = 0,
    val lapses: Int = 0,
    val lastReviewEpochDay: Long = 0,
    val dueEpochDay: Long = 0
)

object FsrsScheduler {
    const val TARGET_RETENTION = 0.90
    private const val DECAY = -0.5
    private val FACTOR = 19.0 / 81.0

    /** Days from now until predicted recall drops to [TARGET_RETENTION], given stability [s]. */
    fun intervalDays(s: Double): Double {
        if (s <= 0) return 0.0
        val r = TARGET_RETENTION
        return (s / FACTOR) * (Math.pow(r, 1.0 / DECAY) - 1.0)
    }

    fun retrievability(elapsedDays: Double, s: Double): Double {
        if (s <= 0) return 0.0
        return Math.pow(1.0 + FACTOR * elapsedDays / s, DECAY)
    }

    /** Infer a rating from an exercise result so lessons never end with a self-rating tap. */
    fun inferRating(correct: Boolean, latencyMs: Long, hintUsed: Boolean): Rating = when {
        !correct -> Rating.AGAIN
        hintUsed -> Rating.HARD
        latencyMs in 0..4_000 -> Rating.EASY
        latencyMs in 4_001..12_000 -> Rating.GOOD
        else -> Rating.HARD
    }

    /** Apply a review and return the new scheduling state. [todayEpochDay] = days since epoch (UTC). */
    fun review(state: FsrsState, rating: Rating, todayEpochDay: Long): FsrsState {
        val elapsed = if (state.reps == 0) 0.0 else max(0.0, (todayEpochDay - state.lastReviewEpochDay).toDouble())
        val r = if (state.reps == 0) 1.0 else retrievability(elapsed, state.stability)

        val newDifficulty = nextDifficulty(state.difficulty, rating)
        val newStability = if (state.reps == 0) {
            // Initial stability by grade (days): Again .4, Hard 1.2, Good 3.0, Easy 7.0 (FSRS-like seeds)
            when (rating) { Rating.AGAIN -> 0.4; Rating.HARD -> 1.2; Rating.GOOD -> 3.0; Rating.EASY -> 7.0 }
        } else if (rating == Rating.AGAIN) {
            // Post-lapse stability: shrink, floored, never below 0.3 days
            max(0.3, state.stability * 0.35 * exp(-0.15 * (newDifficulty - 5.0)))
        } else {
            val hardPenalty = if (rating == Rating.HARD) 0.85 else 1.0
            val easyBonus = if (rating == Rating.EASY) 1.35 else 1.0
            val recallBonus = 1.0 + (1.0 - r) * 0.6 // forgotten-ish success grows stability more
            state.stability * hardPenalty * easyBonus * recallBonus * (1.0 + (10.0 - newDifficulty) / 40.0)
        }
        val interval = intervalDays(newStability)
        // Learning steps: Again comes back today/tomorrow; everything else at least tomorrow.
        val dueInDays = when (rating) {
            Rating.AGAIN -> if (interval < 1.0) 0L else interval.toLong()
            else -> max(1L, Math.round(interval))
        }
        return state.copy(
            difficulty = newDifficulty,
            stability = newStability,
            retrievability = r,
            reps = state.reps + 1,
            lapses = state.lapses + if (rating == Rating.AGAIN) 1 else 0,
            lastReviewEpochDay = todayEpochDay,
            dueEpochDay = todayEpochDay + dueInDays
        )
    }

    private fun nextDifficulty(d: Double, rating: Rating): Double {
        val delta = when (rating) { Rating.AGAIN -> 1.2; Rating.HARD -> 0.6; Rating.GOOD -> 0.0; Rating.EASY -> -0.8 }
        // Mean-reversion toward 5 keeps difficulty stable over long histories (FSRS behaviour).
        val reverted = d + delta - 0.08 * (d - 5.0)
        return min(10.0, max(1.0, reverted))
    }

    /** ln() kept for future parameter optimisation; referenced to avoid dead-code warnings in tooling. */
    @Suppress("unused")
    private fun logCurve(x: Double) = ln(max(1.0, x))
}
