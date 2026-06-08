package com.example.mentora.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.mentora.R

class EvaluationResultActivity : AppCompatActivity() {

    private var idMurid = ""
    private var nama = ""
    private var tipe = ""

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluation_result)

        val toolbar =
            findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val tvNilai = findViewById<TextView>(R.id.tvNilai)
        val tvDurasi = findViewById<TextView>(R.id.tvDurasi)
        val tvSalah = findViewById<TextView>(R.id.tvSalah)
        val tvMateri = findViewById<TextView>(R.id.tvMateri)
        val tvPredikat = findViewById<TextView>(R.id.tvPredikat)
        val tvWrongItems = findViewById<TextView>(R.id.tvWrongItems)
        val imgResult = findViewById<ImageView>(R.id.imgResult)
        val star1 = findViewById<ImageView>(R.id.star1)
        val star2 = findViewById<ImageView>(R.id.star2)
        val star3 = findViewById<ImageView>(R.id.star3)
        val btnRiwayat = findViewById<Button>(R.id.btnRiwayat)

        idMurid = intent.getStringExtra("ID_MURID") ?: ""
        nama = intent.getStringExtra("NAMA_MURID") ?: ""
        tipe = intent.getStringExtra("TIPE") ?: ""

        val score = intent.getIntExtra("SCORE", 0)
        val bintang = intent.getIntExtra("BINTANG", 1)
        val durasi = intent.getIntExtra("DURASI", 0)
        val salah = intent.getIntExtra("SALAH", 0)
        val materi = intent.getStringExtra("MATERI") ?: "-"
        val wrongItems = intent.getStringArrayListExtra("WRONG_ITEMS") ?: arrayListOf()

        simpanHistory(
            score,
            bintang,
            durasi,
            salah,
            materi,
            wrongItems
        )

        val menit = durasi / 60
        val detik = durasi % 60

        tvNilai.text = "Nilai $score"

        tvDurasi.text = String.format("%d:%02d", menit, detik)

        tvSalah.text = salah.toString()
        tvMateri.text = materi
        tvPredikat.text =
            when {
                score >= 90 -> "Sangat Baik"
                score >= 70 -> "Baik"
                else ->
                    "Perlu Latihan"
            }

        tvWrongItems.text =

            if (wrongItems.isEmpty()) {

                "Tidak ada kesalahan 🎉"

            } else {

                wrongItems.joinToString(
                    "\n"
                ) {
                    "❌ $it"
                }
            }

        val img = when (bintang) {

            3 -> R.drawable.ic_star_3

            2 -> R.drawable.ic_star_2

            else -> R.drawable.ic_star_1
        }

        imgResult.setImageResource(img)

        star1.visibility =
            if (bintang >= 1)
                ImageView.VISIBLE
            else
                ImageView.INVISIBLE

        star2.visibility =
            if (bintang >= 2)
                ImageView.VISIBLE
            else
                ImageView.INVISIBLE

        star3.visibility =
            if (bintang >= 3)
                ImageView.VISIBLE
            else
                ImageView.INVISIBLE

        btnRiwayat.setOnClickListener {

            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    @SuppressLint("UseKtx")
    private fun simpanHistory(
        score: Int,
        bintang: Int,
        durasi: Int,
        salah: Int,
        materi: String,
        wrongItems: List<String>
    ) {

        val pref =
            getSharedPreferences(
                "EVALUATION_HISTORY",
                MODE_PRIVATE
            )

        val key =
            "${idMurid}_${tipe}"

        val history =
            pref.getStringSet(
                key,
                mutableSetOf()
            )?.toMutableSet()
                ?: mutableSetOf()

        val item = listOf(
            materi,
            score,
            bintang,
            durasi,
            salah,
            System.currentTimeMillis(),
            wrongItems.joinToString("|")
        ).joinToString(";;")


        android.util.Log.d(
            "HISTORY_SAVE",
            "KEY = $key"
        )

        android.util.Log.d(
            "HISTORY_SAVE",
            "ITEM = $item"
        )

        history.add(item)

        pref.edit()
            .putStringSet(
                key,
                history
            )
            .apply()
    }
}