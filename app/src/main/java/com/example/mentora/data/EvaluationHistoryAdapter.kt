package com.example.mentora.data

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mentora.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION")
class EvaluationHistoryAdapter(
    private val list: List<EvaluationHistory>,
    private val onClick: (EvaluationHistory) -> Unit
) : RecyclerView.Adapter<EvaluationHistoryAdapter.ViewHolder>() {

    class ViewHolder(val card: CardView) : RecyclerView.ViewHolder(card)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_evaluation_history, parent, false) as CardView
        )

    override fun getItemCount() = list.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        val tanggal = SimpleDateFormat("d MMM yyyy, HH:mm", Locale("id", "ID"))
            .format(Date(item.tanggal))

        holder.card.findViewById<TextView>(R.id.tvMateri).text = "Tes ${item.materi}"
        holder.card.findViewById<TextView>(R.id.tvTanggal).text = tanggal
        holder.card.findViewById<TextView>(R.id.tvNilai).text = "Nilai ${item.score}"
        holder.card.findViewById<TextView>(R.id.tvStar).text = "⭐".repeat(item.bintang)

        holder.card.setOnClickListener { onClick(item) }
    }
}