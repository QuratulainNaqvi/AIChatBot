package com.denis.dnschat

import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.denis.dnschat.adapter.ChatAdapter
import com.denis.dnschat.databinding.ActivityMainBinding
import com.denis.dnschat.interfaces.AIAPIService
import com.denis.dnschat.interfaces.ChatVoiceClick
import com.denis.dnschat.model.ChatMessage
import com.denis.dnschat.model.RequestBody
import com.denis.dnschat.network.NetworkUtils
import com.denis.dnschat.util.KeyboardUtils
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var aiService: AIAPIService
    private lateinit var textToSpeech: TextToSpeech
    private var isTTSInitialized = false
    private var isSpeaking = false
    private var currentMessage = ""
    private var chatAdapter: ChatAdapter? = null
    private var chatTypeClicked: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        chatAdapter = ChatAdapter(mutableListOf(), object : ChatVoiceClick {
            override fun onChatClicked(
                view: View?,
                chatMessage: ChatMessage,
                position: Int,
                chatType: String
            ) {
                detectAndSpeak(chatMessage.message)
                chatTypeClicked = chatType
            }

            override fun onStopSpeech(
                view: View?,
                chatMessage: ChatMessage,
                position: Int,
                chatType: String
            ) {
                stopSpeech()
            }

        })
        binding.rvChat.adapter = chatAdapter

        aiService = NetworkUtils.createApiService()
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val availableLanguages = textToSpeech.availableLanguages
                isTTSInitialized = true
            } else {
                Toast.makeText(this, "TextToSpeech initialization failed.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                if (utteranceId == "unique_id") {
                    isSpeaking = true
                }
            }

            override fun onDone(utteranceId: String?) {
                runOnUiThread {
                    if (utteranceId == "unique_id") {
                        isSpeaking = false
                        if (chatTypeClicked.equals("userChat")) {
                            chatAdapter?.stopUserSpeech()
                        } else {
                            chatAdapter?.stopAISpeech()
                        }

                    }
                }
            }

            override fun onError(utteranceId: String?) {
                if (utteranceId == "unique_id") {
                    isSpeaking = false
                    if (chatTypeClicked.equals("userChat")) {
                        chatAdapter?.stopUserSpeech()
                    } else {
                        chatAdapter?.stopAISpeech()
                    }
                }
            }
        })
        chatAdapter?.addMessage(ChatMessage("Hi, How can I help you?", "AI"))
        binding.rvChat.scrollToPosition(chatAdapter?.itemCount!! - 1)

        binding.sendButton.setOnClickListener {
            val userMessage = binding.inputEditText.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                binding.sendButton.visibility = View.GONE
                binding.pbLoading.visibility = View.VISIBLE
                chatAdapter?.addMessage(ChatMessage(userMessage, "USER"))
                binding.rvChat.scrollToPosition(chatAdapter?.itemCount!! - 1)
                sendMessage(userMessage)
            }
        }

    }


    @SuppressLint("SetTextI18n", "InflateParams")
    private fun sendMessage(userMessage: String) {
        binding.inputEditText.text?.clear()

        chatAdapter?.addMessage(ChatMessage("", "Skeleton"))
        binding.rvChat.scrollToPosition(chatAdapter?.itemCount!! - 1)

        var aiResponse = "Hello, I'm your chatbot!"

        if (userMessage == "hi" || userMessage == "Hi") {
            chatAdapter?.removeMessage(chatAdapter?.itemCount!! - 1)
            chatAdapter?.addMessage(ChatMessage("Hi! How can I help you?", "AI"))
            binding.rvChat.scrollToPosition(chatAdapter?.itemCount!! - 1)
        } else {
            val apiKey = "YOUR API KEY"
            val requestBody =
                RequestBody(
                    "text-davinci-003",
                    userMessage,
                    1000,
                    0f,
                    1f,
                    0,
                    0
                )

            lifecycleScope.launch(Dispatchers.Main) {
                try {
                    val response = aiService.getPrompt("Bearer $apiKey", requestBody)
                    aiResponse = response.choices[0].text.trim()
                    chatAdapter?.removeMessage(chatAdapter?.itemCount!! - 1)
                    chatAdapter?.addMessage(ChatMessage(aiResponse, "AI"))
                    binding.rvChat.scrollToPosition(chatAdapter?.itemCount!! - 1)
                } catch (e: Exception) {
                    chatAdapter?.removeMessage(chatAdapter?.itemCount!! - 1)
                    chatAdapter?.addMessage(ChatMessage("Oops. Please try again later...", "AI"))
                    binding.rvChat.scrollToPosition(chatAdapter?.itemCount!! - 1)
                    currentFocus?.let { KeyboardUtils.hideKeyboard(it) }
                }
            }
        }
        binding.pbLoading.visibility = View.GONE
        binding.sendButton.visibility = View.VISIBLE

    }

    private fun showToast(message: String) {
        if (isSpeaking) {
            textToSpeech.stop()
        }
        if (isTTSInitialized) {
            currentMessage = message
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "unique_id")
            textToSpeech.speak(currentMessage, TextToSpeech.QUEUE_FLUSH, params, "unique_id")
            isSpeaking = true
        }
    }

    private fun detectAndSpeak(text: String) {
        val languageIdentifier: LanguageIdentifier = LanguageIdentification.getClient()

        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                val locale = Locale(languageCode)
                textToSpeech.language = locale
                showToast(text)
            }
            .addOnFailureListener { _ ->
                Toast.makeText(this, "Language detection failed.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun stopSpeech() {
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
    }

    override fun onDestroy() {
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
        textToSpeech.shutdown()
        super.onDestroy()
    }
}
