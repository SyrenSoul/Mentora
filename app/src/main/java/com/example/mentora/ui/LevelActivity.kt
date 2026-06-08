package com.example.mentora.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mentora.R
import com.example.mentora.data.LevelAdapter
import com.example.mentora.data.Murid
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

@Suppress("DEPRECATION")
class LevelActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LevelAdapter
    private lateinit var pref: SharedPreferences
    private var idMurid = ""
    private var nama = ""
    private var tipe = ""
    private lateinit var listLevel: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.title = "Pilih Level"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val cardEvaluasi = findViewById<CardView>(R.id.cardEvaluasi)

        cardEvaluasi.setOnClickListener {

            if (getLearnedCount() < 2) {
                showEvaluasiWarning()
                return@setOnClickListener
            }

            val intent = Intent(this, EvaluationActivity::class.java)

            intent.putExtra("ID_MURID", idMurid)
            intent.putExtra("NAMA_MURID", nama)
            intent.putExtra("TIPE", tipe)

            startActivity(intent)
        }

        recyclerView = findViewById(R.id.recyclerLevel)

        idMurid = intent.getStringExtra("ID_MURID") ?: ""
        nama = intent.getStringExtra("NAMA_MURID") ?: ""
        tipe = intent.getStringExtra("TIPE") ?: ""

        pref = getSharedPreferences("PROGRESS", MODE_PRIVATE)

        listLevel = when (tipe) {
            "HURUF" -> ('A'..'Z').map { it.toString() }
            "ANGKA" -> (0..9).map { it.toString() }
            "WARNA" -> listOf(
                "Putih", "Hitam", "Merah", "Kuning", "Biru",
                "Hijau", "Ungu", "Oranye", "Coklat", "Abu-abu", "Pink"
            )
            else -> emptyList()
        }

        adapter = LevelAdapter(
            listLevel,
            isUnlocked = { position -> isUnlocked(position) },
            getStar = { level -> getStar(level) },
            getTime = { level -> getTime(level) },
            onTimeClick = { level -> showPerformanceDialog(level) },
            onLockedClick = {
                showLockedDialog()
            },
            onClick = { level ->
                val intent = Intent(this, LatihanActivity::class.java)
                intent.putExtra("LEVEL", level)
                intent.putExtra("TIPE", tipe)
                intent.putExtra("ID_MURID", idMurid)
                intent.putExtra("NAMA_MURID", nama)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        )

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter

        onBackPressedDispatcher.addCallback(this) {
            val intent = when (tipe) {
                "HURUF" -> Intent(this@LevelActivity, HurufActivity::class.java)
                "ANGKA" -> Intent(this@LevelActivity, AngkaActivity::class.java)
                "WARNA" -> Intent(this@LevelActivity, WarnaActivity::class.java)
                else -> null
            }

            intent?.let {
                startActivity(it)
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showLockedDialog() {

        val view = layoutInflater.inflate(
            R.layout.dialog_keluar_quiz,
            null
        )

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(view)
            .create()

        view.findViewById<ImageView>(R.id.imgWarning)
            .setImageResource(R.drawable.ic_suggest)

        view.findViewById<TextView>(R.id.tvJudul)
            .text = "Level Terkunci"

        view.findViewById<TextView>(R.id.tvPesan)
            .text = "Selesaikan level sebelumnya dulu ya!"

        view.findViewById<Button>(R.id.btnOke)
            .setOnClickListener {
                dialog.dismiss()
            }

        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showEvaluasiWarning() {

        val view =
            layoutInflater.inflate(
                R.layout.dialog_keluar_quiz,
                null
            )

        val dialog =
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(view)
                .create()

        view.findViewById<ImageView>(R.id.imgWarning).setImageResource(R.drawable.ic_suggest)
        view.findViewById<TextView>(R.id.tvJudul).text = "Belum Bisa Evaluasi"
        view.findViewById<TextView>(R.id.tvPesan).text = "Selesaikan minimal 2 quiz terlebih dahulu sebelum mengikuti tes evaluasi."
        view.findViewById<Button>(R.id.btnOke
            ).setOnClickListener {
                dialog.dismiss()
        }

        dialog.show()
    }

    private fun isUnlocked(position: Int): Boolean {
        if (position == 0) return true

        val prevLevel = listLevel[position - 1]
        val key = "${idMurid}_${tipe}_$prevLevel"

        return pref.getInt(key, 0) > 0
    }

    private fun getStar(level: String): Int {
        val key = "${idMurid}_${tipe}_$level"
        return pref.getInt(key, 0)
    }

    private fun getTime(level: String): Int {

        val prefPerforma =
            getSharedPreferences("PERFORMA", MODE_PRIVATE)

        val key =
            "${idMurid}_${tipe}_${level}_TIME"

        return prefPerforma.getInt(key, 0)
    }

    private fun getMurid(): Murid? {

        val prefName = when (tipe) {
            "HURUF" -> "DATA_MURID_HURUF"
            "ANGKA" -> "DATA_MURID_ANGKA"
            "WARNA" -> "DATA_MURID_WARNA"
            else -> return null
        }

        val pref =
            getSharedPreferences(prefName, MODE_PRIVATE)

        val json =
            pref.getString("LIST_MURID", null)
                ?: return null

        val type =
            object : TypeToken<MutableList<Murid>>() {}.type

        val list: MutableList<Murid> =
            Gson().fromJson(json, type)

        return list.find {
            it.id == idMurid
        }
    }

    private fun getLearnedCount(): Int {

        val pref = getSharedPreferences("LEARNED", MODE_PRIVATE)

        val listLevel = when (tipe) {
            "HURUF" -> ('A'..'Z').map {it.toString()}
            "ANGKA" -> (0..9).map {it.toString() }
            "WARNA" -> listOf(
                            "Putih","Hitam","Merah",
                            "Kuning","Biru",
                            "Hijau","Ungu",
                            "Oranye","Coklat",
                            "Abu-abu","Pink"
                        )
            else -> emptyList()
        }

        return listLevel.count {
            pref.getBoolean("${idMurid}_${tipe}_$it",false)
        }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale", "UseKtx", "DiscouragedApi")
    private fun showPerformanceDialog(level: String) {

        val view =
            layoutInflater.inflate(
                R.layout.dialog_performa,
                null
            )

        val dialog =
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(view)
                .create()

        val prefPerforma =
            getSharedPreferences("PERFORMA", MODE_PRIVATE)

        val time =
            prefPerforma.getInt(
                "${idMurid}_${tipe}_${level}_TIME",
                0
            )

        val wrong =
            prefPerforma.getInt(
                "${idMurid}_${tipe}_${level}_WRONG",
                0
            )

        val star =
            prefPerforma.getInt(
                "${idMurid}_${tipe}_${level}_STAR",
                0
            )

        val menit = time / 60
        val detik = time % 60

        view.findViewById<TextView>(R.id.tvNama)
            .text = nama

        view.findViewById<TextView>(R.id.tvLevel)
            .text = "$tipe - $level"

        view.findViewById<TextView>(R.id.tvTime)
            .text = "$menit:${String.format("%02d", detik)}"

        view.findViewById<TextView>(R.id.tvWrong)
            .text = wrong.toString()

        view.findViewById<TextView>(R.id.tvBintang)
            .text = "⭐".repeat(star)

        val murid = getMurid()

        val imgAvatar =
            view.findViewById<ImageView>(R.id.imgAvatar)

        murid?.let {

            val avatar = it.avatar

            Glide.with(this)
                .load(
                    when {
                        avatar.startsWith("/") ->
                            File(avatar)

                        avatar.startsWith("content://") ->
                            android.net.Uri.parse(avatar)

                        avatar.isNotEmpty() ->
                            resources.getIdentifier(
                                avatar,
                                "drawable",
                                packageName
                            )

                        else ->
                            R.drawable.ic_avatar_default
                    }
                )
                .circleCrop()
                .placeholder(R.drawable.ic_avatar_default)
                .into(imgAvatar)
        }

        val predikat =
            when (star) {
                3 if wrong == 0 -> "🏆 Luar Biasa"
                3 -> "⭐ Sangat Baik"
                2 -> "👍 Baik"
                else -> "💪 Tetap Semangat"
            }

        val btnShare =
            view.findViewById<Button>(R.id.btnShare)

        view.findViewById<TextView>(R.id.tvPredikat)
            .text = predikat

        btnShare.setOnClickListener {

            btnShare.visibility = View.GONE

            sharePrestasi(
                level,
                star,
                time,
                wrong,
                predikat
            )

            btnShare.visibility = View.VISIBLE
        }

        dialog.show()
    }

    @SuppressLint("SetTextI18n", "DefaultLocale", "UseKtx", "DiscouragedApi", "InflateParams")
    private fun sharePrestasi(
        level: String,
        star: Int,
        time: Int,
        wrong: Int,
        predikat: String
    ) {

        val view = layoutInflater.inflate(
            R.layout.layout_share_prestasi,
            null
        )

        val murid = getMurid()

        view.findViewById<TextView>(R.id.tvNama)
            .text = nama

        view.findViewById<TextView>(R.id.tvLevel)
            .text = "$tipe - $level"

        view.findViewById<TextView>(R.id.tvBintang)
            .text = "⭐".repeat(star)

        val menit = time / 60
        val detik = time % 60

        view.findViewById<TextView>(R.id.tvTime)
            .text = "$menit:${String.format("%02d", detik)}"

        view.findViewById<TextView>(R.id.tvWrong)
            .text = "$wrong"

        view.findViewById<TextView>(R.id.tvPredikat)
            .text = predikat

        val tanggal =
            java.text.SimpleDateFormat(
                "dd MMMM yyyy",
                java.util.Locale("id")
            ).format(java.util.Date())

        view.findViewById<TextView>(R.id.tvTanggal)
            .text = tanggal

        val imgAvatar =
            view.findViewById<ImageView>(R.id.imgAvatar)

        murid?.let {

            val avatar = it.avatar

            Glide.with(this)
                .load(
                    when {
                        avatar.startsWith("/") ->
                            File(avatar)

                        avatar.startsWith("content://") ->
                            android.net.Uri.parse(avatar)

                        avatar.isNotEmpty() ->
                            resources.getIdentifier(
                                avatar,
                                "drawable",
                                packageName
                            )

                        else ->
                            R.drawable.ic_avatar_default
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

        val file = File(
            cacheDir,
            "prestasi.png"
        )

        val stream =
            FileOutputStream(file)

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

        startActivity(
            Intent.createChooser(
                intent,
                "Bagikan Prestasi"
            )
        )
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

}