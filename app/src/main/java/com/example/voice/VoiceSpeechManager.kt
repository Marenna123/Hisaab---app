package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class SpeechState {
    IDLE,
    LISTENING,
    PROCESSING,
    ERROR
}

class VoiceSpeechManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _speechState = MutableStateFlow(SpeechState.IDLE)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    var selectedLanguageCode: String = "auto" // "auto", "te-IN", "en-IN"

    var onFinalSpeechResult: ((String) -> Unit)? = null

    init {
        initTts()
    }

    private fun initTts() {
        try {
            textToSpeech = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsReady = true
                    textToSpeech?.language = Locale.ENGLISH
                }
            }
        } catch (e: Exception) {
            Log.e("VoiceSpeechManager", "TTS init error: ${e.message}")
        }
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _errorMessage.value = "Speech recognition is not available on this device"
            _speechState.value = SpeechState.ERROR
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

                when (selectedLanguageCode) {
                    "te-IN" -> {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "te-IN")
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "te-IN")
                    }
                    "en-IN" -> {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-IN")
                    }
                    else -> {
                        // Auto: default with Indian English / System
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
                    }
                }
            }

            _spokenText.value = ""
            _errorMessage.value = null
            _speechState.value = SpeechState.LISTENING
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Failed to start listening"
            _speechState.value = SpeechState.ERROR
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _speechState.value = SpeechState.PROCESSING
        } catch (e: Exception) {
            Log.e("VoiceSpeechManager", "Stop error: ${e.message}")
        }
    }

    fun cancelListening() {
        try {
            speechRecognizer?.cancel()
            _speechState.value = SpeechState.IDLE
            _rmsLevel.value = 0f
        } catch (e: Exception) {
            Log.e("VoiceSpeechManager", "Cancel error: ${e.message}")
        }
    }

    /**
     * Provide a simulated spoken phrase (useful for emulator testing & quick phrase suggestions)
     */
    fun simulateSpeechInput(phrase: String) {
        _spokenText.value = phrase
        _speechState.value = SpeechState.PROCESSING
        onFinalSpeechResult?.invoke(phrase)
        _speechState.value = SpeechState.IDLE
    }

    fun speak(text: String, isTelugu: Boolean = false) {
        if (!isTtsReady || textToSpeech == null) return
        try {
            if (isTelugu) {
                val teluguLocale = Locale("te", "IN")
                val result = textToSpeech?.setLanguage(teluguLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech?.language = Locale.ENGLISH
                }
            } else {
                textToSpeech?.language = Locale.ENGLISH
            }
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "HISAAB_TTS")
        } catch (e: Exception) {
            Log.e("VoiceSpeechManager", "Speak error: ${e.message}")
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (e: Exception) {
            Log.e("VoiceSpeechManager", "Destroy error: ${e.message}")
        }
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _speechState.value = SpeechState.LISTENING
        }

        override fun onBeginningOfSpeech() {
            _speechState.value = SpeechState.LISTENING
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Normalize roughly between 0.0 and 1.0
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1.0f)
            _rmsLevel.value = normalized
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _speechState.value = SpeechState.PROCESSING
            _rmsLevel.value = 0f
        }

        override fun onError(error: Int) {
            _rmsLevel.value = 0f
            val message = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please speak clearly into the microphone."
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please check microphone."
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
                SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network issue for voice recognition."
                else -> "Speech recognition error ($error). You can also tap example phrases below."
            }
            _errorMessage.value = message
            _speechState.value = SpeechState.ERROR
        }

        override fun onResults(results: Bundle?) {
            _speechState.value = SpeechState.PROCESSING
            _rmsLevel.value = 0f
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val bestMatch = matches?.firstOrNull() ?: ""
            if (bestMatch.isNotBlank()) {
                _spokenText.value = bestMatch
                onFinalSpeechResult?.invoke(bestMatch)
            }
            _speechState.value = SpeechState.IDLE
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.firstOrNull()?.let {
                _spokenText.value = it
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
