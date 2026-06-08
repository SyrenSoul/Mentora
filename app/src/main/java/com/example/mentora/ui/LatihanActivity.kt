package com.example.mentora.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.example.mentora.R

@Suppress("DEPRECATION")
class LatihanActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var idMurid = ""
    private var level = ""
    private var tipe = ""
    private var nama = ""

    @SuppressLint("DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latihan)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        idMurid = intent.getStringExtra("ID_MURID") ?: ""
        level = intent.getStringExtra("LEVEL") ?: ""
        tipe = intent.getStringExtra("TIPE") ?: ""
        nama = intent.getStringExtra("NAMA_MURID") ?: ""

        supportActionBar?.title = when (tipe) {
            "HURUF" -> "Huruf $level"
            "ANGKA" -> "Angka $level"
            "WARNA" -> "Warna $level"
            else -> level
        }

        val imgLatihan = findViewById<ImageView>(R.id.imgLatihan)
        val cardGambar = findViewById<CardView>(R.id.cardGambar)
        val btnLanjut = findViewById<CardView>(R.id.btnLanjut)

        val namaDrawable = "ic_" + level.lowercase()
            .replace(" ", "_")
            .replace("-", "_")

        val resId = resources.getIdentifier(namaDrawable, "drawable", packageName)
        if (resId != 0) {
            imgLatihan.setImageResource(resId)
        }

        val namaSound = if (tipe == "ANGKA") {
            convertAngkaToText(level)
        } else {
            level.lowercase()
                .replace(" ", "_")
                .replace("-", "_")
        }

        val soundId = resources.getIdentifier(namaSound, "raw", packageName)

        cardGambar.setOnClickListener {
            it.clickAnimation {
                if (soundId != 0) {
                    mediaPlayer?.release()
                    mediaPlayer = MediaPlayer.create(this, soundId)
                    mediaPlayer?.start()
                }
                spawnMiniItems()
            }
        }

        btnLanjut.setOnClickListener {
            it.clickAnimation {
                val intent = Intent(this, QuizActivity::class.java)
                intent.putExtra("LEVEL", level)
                intent.putExtra("TIPE", tipe)
                intent.putExtra("ID_MURID", idMurid)
                intent.putExtra("NAMA_MURID", nama)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            val intent = Intent(this@LatihanActivity, LevelActivity::class.java)
            intent.putExtra("TIPE", tipe)
            intent.putExtra("ID_MURID", idMurid)
            intent.putExtra("NAMA_MURID", nama)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun convertAngkaToText(level: String): String {
        return when (level) {
            "0" -> "nol"
            "1" -> "satu"
            "2" -> "dua"
            "3" -> "tiga"
            "4" -> "empat"
            "5" -> "lima"
            "6" -> "enam"
            "7" -> "tujuh"
            "8" -> "delapan"
            "9" -> "sembilan"
            else -> level.lowercase()
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun spawnMiniItems() {
        val root = findViewById<ViewGroup>(android.R.id.content)

        repeat(6) {

            val mini = ImageView(this)

            val nama = "ic_" + level.lowercase()
                .replace(" ", "_")
                .replace("-", "_")

            val resId = resources.getIdentifier(
                nama,
                "drawable",
                packageName
            )

            mini.setImageResource(
                if (resId != 0) resId else R.drawable.ic_default
            )

            val size = (80..140).random()

            val params = ViewGroup.LayoutParams(size, size)
            mini.layoutParams = params

            mini.alpha = 0f
            mini.scaleX = 0.3f
            mini.scaleY = 0.3f

            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels

            mini.x = (50..screenWidth - 150).random().toFloat()
            mini.y = (150..screenHeight - 250).random().toFloat()

            root.addView(mini)

            mini.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationYBy(-120f)
                .setDuration(700)
                .withEndAction {
                    mini.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            root.removeView(mini)
                        }
                }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }

    fun View.clickAnimation(onEnd: () -> Unit = {}) {
        this.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(80)
            .withEndAction {
                this.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(80)
                    .withEndAction { onEnd() }
            }
    }
}