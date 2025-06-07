package com.example.myapplication

import android.app.Application
import android.content.Context.MODE_PRIVATE
import androidx.appcompat.app.AppCompatDelegate

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化主题设置
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val theme = sharedPreferences.getString("theme", "System Default") ?: "System Default"
        val mode = when (theme) {
            "Light" -> AppCompatDelegate.MODE_NIGHT_NO
            "Dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}