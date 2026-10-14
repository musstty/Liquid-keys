package com.liquidkeys.keyboard

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.widget.*

class SettingsActivity : Activity() {
    private val prefs by lazy { Prefs.get(this) }
    private val bg = Color.rgb(9, 15, 31)
    private val white = Color.WHITE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(22, 24, 22, 28); setBackgroundColor(bg) }
        scroll.addView(root)
        root.addView(TextView(this).apply { text = "LiquidKeys"; textSize = 32f; setTextColor(white); typeface = Typeface.DEFAULT_BOLD })
        root.addView(TextView(this).apply { text = "A keyboard with a liquid-glass feel"; textSize = 15f; setTextColor(Color.LTGRAY); setPadding(0, 4, 0, 22) })
        val enable = button("1. Enable LiquidKeys") { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        root.addView(enable)
        root.addView(button("2. Choose LiquidKeys keyboard") {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        })
        section(root, "APPEARANCE")
        root.addView(choice("Glass theme", listOf("Aurora", "Pure Glass", "Midnight", "Neon"), Prefs.theme(this)) { prefs.edit().putString("theme", it).apply() })
        root.addView(slider("Keyboard height", 200, 340, Prefs.height(this), " px") { prefs.edit().putInt("height", it).apply() })
        root.addView(slider("Key font size", 14, 26, Prefs.fontSize(this), " sp") { prefs.edit().putInt("font", it).apply() })
        root.addView(slider("Glass opacity", 35, 100, Prefs.transparency(this), "%") { prefs.edit().putInt("alpha", it).apply() })
        root.addView(button("Choose keyboard background photo") {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "image/*"; addCategory(Intent.CATEGORY_OPENABLE); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION) }
            try { startActivityForResult(intent, 42) } catch (_: Exception) { toast("Gallery picker unavailable") }
        })
        section(root, "TYPING")
        root.addView(toggle("Show number row", Prefs.numberRow(this)) { prefs.edit().putBoolean("numbers", it).apply() })
        root.addView(toggle("One-handed keyboard", Prefs.oneHanded(this)) { prefs.edit().putBoolean("onehand", it).apply() })
        root.addView(toggle("Haptic feedback", Prefs.haptics(this)) { prefs.edit().putBoolean("haptics", it).apply() })
        root.addView(toggle("Typing sounds", Prefs.sounds(this)) { prefs.edit().putBoolean("sounds", it).apply() })
        section(root, "ABOUT & PRIVACY")
        root.addView(TextView(this).apply { text = "LiquidKeys is an early prototype. Keyboard text is processed locally by the keyboard service; voice typing uses Android's available speech-recognition app and may be processed according to that service's privacy policy. Avoid typing sensitive information into an experimental keyboard."; textSize = 14f; setTextColor(Color.LTGRAY); setPadding(0, 4, 0, 18) })
        root.addView(button("Refresh keyboard (reopen keyboard)") { toast("Switch away from LiquidKeys and back to apply every setting.") })
        setContentView(scroll)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 42 && resultCode == RESULT_OK) {
            data?.data?.let { uri: Uri ->
                try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
                prefs.edit().putString("background_uri", uri.toString()).apply()
                toast("Background saved. Reopen the keyboard to preview it.")
            }
        }
    }
    private fun section(root: LinearLayout, title: String) { root.addView(TextView(this).apply { text = title; textSize = 12f; typeface = Typeface.DEFAULT_BOLD; setTextColor(0xFF79DFFF.toInt()); setPadding(2, 22, 0, 8) }) }
    private fun button(label: String, action: () -> Unit) = TextView(this).apply {
        text = label; textSize = 16f; setTextColor(white); gravity = Gravity.CENTER_VERTICAL; setPadding(16, 15, 16, 15)
        background = shape(0x3379DFFF, 18); setOnClickListener { action() }
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.bottomMargin = 9; layoutParams = lp
    }
    private fun toggle(label: String, initial: Boolean, changed: (Boolean) -> Unit): Switch {
        return Switch(this).apply { text = label; textSize = 15f; setTextColor(white); isChecked = initial; setPadding(4, 10, 4, 10); setOnCheckedChangeListener { _, checked -> changed(checked) } }
    }
    private fun choice(label: String, options: List<String>, current: String, changed: (String) -> Unit): LinearLayout {
        val row = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 8, 0, 8) }
        row.addView(TextView(this).apply { text = label; textSize = 15f; setTextColor(white) })
        val spinner = Spinner(this); spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        spinner.setSelection(options.indexOf(current).coerceAtLeast(0)); spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) { changed(options[position]) }
        }; row.addView(spinner); return row
    }
    private fun slider(label: String, min: Int, max: Int, initial: Int, suffix: String, changed: (Int) -> Unit): LinearLayout {
        val row = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 10, 0, 8) }
        val value = TextView(this).apply { setTextColor(white); textSize = 15f; text = "$label: $initial$suffix" }
        val seek = SeekBar(this).apply { this.max = max - min; progress = initial - min; setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) { value.text = "$label: ${p + min}$suffix"; if (fromUser) changed(p + min) }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        }) }
        row.addView(value); row.addView(seek); return row
    }
    private fun shape(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = radius.toFloat(); setStroke(1, 0x6679DFFF) }
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()
}
