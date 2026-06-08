package com.example.mentora.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.mentora.R

@Suppress("DEPRECATION")
class ResultActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null

    @SuppressLint("UseKtx", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val tvHasil = findViewById<TextView>(R.id.tvHasil)
        val imgResult = findViewById<ImageView>(R.id.imgResult)
        val star1 = findViewById<ImageView>(R.id.star1)
        val star2 = findViewById<ImageView>(R.id.star2)
        val star3 = findViewById<ImageView>(R.id.star3)
        val tvDurasi = findViewById<TextView>(R.id.tvDurasi)
        val tvSalah = findViewById<TextView>(R.id.tvSalah)
        val btnUlang = findViewById<ImageView>(R.id.btnUlang)
        val btnLevel = findViewById<Button>(R.id.btnLevel)

        val bintang = intent.getIntExtra("BINTANG", 1)
        val level = intent.getStringExtra("LEVEL") ?: ""

        val tipe = intent.getStringExtra("TIPE") ?: ""
        val idMurid = intent.getStringExtra("ID_MURID") ?: ""
        val nama = intent.getStringExtra("NAMA_MURID") ?: ""

        getSharedPreferences("LEARNED", MODE_PRIVATE)
            .edit()
            .putBoolean("${idMurid}_${tipe}_${level}",true)
            .apply()

        val durasi = intent.getIntExtra("DURASI", 0)
        val salah = intent.getIntExtra("SALAH", 0)

        val pref = getSharedPreferences("PERFORMA", MODE_PRIVATE)

        val menit = durasi / 60
        val detik = durasi % 60

        val text = when (bintang) {
            3 -> "Sempurna!"
            2 -> "Hebat!"
            else -> "Ayo coba lagi!"
        }
        tvHasil.text = text

        tvDurasi.text = String.format("%d:%02d", menit, detik)
        tvSalah.text = salah.toString()

        simpanPerformaTerbaik(
            pref,
            idMurid,
            tipe,
            level,
            durasi,
            salah,
            bintang
        )

        val img = when (bintang) {
            3 -> R.drawable.ic_star_3
            2 -> R.drawable.ic_star_2
            else -> R.drawable.ic_star_1
        }
        imgResult.setImageResource(img)

        star1.visibility = if (bintang >= 1) View.VISIBLE else View.INVISIBLE
        star2.visibility = if (bintang >= 2) View.VISIBLE else View.INVISIBLE
        star3.visibility = if (bintang >= 3) View.VISIBLE else View.INVISIBLE

        playSound("win")

        btnUlang.setOnClickListener {
            it.clickAnimation {
                val intent = Intent(this, QuizActivity::class.java)
                intent.putExtra("LEVEL", level)
                intent.putExtra("TIPE", tipe)
                intent.putExtra("ID_MURID", idMurid)
                intent.putExtra("NAMA_MURID", nama)
                startActivity(intent)
                finish()
            }
        }

        btnLevel.setOnClickListener {
            it.clickAnimation {
                val intent = Intent(this, LevelActivity::class.java)
                intent.putExtra("TIPE", tipe)
                intent.putExtra("ID_MURID", idMurid)
                intent.putExtra("NAMA_MURID", nama)
                startActivity(intent)
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }

        animateStars(star1, star2, star3)

        onBackPressedDispatcher.addCallback(this) {
            val intent = Intent(this@ResultActivity, LevelActivity::class.java)
            intent.putExtra("LEVEL", level)
            intent.putExtra("TIPE", tipe)
            intent.putExtra("ID_MURID", idMurid)
            intent.putExtra("NAMA_MURID", nama)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun playSound(name: String) {
        val resId = resources.getIdentifier(name, "raw", packageName)
        if (resId != 0) {
            mediaPlayer = MediaPlayer.create(this, resId)
            mediaPlayer?.start()
        }
    }

    private fun animateStars(vararg stars: ImageView) {
        stars.forEachIndexed { index, star ->
            star.scaleX = 0f
            star.scaleY = 0f

            star.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay((index * 200).toLong())
                .setDuration(300)
        }
    }

    @SuppressLint("UseKtx")
    private fun simpanPerformaTerbaik(
        pref: android.content.SharedPreferences,
        idMurid: String,
        tipe: String,
        level: String,
        durasi: Int,
        salah: Int,
        bintang: Int
    ) {

        val keyTime =
            "${idMurid}_${tipe}_${level}_TIME"

        val keyWrong =
            "${idMurid}_${tipe}_${level}_WRONG"

        val keyStar =
            "${idMurid}_${tipe}_${level}_STAR"

        val oldTime =
            pref.getInt(keyTime, Int.MAX_VALUE)

        val oldWrong =
            pref.getInt(keyWrong, Int.MAX_VALUE)

        val lebihBaik =
            durasi < oldTime ||
                    (durasi == oldTime && salah < oldWrong)

        if (lebihBaik) {

            pref.edit()
                .putInt(keyTime, durasi)
                .putInt(keyWrong, salah)
                .putInt(keyStar, bintang)
                .apply()
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