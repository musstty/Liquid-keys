package com.liquidkeys.keyboard

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.media.AudioManager
import android.net.Uri

class LiquidKeysService : InputMethodService() {
    private var caps = false
    private var symbols = false
    private var root: LinearLayout? = null
    private val voiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val text = intent?.getStringExtra("recognized_text") ?: return
            currentInputConnection?.commitText(text, 1)
        }
    }
    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter("com.liquidkeys.keyboard.VOICE_TEXT")
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(voiceReceiver, filter, RECEIVER_NOT_EXPORTED) else @Suppress("DEPRECATION") registerReceiver(voiceReceiver, filter)
    }
    override fun onDestroy() { try { unregisterReceiver(voiceReceiver) } catch (_: Exception) {}; super.onDestroy() }

    override fun onCreateInputView(): View {
        val outer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(5, 7, 5, 8) }
        root = outer
        applyKeyboardBackground(outer)
        val toolbar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        toolbar.addView(tool("✨", "LiquidKeys") { Toast.makeText(this, "LiquidKeys · Glass typing", Toast.LENGTH_SHORT).show() }, weight(1f))
        toolbar.addView(tool("🎙", "Voice typing") { startActivity(Intent(this, VoiceCaptureActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }, weight(1f))
        toolbar.addView(tool("📋", "Paste clipboard") { pasteClipboard() }, weight(1f))
        toolbar.addView(tool("😊", "Emoji") { currentInputConnection?.commitText("😊", 1) }, weight(1f))
        outer.addView(toolbar, LinearLayout.LayoutParams(-1, 43))
        if (Prefs.numberRow(this)) addRow(outer, listOf("1","2","3","4","5","6","7","8","9","0"), 0)
        val rows = if (!symbols) listOf("qwertyuiop", "asdfghjkl", "⇧zxcvbnm⌫") else listOf("1234567890", "@#\$%&-*+(", "=/:;!?'\"⌫")
        rows.forEachIndexed { index, row ->
            val items = row.map { it.toString() }
            val wrap = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
            if (index == 1) { val spacer = View(this); wrap.addView(spacer, LinearLayout.LayoutParams(10, 1)) }
            items.forEach { label -> wrap.addView(key(label), LinearLayout.LayoutParams(0, rowHeight(), 1f).apply { setMargins(2, 3, 2, 3) }) }
            if (index == 1) wrap.addView(View(this), LinearLayout.LayoutParams(10, 1))
            outer.addView(wrap)
        }
        val bottom = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        bottom.addView(key("?123", wide = true) { symbols = !symbols; recreateKeyboard() }, LinearLayout.LayoutParams(0, rowHeight(), 1.25f).apply { setMargins(2,3,2,3) })
        bottom.addView(key(",", wide = false) { currentInputConnection?.commitText(",", 1) }, LinearLayout.LayoutParams(0, rowHeight(), .7f).apply { setMargins(2,3,2,3) })
        bottom.addView(key("English (US)", wide = true) { currentInputConnection?.commitText(" ", 1) }, LinearLayout.LayoutParams(0, rowHeight(), 4.2f).apply { setMargins(2,3,2,3) })
        bottom.addView(key(".", wide = false) { currentInputConnection?.commitText(".", 1) }, LinearLayout.LayoutParams(0, rowHeight(), .7f).apply { setMargins(2,3,2,3) })
        bottom.addView(key("↵", wide = true) { enterKey() }, LinearLayout.LayoutParams(0, rowHeight(), 1.15f).apply { setMargins(2,3,2,3) })
        outer.addView(bottom)
        return outer
    }
    private fun recreateKeyboard() { setInputView(onCreateInputView()) }
    private fun addRow(parent: LinearLayout, labels: List<String>, unused: Int) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        labels.forEach { row.addView(key(it), LinearLayout.LayoutParams(0, rowHeight() - 2, 1f).apply { setMargins(2,2,2,2) }) }
        parent.addView(row)
    }
    private fun key(label: String, wide: Boolean = false, action: (() -> Unit)? = null): TextView {
        return TextView(this).apply {
            text = if (label.length == 1 && label[0].isLetter() && caps) label.uppercase() else label
            gravity = Gravity.CENTER; textSize = Prefs.fontSize(this@LiquidKeysService).toFloat(); setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            background = glassDrawable(label in listOf("⇧", "⌫", "?123", "↵"))
            isClickable = true; isFocusable = true
            setOnClickListener {
                if (Prefs.haptics(this@LiquidKeysService)) vibrate()
                if (Prefs.sounds(this@LiquidKeysService)) try { (getSystemService(AUDIO_SERVICE) as AudioManager).playSoundEffect(AudioManager.FX_KEY_CLICK) } catch (_: Exception) {}
                if (action != null) action() else when (label) {
                    "⌫" -> currentInputConnection?.deleteSurroundingText(1, 0)
                    "⇧" -> { caps = !caps; recreateKeyboard() }
                    else -> currentInputConnection?.commitText(if (label.length == 1 && label[0].isLetter() && caps) label.uppercase() else label, 1)
                }
            }
            contentDescription = label
        }
    }
    private fun tool(label: String, description: String, action: () -> Unit) = TextView(this).apply {
        text = label; gravity = Gravity.CENTER; textSize = 20f; setTextColor(Color.WHITE); contentDescription = description
        background = glassDrawable(false); setOnClickListener { action() }
    }
    private fun weight(w: Float) = LinearLayout.LayoutParams(0, -1, w).apply { setMargins(2, 1, 2, 5) }
    private fun rowHeight(): Int = (Prefs.height(this) / if (Prefs.numberRow(this)) 5.4 else 4.4).toInt().coerceIn(35, 56)
    private fun glassDrawable(accent: Boolean): GradientDrawable {
        val theme = Prefs.theme(this)
        val color = when (theme) {
            "Midnight" -> 0xFF111827.toInt()
            "Neon" -> if (accent) 0xAA6939C6.toInt() else 0x6655C8FF
            "Pure Glass" -> 0x44FFFFFF
            else -> if (accent) 0xAA326B9A.toInt() else 0x6679BDE8
        }
        val alpha = (Prefs.transparency(this) * 2.55).toInt().coerceIn(30, 255)
        return GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf((color and 0x00FFFFFF) or (alpha shl 24), (color and 0x00FFFFFF) or ((alpha * .62).toInt() shl 24))).apply {
            cornerRadius = if (theme == "Neon") 15f else 12f
            setStroke(1, if (theme == "Neon") 0xCC9DEBFF.toInt() else 0x66FFFFFF)
        }
    }
    private fun applyKeyboardBackground(view: LinearLayout) {
        val uriText = Prefs.get(this).getString("background_uri", null) ?: return
        try { contentResolver.openInputStream(Uri.parse(uriText))?.use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream)
            if (bitmap != null) view.background = BitmapDrawable(resources, bitmap).apply { alpha = 115 }
        } } catch (_: Exception) {}
    }
    private fun pasteClipboard() {
        val manager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData? = manager.primaryClip
        if (clip != null && clip.itemCount > 0) currentInputConnection?.commitText(clip.getItemAt(0).coerceToText(this), 1)
        else Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show()
    }
    private fun enterKey() {
        val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) currentInputConnection?.performEditorAction(action)
        else currentInputConnection?.commitText("\n", 1)
    }
    private fun vibrate() {
        try { val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE)) else @Suppress("DEPRECATION") vibrator.vibrate(12)
        } catch (_: Exception) {}
    }
}
