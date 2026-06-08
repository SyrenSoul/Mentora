package com.example.mentora

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class MentoraApp : Application() {

    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}