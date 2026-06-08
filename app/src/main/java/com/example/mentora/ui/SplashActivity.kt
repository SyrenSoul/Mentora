package com.example.mentora.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.mentora.R

@SuppressLint("CustomSplashScreen")
@Suppress("DEPRECATION")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN

        setContentView(R.layout.activity_splash)

        val imgLogo = findViewById<ImageView>(R.id.imgLogo)

        imgLogo.scaleX = 0.7f
        imgLogo.scaleY = 0.7f

        imgLogo.animate()
            .alpha(1f)
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(1200)
            .start()

        Handler(Looper.getMainLooper()).postDelayed({

            startActivity(
                Intent(this, MainActivity::class.java)
            )

            finish()

        }, 2200)
    }
}