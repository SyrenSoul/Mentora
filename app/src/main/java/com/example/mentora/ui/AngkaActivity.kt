package com.example.mentora.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mentora.R
import com.example.mentora.data.Murid
import com.example.mentora.data.MuridAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Suppress("DEPRECATION")
class AngkaActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MuridAdapter
    private val listMurid = mutableListOf<Murid>()
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private var selectedAvatar: String = ""
    private var imgPreview: ImageView? = null
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                val path = saveImageToInternalStorage(it)
                selectedAvatar = path
                Glide.with(this)
                    .load(java.io.File(path))
                    .circleCrop()
                    .into(imgPreview!!)
            }
        }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_angka)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.title = "Angka"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sharedPreferences = getSharedPreferences("DATA_MURID_ANGKA", MODE_PRIVATE)

        loadData()

        val btnTambah = findViewById<CardView>(R.id.btnTambah)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = MuridAdapter(
            listMurid,
            "ANGKA",
            onDelete = { posisi ->

                val view = layoutInflater.inflate(R.layout.dialog_hapus_murid, null)

                val tvPesan = view.findViewById<TextView>(R.id.tvPesan)
                val btnKonfirmasiHapus = view.findViewById<Button>(R.id.btnKonfirmasiHapus)
                val btnBatal = view.findViewById<Button>(R.id.btnBatal)

                val nama = listMurid[posisi].nama
                tvPesan.text = "Hapus \"$nama\"?"

                val dialog = AlertDialog.Builder(this)
                    .setView(view)
                    .create()

                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

                btnKonfirmasiHapus.setOnClickListener {
                    it.clickAnimation {
                        listMurid.removeAt(posisi)
                        adapter.notifyDataSetChanged()
                        Thread {
                            saveData()
                        }.start()
                        dialog.dismiss()
                    }
                }

                btnBatal.setOnClickListener {
                    it.clickAnimation {
                        dialog.dismiss()
                    }
                }

                dialog.show()
            },

            onClick = { murid ->

                murid.lastPlayed = System.currentTimeMillis()
                listMurid.sortByDescending { it.lastPlayed }
                adapter.notifyDataSetChanged()
                Thread {
                    saveData()
                }.start()

                val intent = Intent(this, LevelActivity::class.java)
                intent.putExtra("ID_MURID", murid.id)
                intent.putExtra("NAMA_MURID", murid.nama)
                intent.putExtra("TIPE", "ANGKA")

                showLoading()
                Handler(Looper.getMainLooper()).postDelayed({

                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

                    hideLoading()

                }, 350)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnTambah.setOnClickListener {
            it.clickAnimation {
                showDialogTambah()
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            val intent = Intent(this@AngkaActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    @SuppressLint("NotifyDataSetChanged", "DiscouragedApi", "UseKtx")
    private fun showDialogTambah() {
        imgPreview?.setImageResource(R.drawable.ic_avatar_default)
        selectedAvatar = ""
        val view = layoutInflater.inflate(R.layout.dialog_tambah_murid, null)

        val etNama = view.findViewById<EditText>(R.id.etNama)
        val gridAvatar = view.findViewById<GridLayout>(R.id.gridAvatar)
        imgPreview = view.findViewById(R.id.imgPreview)
        val btnGallery = view.findViewById<ImageView>(R.id.btnGallery)
        val btnSimpan = view.findViewById<Button>(R.id.btnSimpan)
        val btnBatal = view.findViewById<Button>(R.id.btnBatal)
        val avatarList = listOf(
            "avatar_1", "avatar_2", "avatar_3",
            "avatar_4", "avatar_5", "avatar_6"
        )

        avatarList.forEach { avatarName ->

            val img = ImageView(this)

            val imgParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            img.layoutParams = imgParams
            img.scaleType = ImageView.ScaleType.CENTER_CROP

            val resId = resources.getIdentifier(avatarName, "drawable", packageName)
            img.setImageResource(resId)

            val card = CardView(this)

            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = 180
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.setMargins(8, 12, 8, 12)

            card.layoutParams = params
            card.radius = 100f
            card.setCardBackgroundColor(android.graphics.Color.WHITE)
            card.cardElevation = 0f
            card.setContentPadding(6, 6, 6, 6)

            card.addView(img)

            card.setOnClickListener {

                for (i in 0 until gridAvatar.childCount) {
                    val child = gridAvatar.getChildAt(i) as CardView
                    child.setCardBackgroundColor(android.graphics.Color.WHITE)
                }

                card.setCardBackgroundColor(android.graphics.Color.parseColor("#2196F3"))

                selectedAvatar = avatarName

                Glide.with(this)
                    .load(resId)
                    .circleCrop()
                    .into(imgPreview!!)
            }

            gridAvatar.addView(card)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnGallery.setOnClickListener {
            it.clickAnimation {
                pickImage.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        }

        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString().trim()

            if (nama.isEmpty()) {
                etNama.error = "Masukkan nama terlebih dahulu"
                return@setOnClickListener
            }

            it.clickAnimation {
                val finalAvatar = selectedAvatar.ifEmpty {
                    avatarList.random()
                }

                val muridBaru = Murid(
                    nama = nama,
                    avatar = finalAvatar,
                    lastPlayed = System.currentTimeMillis()
                )

                listMurid.add(muridBaru)
                listMurid.sortByDescending { it.lastPlayed }

                adapter.notifyDataSetChanged()
                Thread {
                    saveData()
                }.start()
                dialog.dismiss()
            }
        }

        btnBatal.setOnClickListener {
            it.clickAnimation {
                dialog.dismiss()
            }
        }

        dialog.show()
        dialog.setOnDismissListener {
            imgPreview = null
        }
    }

    @SuppressLint("UseKtx")
    private fun saveData() {
        val editor = sharedPreferences.edit()
        val json = gson.toJson(listMurid)
        editor.putString("LIST_MURID", json)
        editor.apply()
    }

    private fun loadData() {
        val json = sharedPreferences.getString("LIST_MURID", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<Murid>>() {}.type
            val data: MutableList<Murid> = gson.fromJson(json, type)

            listMurid.clear()
            listMurid.addAll(data)

            listMurid.sortByDescending { it.lastPlayed }
        }
    }

    private fun saveImageToInternalStorage(uri: android.net.Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val fileName = "avatar_${System.currentTimeMillis()}.jpg"
        val file = java.io.File(filesDir, fileName)

        val outputStream = java.io.FileOutputStream(file)

        inputStream?.copyTo(outputStream)

        inputStream?.close()
        outputStream.close()

        return file.absolutePath
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onPause() {
        super.onPause()
        Thread {
            saveData()
        }.start()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        Thread {

            loadData()

            listMurid.sortByDescending { it.lastPlayed }

            runOnUiThread {
                adapter.notifyDataSetChanged()
            }

        }.start()
    }

    private fun showLoading() {
        findViewById<View>(R.id.loadingOverlay).visibility = View.VISIBLE
    }

    private fun hideLoading() {
        findViewById<View>(R.id.loadingOverlay).visibility = View.GONE
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