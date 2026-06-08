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
import androidx.cardview.widget.CardView
import com.example.mentora.R

class EvaluationQuizActivity : AppCompatActivity() {

    private lateinit var container: GridLayout
    private lateinit var tvJudul: TextView
    private lateinit var btnVolume: CardView

    private var stage = 1
    private var totalSalah = 0

    private var idMurid = ""
    private var nama = ""
    private var tipe = ""

    private var startTime = 0L

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var unlockedItems: List<String>
    private lateinit var soalEvaluasi: List<String>

    private var currentQuestion = ""

    private val wrongItems =
        mutableListOf<String>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluation_quiz)

        container = findViewById(R.id.containerPilihan)
        tvJudul = findViewById(R.id.tvJudulQuiz)
        btnVolume = findViewById(R.id.btnVolume)

        idMurid = intent.getStringExtra("ID_MURID") ?: ""
        nama = intent.getStringExtra("NAMA_MURID") ?: ""
        tipe = intent.getStringExtra("TIPE") ?: ""

        unlockedItems = getLearnedItems()

        val temp =
            unlockedItems.shuffled()
                .toMutableList()

        while (temp.size < 10) {
            temp.add(
                unlockedItems.random()
            )
        }

        soalEvaluasi =
            temp.shuffled()
                .take(10)

        startTime = System.currentTimeMillis()

        loadStage()

        btnVolume.setOnClickListener {
            it.clickAnimation {
                playQuestionSound()
            }
        }

        onBackPressedDispatcher.addCallback(this) {

            val view = layoutInflater.inflate(
                R.layout.dialog_keluar_quiz,
                null
            )

            val dialog = AlertDialog.Builder(this@EvaluationQuizActivity)
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

        currentQuestion = soalEvaluasi[stage - 1]

        container.removeAllViews()

        val jumlahPilihan = when (stage) {
            in 1..3 -> 2
            in 4..6 -> 3
            else -> 4
        }

        tvJudul.text = "Evaluasi $stage/10"

        val listPilihan = generatePilihan(currentQuestion, jumlahPilihan)

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
                    if (item == currentQuestion) {
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
            convertAngkaToText(currentQuestion)
        } else {
            currentQuestion.lowercase()
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

    private fun generatePilihan(
        jawaban: String,
        jumlah: Int
    ): List<String> {

        val semuaData = getSemuaData()
        val pilihan = mutableSetOf<String>()

        pilihan.add(jawaban)

        while (pilihan.size < jumlah) {pilihan.add(semuaData.random())}
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

    private fun salah() {
        totalSalah++

        wrongItems.add(currentQuestion)

        playSound("wrong")
        showPopup(R.drawable.ic_wrong)
    }

    private fun benar() {
        playSound("correct")
        showPopup(R.drawable.ic_correct)

        stage++

        if (stage > 10) {
            Handler(Looper.getMainLooper()).postDelayed({
                selesaiEvaluasi()
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

    private fun getLearnedItems(): List<String> {

        val pref =
            getSharedPreferences(
                "LEARNED",
                MODE_PRIVATE
            )

        val listLevel = when (tipe) {

            "HURUF" ->
                ('A'..'Z').map {
                    it.toString()
                }

            "ANGKA" ->
                (0..9).map {
                    it.toString()
                }

            "WARNA" ->
                listOf(
                    "Putih","Hitam","Merah",
                    "Kuning","Biru",
                    "Hijau","Ungu",
                    "Oranye","Coklat",
                    "Abu-abu","Pink"
                )

            else -> emptyList()
        }

        return listLevel.filter {

            pref.getBoolean(
                "${idMurid}_${tipe}_$it",
                false
            )
        }
    }

    private fun selesaiEvaluasi() {

        val benar = 10 - totalSalah
        val score = benar * 10
        val bintang = when {
            score >= 80 -> 3
            score >= 60 -> 2
            else -> 1
        }
        val durasi = ((System.currentTimeMillis()- startTime) / 1000).toInt()

        val intent = Intent(this,EvaluationResultActivity::class.java)
        intent.putExtra("SCORE",score)
        intent.putExtra("BINTANG",bintang)
        intent.putExtra("SALAH",totalSalah)
        intent.putExtra("DURASI",durasi)
        intent.putExtra("TIPE",tipe)
        intent.putExtra("ID_MURID",idMurid)
        intent.putExtra("NAMA_MURID",nama)
        intent.putExtra("MATERI",getMateriLabel())
        intent.putStringArrayListExtra("WRONG_ITEMS",ArrayList(wrongItems))

        startActivity(intent)

        finish()
    }

    private fun getMateriLabel(): String {
        return when (tipe) {
            "HURUF" -> "${unlockedItems.first()}-${unlockedItems.last()}"
            "ANGKA" -> "${unlockedItems.first()}-${unlockedItems.last()}"
            "WARNA" -> "${unlockedItems.size} warna"
            else -> "-"
        }
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