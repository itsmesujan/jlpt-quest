package com.itsmesujan.jlpt

import com.itsmesujan.jlpt.domain.Exercise
import com.itsmesujan.jlpt.domain.ExerciseType
import com.itsmesujan.jlpt.domain.FsrsScheduler
import com.itsmesujan.jlpt.domain.FsrsState
import com.itsmesujan.jlpt.domain.JlptLevel
import com.itsmesujan.jlpt.domain.MockScoring
import com.itsmesujan.jlpt.domain.Rating
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FsrsAndScoringTest {

    @Test
    fun newCard_good_getsMultiDayInterval() {
        val next = FsrsScheduler.review(FsrsState(), Rating.GOOD, todayEpochDay = 20_000)
        assertEquals(1, next.reps)
        assertTrue(next.dueEpochDay > 20_000)
        assertTrue(next.stability > 0)
    }

    @Test
    fun lapse_shrinksStabilityAndCounts() {
        val grown = FsrsScheduler.review(FsrsState(), Rating.GOOD, 20_000)
        val lapsed = FsrsScheduler.review(grown, Rating.AGAIN, grown.dueEpochDay)
        assertEquals(1, lapsed.lapses)
        assertTrue(lapsed.stability < grown.stability * 1.5)
    }

    @Test
    fun retrievability_decaysWithTime() {
        val soon = FsrsScheduler.retrievability(1.0, 10.0)
        val later = FsrsScheduler.retrievability(30.0, 10.0)
        assertTrue(soon > later)
    }

    private fun ex(skill: String) = Exercise(
        id = "t-$skill", type = ExerciseType.MULTIPLE_CHOICE, skill = skill,
        prompt = "p", options = listOf("a"), correctAnswer = "a", explanation = "e", itemKey = "k-$skill"
    )

    @Test
    fun mock_sectionalMinimumTrap_failsDespitePassingTotal() {
        // Language perfect, listening all wrong: total = 120 which is >= 80, but listening 0 < 19.
        val results = (1..6).map { ex("vocab") to true } + (1..4).map { ex("listening") to false }
        val result = MockScoring.score(JlptLevel.N5, results)
        assertTrue(result.totalScaled >= 80)
        assertFalse(result.estimatedPass)
        assertTrue(result.failedSectionMinimum!!.contains("Listening"))
    }

    @Test
    fun mock_allCorrect_passes() {
        val results = (1..6).map { ex("grammar") to true } + (1..4).map { ex("listening") to true }
        assertTrue(MockScoring.score(JlptLevel.N5, results).estimatedPass)
    }
}
