package com.itsmesujan.jlpt

import android.app.Application
import com.itsmesujan.jlpt.data.ProgressRepository
import com.itsmesujan.jlpt.data.local.AppDatabase

/** Minimal manual DI (no framework) — a solo project stays debuggable.
 *  TODO(v1.5): Firebase Auth/Firestore sync, Crashlytics, Analytics event taxonomy
 *  (onboarding_complete, placement_result, lesson_start/complete, review_session,
 *   mock_start/complete, paywall_view, trial_start, purchase, streak_day). */
class JlptQuestApplication : Application() {
    lateinit var repository: ProgressRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = ProgressRepository(AppDatabase.get(this))
    }
}
