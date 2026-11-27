package com.example.voicecompanion

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.util.Locale

private data class ChatMessage(val text: String, val fromUser: Boolean)

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
            }
        }
        setContent {
            MaterialTheme {
                CompanionScreen(
                    speak = { text ->
                        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "companion_reply")
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
private fun CompanionScreen(speak: (String) -> Unit) {
    val context = LocalContext.current
    val messages = remember {
        mutableStateListOf(
            ChatMessage("Hello. Voice interaction is ready to test. My AI brain is not connected yet.", false)
        )
    }
    var input by remember { mutableStateOf("") }
    var listening by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Ready") }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    fun addUserMessage(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        messages.add(ChatMessage(clean, true))
        input = ""
        val reply = "I heard you say: $clean. The conversation AI will be connected in a later step."
        messages.add(ChatMessage(reply, false))
        speak(reply)
        status = "Ready"
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            status = "Starting microphone…"
            speechRecognizer?.startListening(
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                }
            )
        } else {
            status = "Microphone permission was not granted"
        }
    }

    DisposableEffect(Unit) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).also { recognizer ->
                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) { listening = true; status = "Listening…" }
                    override fun onBeginningOfSpeech() { status = "Listening…" }
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() { listening = false; status = "Processing speech…" }
                    override fun onError(error: Int) {
                        listening = false
                        status = "Speech recognition error. Try again or type a message."
                    }
                    override fun onResults(results: Bundle?) {
                        listening = false
                        val result = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                        if (result != null) addUserMessage(result) else status = "No speech detected"
                    }
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } else {
            status = "Speech recognition is unavailable on this device"
        }
        onDispose {
            speechRecognizer?.destroy()
            speechRecognizer = null
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0B1020)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Text("VOICE COMPANION", color = Color(0xFF91A7FF), fontWeight = FontWeight.Bold)
                Text("A voice-first AI project", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Text(status, color = Color(0xFFABB6D3), style = MaterialTheme.typography.bodySmall)
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { message ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .background(
                                    if (message.fromUser) Color(0xFF293A70) else Color(0xFF182238),
                                    RoundedCornerShape(18.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Text(message.text, color = Color.White)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Type a message…", color = Color(0xFF8792AD)) },
                singleLine = true
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { addUserMessage(input) },
                    modifier = Modifier.weight(1f),
                    enabled = input.isNotBlank()
                ) { Text("Send") }

                Button(
                    onClick = {
                        val recognizer = speechRecognizer
                        if (recognizer == null) {
                            status = "Speech recognition is unavailable"
                        } else if (ContextCompat.checkSelfPermission(
                                context, Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            status = "Starting microphone…"
                            recognizer.startListening(
                                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                                }
                            )
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (listening) "Listening…" else "Talk")
                }
            }
            TextButton(
                onClick = {
                    val lastReply = messages.lastOrNull { !it.fromUser }?.text
                    if (lastReply != null) speak(lastReply)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text("Replay last voice response", color = Color(0xFF91A7FF)) }
        }
    }
}
