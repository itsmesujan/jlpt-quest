package com.itsmesujan.jlpt.domain

/** JLPT levels supported by the app. Content ships in waves: N5 first, then N4, N3, N2, N1 (see plan). */
enum class JlptLevel(val label: String) { N5("N5"), N4("N4"), N3("N3"), N2("N2"), N1("N1") }

enum class NodeKind(val title: String) {
    WORD_BATTLE("Word Battle"),
    KANJI_CLASH("Kanji Clash"),
    GRAMMAR_DOJO("Grammar Dojo"),
    ECHO_QUEST("Echo Quest · Listening"),
    STORY_TRAIL("Story Trail · Reading"),
    CHAPTER_BOSS("Chapter Boss · Mini Mock")
}

enum class ExerciseType {
    MULTIPLE_CHOICE,   // pick the correct meaning / reading / answer
    TYPED_RETRIEVAL,   // type the answer (kana or romaji-free production where possible)
    LISTENING,         // hear it (TTS), then answer — text reveal is the offline/accessibility fallback
    READING            // answer about the chapter's reading passage (passage shown on the exercise)
}

data class Exercise(
    val id: String,
    val type: ExerciseType,
    val skill: String,               // vocab | kanji | grammar | listening | reading
    val prompt: String,              // main prompt shown to the learner
    val promptSub: String? = null,   // reading / furigana hint / passage context
    val spokenText: String? = null,  // for LISTENING: what TTS says
    val passage: String? = null,     // for READING: the passage text
    val options: List<String> = emptyList(), // empty for TYPED_RETRIEVAL
    val correctAnswer: String,
    val acceptedAnswers: List<String> = listOf(correctAnswer),
    val explanation: String,
    /** Stable key of the underlying study item (word / kanji / grammar point) — becomes a review card. */
    val itemKey: String
)

data class LessonNode(
    val id: String,
    val kind: NodeKind,
    val title: String,
    val subtitle: String,
    val exercises: List<Exercise>,
    /** Boss nodes are timed mini-mocks; all other nodes are untimed lessons. */
    val isBoss: Boolean = false,
    val timeLimitSeconds: Int? = null,
    val premiumOnly: Boolean = false
)

data class Chapter(
    val id: String,
    val level: JlptLevel,
    val title: String,
    val theme: String,
    val nodes: List<LessonNode>
)
