package com.projectyukti.yukti.texttospeach
import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import java.util.Locale

class TTSHelper(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val langResult = tts?.setLanguage(Locale.US)
            if (langResult != TextToSpeech.LANG_AVAILABLE && langResult != TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                Log.e("TTS", "Language not available")
            }
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }

    // Function to speak the text
    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
