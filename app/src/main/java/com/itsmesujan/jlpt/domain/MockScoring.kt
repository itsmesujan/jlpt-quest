package com.itsmesujan.jlpt.domain

/**
 * JLPT mock scoring — documented APPROXIMATION.
 * Real JLPT scoring is scaled scoring via Item Response Theory (total /180); the exact
 * conversion is not published and cannot be replicated. We scale raw section performance
 * onto the official section totals and apply the official pass rules:
 *   N5: overall >= 80/180 AND Language Knowledge+Reading >= 38/120 AND Listening >= 19/60
 *   N4: overall >= 90/180 AND the same sectional minimums (38/120, 19/60)
 * Failing a sectional minimum fails the mock even when the total passes — the trap this
 * screen exists to make visible. Always labelled in UI as an estimate, never a guarantee.
 */
data class MockResult(
    val totalScaled: Int,          // /180
    val languageScaled: Int,       // /120 (vocab + kanji + grammar + reading)
    val listeningScaled: Int,      // /60
    val overallPassMark: Int,
    val estimatedPass: Boolean,
    val failedSectionMinimum: String?, // e.g. "Listening" when the sectional trap fires
    val weakestSkill: String
)

object MockScoring {
    fun score(level: JlptLevel, results: List<Pair<Exercise, Boolean>>): MockResult {
        val language = results.filter { it.first.skill != "listening" }
        val listening = results.filter { it.first.skill == "listening" }
        fun scaled(part: List<Pair<Exercise, Boolean>>, outOf: Int): Int {
            if (part.isEmpty()) return 0
            val ratio = part.count { it.second }.toDouble() / part.size
            return Math.round(ratio * outOf).toInt()
        }
        val langScaled = scaled(language, 120)
        val listScaled = scaled(listening, 60)
        val total = langScaled + listScaled
        val passMark = when (level) { JlptLevel.N5 -> 80; JlptLevel.N4 -> 90; JlptLevel.N3 -> 95; JlptLevel.N2 -> 90; JlptLevel.N1 -> 100 }
        val failedMin = when {
            langScaled < 38 -> "Language Knowledge & Reading (below the 38/120 sectional minimum)"
            listScaled < 19 -> "Listening (below the 19/60 sectional minimum)"
            else -> null
        }
        val bySkill = results.groupBy { it.first.skill }
            .mapValues { (_, v) -> if (v.isEmpty()) 1.0 else v.count { it.second }.toDouble() / v.size }
        val weakest = bySkill.minByOrNull { it.value }?.key ?: "vocab"
        return MockResult(total, langScaled, listScaled, passMark, total >= passMark && failedMin == null, failedMin, weakest)
    }
}
