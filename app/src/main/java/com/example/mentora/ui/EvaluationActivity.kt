package com.example.mentora.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mentora.R
import com.example.mentora.data.EvaluationHistory
import com.example.mentora.data.EvaluationHistoryAdapter
import com.example.mentora.data.Murid
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

@Suppress("DEPRECATION")
class EvaluationActivity : AppCompatActivity() {

    private lateinit var recyclerHistory: RecyclerView
    private var idMurid = ""
    private var nama = ""
    private var tipe = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluation)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.title = "Tes Evaluasi"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        idMurid = intent.getStringExtra("ID_MURID") ?: ""
        nama = intent.getStringExtra("NAMA_MURID") ?: ""
        tipe = intent.getStringExtra("TIPE") ?: ""

        val tvMateri = findViewById<TextView>(R.id.tvMateri)
        val tvJumlahSoal = findViewById<TextView>(R.id.tvJumlahSoal)
        val btnMulai = findViewById<CardView>(R.id.btnMulaiEvaluasi)
        recyclerHistory = findViewById(R.id.recyclerHistory)

        tvJumlahSoal.text = "Jumlah soal : 10"
        tvMateri.text = getMateriText()

        btnMulai.setOnClickListener {
            it.clickAnimation {
                val intent = Intent(this,EvaluationQuizActivity::class.java)
                intent.putExtra("ID_MURID", idMurid)
                intent.putExtra("NAMA_MURID", nama)
                intent.putExtra("TIPE", tipe)
                startActivity(intent)
            }
        }
        loadHistory()
    }

    private fun getMateriText(): String {
        val items = getLearnedItems()

        if (items.isEmpty())
            return "Belum ada materi yang dipelajari"

        return when (tipe) {

            "HURUF" -> "Materi tersedia : ${items.size} huruf (${items.first()}-${items.last()})"
            "ANGKA" -> "Materi tersedia : ${items.size} angka (${items.first()}-${items.last()})"
            "WARNA" -> "Materi tersedia : ${items.size} warna"

            else -> ""
        }
    }

    private fun getLearnedItems(): List<String> {
        val pref = getSharedPreferences("LEARNED", MODE_PRIVATE)

        val listLevel = when (tipe) {

            "HURUF" -> ('A'..'Z').map {it.toString()}
            "ANGKA" -> (0..9).map {it.toString() }
            "WARNA" -> listOf(
                        "Putih","Hitam","Merah",
                        "Kuning","Biru",
                        "Hijau","Ungu",
                        "Oranye","Coklat",
                        "Abu-abu","Pink")

            else -> emptyList()
        }

        return listLevel.filter {
            pref.getBoolean("${idMurid}_${tipe}_$it",false)
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

    private fun loadHistory() {

        val pref = getSharedPreferences("EVALUATION_HISTORY", MODE_PRIVATE)

        val key = "${idMurid}_${tipe}"

        val history = pref.getStringSet(key,emptySet()) ?: emptySet()

        val list =
            history.mapNotNull {

                val part = it.split(";;")

                if (part.size < 7)
                    return@mapNotNull null

                EvaluationHistory(
                    materi = part[0],
                    score = part[1].toInt(),
                    bintang = part[2].toInt(),
                    durasi = part[3].toInt(),
                    salah = part[4].toInt(),
                    tanggal = part[5].toLong(),
                    wrongItems = part[6]
                )
            }
                .sortedByDescending {
                    it.tanggal
                }

        recyclerHistory.layoutManager = LinearLayoutManager(this)
        recyclerHistory.adapter = EvaluationHistoryAdapter(list) {
                showHistoryDetail(it)
            }
    }

    @SuppressLint("UseKtx", "DiscouragedApi", "SetTextI18n", "DefaultLocale")
    private fun showHistoryDetail(item: EvaluationHistory) {
        val view = layoutInflater.inflate(R.layout.dialog_evaluation_detail,null)

        val dialog =
            androidx.appcompat.app.AlertDialog
                .Builder(this)
                .setView(view)
                .create()

        val imgAvatar = view.findViewById<ImageView>(R.id.imgAvatar)
        val tvNama = view.findViewById<TextView>(R.id.tvNama)
        val tvLevel = view.findViewById<TextView>(R.id.tvLevel)
        val tvBintang = view.findViewById<TextView>(R.id.tvBintang)
        val tvNilai = view.findViewById<TextView>(R.id.tvNilai)
        val tvTime = view.findViewById<TextView>(R.id.tvTime)
        val tvWrong = view.findViewById<TextView>(R.id.tvWrong)
        val tvPredikat = view.findViewById<TextView>(R.id.tvPredikat)
        val tvTanggal = view.findViewById<TextView>(R.id.tvTanggal)
        val btnShare = view.findViewById<Button>(R.id.btnShare)

        val murid = getMurid()

        murid?.let {

            val avatar = it.avatar

            Glide.with(this)
                .load(
                    when {
                        avatar.startsWith("/") -> File(avatar)

                        avatar.startsWith("content://") -> android.net.Uri.parse(avatar)

                        avatar.isNotEmpty() ->
                            resources.getIdentifier(
                                avatar,
                                "drawable",
                                packageName
                            )

                        else -> R.drawable.ic_avatar_default
                    }
                )
                .circleCrop()
                .placeholder(R.drawable.ic_avatar_default)
                .into(imgAvatar)
        }

        tvNama.text = nama
        tvLevel.text = "Tes materi ${item.materi}"
        tvBintang.text = "⭐".repeat(item.bintang)
        tvNilai.text = "Nilai ${item.score}"

        val menit = item.durasi / 60
        val detik = item.durasi % 60

        tvTime.text = String.format("%d:%02d", menit, detik)
        tvWrong.text = item.salah.toString()

        tvPredikat.text =
            if (
                item.wrongItems.isBlank()
            ) {
                "🎉 Tidak Ada Kesalahan"
            } else {
                "Materi Perlu Dilatih\n" +
                        item.wrongItems
                            .split("|")
                            .joinToString("\n") {
                                "• $it"
                            }
            }

        tvTanggal.text = java.text.SimpleDateFormat("d MMM yyyy, HH:mm", java.util.Locale("id","ID"))
            .format(java.util.Date(item.tanggal))

        btnShare.setOnClickListener {
            btnShare.visibility = View.GONE
            shareEvaluasi(item)
            btnShare.visibility = View.VISIBLE
        }
        dialog.show()
    }

    private fun getMurid(): Murid? {

        val prefName = when (tipe) {
            "HURUF" -> "DATA_MURID_HURUF"
            "ANGKA" -> "DATA_MURID_ANGKA"
            "WARNA" -> "DATA_MURID_WARNA"
            else -> return null
        }

        val pref = getSharedPreferences(prefName, MODE_PRIVATE)
        val json = pref.getString("LIST_MURID", null) ?: return null
        val type = object : TypeToken<MutableList<Murid>>() {}.type
        val list: MutableList<Murid> = Gson().fromJson(json, type)
        return list.find {it.id == idMurid}
    }

    @SuppressLint("InflateParams", "DiscouragedApi", "UseKtx", "DefaultLocale", "SetTextI18n")
    private fun shareEvaluasi(item: EvaluationHistory){
        val view = layoutInflater.inflate(R.layout.layout_share_prestasi,null)

        val murid = getMurid()

        view.findViewById<TextView>(R.id.tvNama)
            .text = nama

        view.findViewById<TextView>(R.id.tvLevel)
            .text = "Evaluasi ${item.materi}"

        view.findViewById<TextView>(R.id.tvBintang)
            .text = "⭐".repeat(item.bintang)

        val menit = item.durasi / 60
        val detik = item.durasi % 60

        view.findViewById<TextView>(R.id.tvTime)
            .text = "$menit:${String.format("%02d", detik)}"

        view.findViewById<TextView>(R.id.tvWrong)
            .text = item.salah.toString()

        val predikat =
            when {
                item.score >= 90 ->
                    "🏆 Sangat Baik"

                item.score >= 70 ->
                    "⭐ Baik"

                else ->
                    "📚 Perlu Latihan"
            }

        view.findViewById<TextView>(R.id.tvPredikat)
            .text = "Nilai ${item.score}\n$predikat"

        val tanggal = java.text.SimpleDateFormat("dd MMMM yyyy",java.util.Locale("id"))
            .format(java.util.Date(item.tanggal))

        view.findViewById<TextView>(R.id.tvTanggal)
            .text = tanggal

        val imgAvatar = view.findViewById<ImageView>(R.id.imgAvatar)

        murid?.let {
            val avatar = it.avatar
            Glide.with(this)
                .load(
                    when {

                        avatar.startsWith("/") -> File(avatar)

                        avatar.startsWith("content://") -> android.net.Uri.parse(avatar)

                        avatar.isNotEmpty() ->
                            resources.getIdentifier(
                                avatar,
                                "drawable",
                                packageName
                            )

                        else -> R.drawable.ic_avatar_default
                    }
                )
                .circleCrop()
                .into(imgAvatar)
        }
        shareViewAsImage(view)
    }

    @SuppressLint("UseKtx")
    private fun shareViewAsImage(view: View) {

        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        view.layout(
            0,
            0,
            view.measuredWidth,
            view.measuredHeight
        )

        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        canvas.drawColor(
            android.graphics.Color.WHITE
        )

        view.draw(canvas)

        val file = File(cacheDir,"prestasi.png")

        val stream = FileOutputStream(file)

        bitmap.compress(
            Bitmap.CompressFormat.PNG,
            100,
            stream
        )

        stream.flush()
        stream.close()

        val uri = FileProvider.getUriForFile(
            this,
            "$packageName.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND)

        intent.type = "image/png"

        intent.putExtra(Intent.EXTRA_STREAM,uri)

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startActivity(Intent.createChooser(intent,"Bagikan Prestasi"))
    }

    override fun onResume() {
        super.onResume()

        loadHistory()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}