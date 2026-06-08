package com.example.mentora.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mentora.R
import com.example.mentora.data.Murid
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Suppress("DEPRECATION")
class QuizActivity : AppCompatActivity() {

    private lateinit var container: GridLayout
    private lateinit var tvJudul: TextView
    private lateinit var btnVolume: androidx.cardview.widget.CardView

    private var stage = 1
    private var totalSalah = 0
    private var idMurid = ""
    private var level = ""
    private var tipe = ""
    private var startTime = 0L
    private var mediaPlayer: MediaPlayer? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        container = findViewById(R.id.containerPilihan)
        tvJudul = findViewById(R.id.tvJudulQuiz)
        btnVolume = findViewById(R.id.btnVolume)

        idMurid = intent.getStringExtra("ID_MURID") ?: ""
        level = intent.getStringExtra("LEVEL") ?: ""
        tipe = intent.getStringExtra("TIPE") ?: ""
        startTime = System.currentTimeMillis()

        btnVolume.setOnClickListener {
            it.clickAnimation {
                playQuestionSound()
            }
        }

        loadStage()

        onBackPressedDispatcher.addCallback(this) {

            val view = layoutInflater.inflate(
                R.layout.dialog_keluar_quiz,
                null
            )

            val dialog = AlertDialog.Builder(this@QuizActivity)
                .setView(view)
                .create()

            view.findViewById<ImageView>(R.id.imgWarning)
                .setImageResource(R.drawable.ic_no)

            view.findViewById<TextView>(R.id.tvJudul)
                .text = "Tidak Bisa Keluar"

            view.findViewById<TextView>(R.id.tvPesan)
                .text = "Selesaikan quiz terlebih dahulu"

            view.findViewById<Button>(R.id.btnOke)
                .setOnClickListener {
                    dialog.dismiss()
                }

            dialog.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadStage() {
        container.removeAllViews()

        val jumlahPilihan = when (stage) {
            in 1..3 -> 2
            in 4..6 -> 3
            else -> 4
        }

        tvJudul.text = "Stage $stage"

        val listPilihan = generatePilihan(jumlahPilihan)

        val isLandscape =
            resources.configuration.orientation ==
                    android.content.res.Configuration.ORIENTATION_LANDSCAPE

        listPilihan.forEach { item ->

            val img = ImageView(this)

            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = if (isLandscape) 180 else 300
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.setMargins(16, 16, 16, 16)

            img.layoutParams = params
            img.scaleType = ImageView.ScaleType.CENTER_INSIDE
            img.setPadding(20, 20, 20, 20)
            img.setBackgroundResource(R.drawable.bg_card)

            val resId = getDrawableId(item)
            img.setImageResource(resId)

            img.setOnClickListener {
                it.clickAnimation {
                    if (item == level) {
                        benar()
                    } else {
                        salah()
                    }
                }
            }

            container.addView(img)
        }
    }

    private fun playQuestionSound() {
        val namaSound = if (tipe == "ANGKA") {
            convertAngkaToText(level)
        } else {
            level.lowercase()
                .replace(" ", "_")
                .replace("-", "_")
        }

        playSound(namaSound)
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

    private fun generatePilihan(jumlah: Int): List<String> {
        val semuaData = getSemuaData()

        val pilihan = mutableSetOf<String>()
        pilihan.add(level)

        while (pilihan.size < jumlah) {
            pilihan.add(semuaData.random())
        }

        return pilihan.shuffled()
    }

    private fun getSemuaData(): List<String> {
        return when (tipe) {
            "HURUF" -> ('A'..'Z').map { it.toString() }
            "ANGKA" -> (0..9).map { it.toString() }
            "WARNA" -> listOf(
                "Putih", "Hitam", "Merah", "Kuning", "Biru",
                "Hijau", "Ungu", "Oranye", "Coklat", "Abu-abu", "Pink"
            )
            else -> emptyList()
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun getDrawableId(item: String): Int {
        val nama = "ic_" + item.lowercase()
            .replace(" ", "_")
            .replace("-", "_")

        val resId = resources.getIdentifier(nama, "drawable", packageName)

        return if (resId != 0) resId else R.drawable.ic_default
    }

    private fun getIndexWarna(level: String): Int {
        val list = listOf(
            "Putih", "Hitam", "Merah", "Kuning", "Biru",
            "Hijau", "Ungu", "Oranye", "Coklat", "Abu-abu", "Pink"
        )

        return list.indexOf(level).coerceAtLeast(0)
    }

    private fun salah() {
        totalSalah++

        playSound("wrong")
        showPopup(R.drawable.ic_wrong)
    }

    private fun benar() {
        playSound("correct")
        showPopup(R.drawable.ic_correct)

        stage++

        if (stage > 10) {
            Handler(Looper.getMainLooper()).postDelayed({
                selesaiQuiz()
            }, 600)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                loadStage()
            }, 600)
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun playSound(name: String) {
        val resId = resources.getIdentifier(name, "raw", packageName)

        if (resId != 0) {
            mediaPlayer?.release()

            mediaPlayer = MediaPlayer.create(this, resId)
            mediaPlayer?.start()
        }
    }

    private fun showPopup(icon: Int) {
        val img = ImageView(this)
        img.setImageResource(icon)

        val dialog = AlertDialog.Builder(this)
            .setView(img)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 500)
    }

    @SuppressLint("UseKtx")
    private fun updateProgress() {
        val sharedPreferences = getSharedPreferences("DATA_MURID_$tipe", MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("LIST_MURID", null)

        if (json != null) {
            val type = object : TypeToken<MutableList<Murid>>() {}.type
            val list: MutableList<Murid> = gson.fromJson(json, type)

            val murid = list.find { it.id == idMurid }

            if (murid != null) {

                val progressBaru = when (tipe) {
                    "HURUF" -> level[0] - 'A' + 1
                    "ANGKA" -> level.toInt() + 1
                    "WARNA" -> getIndexWarna(level) + 1
                    else -> 0
                }

                when (tipe) {
                    "HURUF" -> if (progressBaru > murid.hurufProgress)
                        murid.hurufProgress = progressBaru

                    "ANGKA" -> if (progressBaru > murid.angkaProgress)
                        murid.angkaProgress = progressBaru

                    "WARNA" -> if (progressBaru > murid.warnaProgress)
                        murid.warnaProgress = progressBaru
                }

                murid.lastPlayed = System.currentTimeMillis()

                val updatedJson = gson.toJson(list)
                sharedPreferences.edit()
                    .putString("LIST_MURID", updatedJson)
                    .apply()
            }
        }
    }

    @SuppressLint("UseKtx")
    private fun selesaiQuiz() {
        val bintang = when {
            totalSalah <= 3 -> 3
            totalSalah <= 5 -> 2
            else -> 1
        }

        val nama = intent.getStringExtra("NAMA_MURID") ?: ""

        val pref = getSharedPreferences("PROGRESS", MODE_PRIVATE)
        val key = "${idMurid}_${tipe}_$level"
        val current = pref.getInt(key, 0)

        if (bintang > current) {
            pref.edit().putInt(key, bintang).apply()

            Thread {
                updateProgress()
            }.start()
        }

        val durasi =
            ((System.currentTimeMillis() - startTime) / 1000).toInt()

        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("BINTANG", bintang)
        intent.putExtra("LEVEL", level)
        intent.putExtra("TIPE", tipe)
        intent.putExtra("ID_MURID", idMurid)
        intent.putExtra("NAMA_MURID", nama)
        intent.putExtra("DURASI", durasi)
        intent.putExtra("SALAH", totalSalah)

        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        finish()
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

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}