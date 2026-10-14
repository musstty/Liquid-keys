package com.liquidkeys.keyboard

import android.content.Context

object Prefs {
    private const val FILE = "liquid_keys_settings"
    fun get(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    fun theme(context: Context) = get(context).getString("theme", "Aurora") ?: "Aurora"
    fun height(context: Context) = get(context).getInt("height", 260)
    fun fontSize(context: Context) = get(context).getInt("font", 18)
    fun transparency(context: Context) = get(context).getInt("alpha", 88)
    fun haptics(context: Context) = get(context).getBoolean("haptics", true)
    fun sounds(context: Context) = get(context).getBoolean("sounds", false)
    fun numberRow(context: Context) = get(context).getBoolean("numbers", true)
    fun oneHanded(context: Context) = get(context).getBoolean("onehand", false)
}
