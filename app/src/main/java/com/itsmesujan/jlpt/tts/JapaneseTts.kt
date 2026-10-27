package com.itsmesujan.jlpt.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * On-device Japanese TTS for listening exercises and teach-card audio.
 * Fallback only (blueprint §7.2): launch content should migrate to pre-generated neural audio
 * packs; device TTS quality varies by OEM. Every listening exercise also offers a text reveal,
 * so a missing Japanese voice never blocks an answer (accessibility + offline guarantee).
 */
class JapaneseTts(context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.JAPANESE)
                ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
                tts?.setSpeechRate(0.9f) // slightly slow for learners; mock mode uses 1.0f
            }
        }
    }

    fun speak(text: String, slow: Boolean = false) {
        if (!ready) return
        tts?.setSpeechRate(if (slow) 0.75f else 0.9f)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jlptquest-utterance")
    }

    fun shutdown() {
        tts?.stop(); tts?.shutdown(); tts = null
    }
}
