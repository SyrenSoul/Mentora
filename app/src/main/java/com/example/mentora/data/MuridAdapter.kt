package com.example.mentora.data

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mentora.R

class MuridAdapter(
    private val list: MutableList<Murid>,
    private val tipe: String,
    private val onDelete: (Int) -> Unit,
    private val onClick: (Murid) -> Unit
) : RecyclerView.Adapter<MuridAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nama = view.findViewById<TextView>(R.id.tvNama)!!
        val imgAvatar = view.findViewById<ImageView>(R.id.imgAvatar)!!
        val btnHapus = view.findViewById<ImageView>(R.id.btnHapus)!!
        val tvProgress = view.findViewById<TextView>(R.id.tvProgress)!!
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_murid, parent, false))

    override fun getItemCount() = list.size

    @SuppressLint("SetTextI18n", "UseKtx", "DiscouragedApi")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val murid = list[position]

        holder.nama.text = murid.nama

        val avatar = murid.avatar

        com.bumptech.glide.Glide.with(holder.itemView.context)
            .load(
                when {
                    avatar.startsWith("/") -> java.io.File(avatar)
                    avatar.startsWith("content://") -> android.net.Uri.parse(avatar)
                    avatar.isNotEmpty() -> holder.itemView.context.resources.getIdentifier(
                        avatar,
                        "drawable",
                        holder.itemView.context.packageName
                    )
                    else -> R.drawable.ic_avatar_default
                }
            )
            .circleCrop()
            .placeholder(R.drawable.ic_avatar_default)
            .into(holder.imgAvatar)

        when (tipe) {
            "HURUF" -> {
                holder.tvProgress.text = "Huruf (${murid.hurufProgress}/26)"
                holder.progressBar.max = 26
                holder.progressBar.progress = murid.hurufProgress
            }
            "ANGKA" -> {
                holder.tvProgress.text = "Angka (${murid.angkaProgress}/10)"
                holder.progressBar.max = 10
                holder.progressBar.progress = murid.angkaProgress
            }
            "WARNA" -> {
                holder.tvProgress.text = "Warna (${murid.warnaProgress}/11)"
                holder.progressBar.max = 11
                holder.progressBar.progress = murid.warnaProgress
            }
        }

        holder.itemView.setOnClickListener { it.clickAnimation { onClick(list[position]) } }
        holder.btnHapus.setOnClickListener { it.clickAnimation { onDelete(position) } }
    }

    fun View.clickAnimation(onEnd: () -> Unit = {}) {
        animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(80)
            .withEndAction {
                animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(80)
                    .withEndAction { onEnd() }
            }
    }
}