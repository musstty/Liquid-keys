package com.liquidkeys.keyboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import java.util.Locale

class VoiceCaptureActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to type with LiquidKeys")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try { startActivityForResult(intent, 701) } catch (_: Exception) {
            Toast.makeText(this, "No compatible speech-recognition app is available.", Toast.LENGTH_LONG).show(); finish()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 701 && resultCode == RESULT_OK) {
            val recognized = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!recognized.isNullOrBlank()) sendBroadcast(Intent("com.liquidkeys.keyboard.VOICE_TEXT").setPackage(packageName).putExtra("recognized_text", "$recognized "))
        }
        finish()
    }
}
