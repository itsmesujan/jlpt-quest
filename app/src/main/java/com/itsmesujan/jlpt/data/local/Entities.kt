package com.itsmesujan.jlpt.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Single-row player profile (id always 1). Entitlement fields are written ONLY by the
 *  billing layer after purchase verification — never trust them from UI state. */
@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val onboarded: Boolean = false,
    val goal: String = "JLPT exam",
    val dailyGoalMinutes: Int = 10,
    val targetLevel: String = "N5",
    val xp: Int = 0,
    val streakDays: Int = 0,
    val lastStudyEpochDay: Long = 0,
    val streakFreezes: Int = 2,          // 2 free freezes/month (blueprint) — replenished monthly in v1.5
    val isPremium: Boolean = false,
    val premiumProductId: String? = null,
    val premiumPurchaseToken: String? = null
)

@Entity(tableName = "node_progress")
data class NodeProgressEntity(
    @PrimaryKey val nodeId: String,
    val chapterId: String,
    val completed: Boolean = false,
    val stars: Int = 0,                  // 0..3 by accuracy
    val bestAccuracy: Float = 0f,
    val completedEpochDay: Long = 0
)

/** One FSRS review card per study item. Carries a self-contained snapshot of the question so a
 *  review session never depends on finding the original exercise again. */
@Entity(tableName = "review_cards")
data class ReviewCardEntity(
    @PrimaryKey val itemKey: String,
    val prompt: String,
    val promptSub: String?,
    val correctAnswer: String,
    val optionsCsv: String,              // options joined by \u0001; empty = typed recall
    val explanation: String,
    val skill: String,
    // FSRS-style scheduling fields (see domain/FsrsScheduler.kt)
    val difficulty: Double = 5.0,
    val stability: Double = 0.0,
    val retrievability: Double = 1.0,
    val reps: Int = 0,
    val lapses: Int = 0,
    val lastReviewEpochDay: Long = 0,
    val dueEpochDay: Long = 0
)

/** Weak-spot queue: every missed item lands here until cleared in review. */
@Entity(tableName = "mistakes")
data class MistakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemKey: String,
    val nodeId: String,
    val missedEpochDay: Long,
    val cleared: Boolean = false
)
