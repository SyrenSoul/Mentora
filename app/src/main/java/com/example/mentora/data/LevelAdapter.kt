package com.example.mentora.data

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mentora.R

class LevelAdapter(
    private val list: List<String>,
    private val isUnlocked: (Int) -> Boolean,
    private val getStar: (String) -> Int,
    private val getTime: (String) -> Int,
    private val onTimeClick: (String) -> Unit,
    private val onLockedClick: () -> Unit,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<LevelAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgLevel = view.findViewById<ImageView>(R.id.imgLevel)!!
        val iconLock = view.findViewById<ImageView>(R.id.iconLock)!!
        val star1 = view.findViewById<ImageView>(R.id.star1)!!
        val star2 = view.findViewById<ImageView>(R.id.star2)!!
        val star3 = view.findViewById<ImageView>(R.id.star3)!!
        val tvTime = view.findViewById<TextView>(R.id.tvTime)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_level, parent, false))

    override fun getItemCount() = list.size

    @SuppressLint("DiscouragedApi", "SetTextI18n", "DefaultLocale")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val context = holder.itemView.context

        val namaDrawable = "ic_" + item.lowercase().replace(" ", "_").replace("-", "_")

        val resId = context.resources.getIdentifier(
            namaDrawable,
            "drawable",
            context.packageName
        )

        holder.imgLevel.setImageResource(if (resId != 0) resId else R.drawable.ic_default)

        val star = getStar(item)
        val time = getTime(item)

        holder.star1.visibility = if (star >= 1) View.VISIBLE else View.INVISIBLE
        holder.star2.visibility = if (star >= 2) View.VISIBLE else View.INVISIBLE
        holder.star3.visibility = if (star >= 3) View.VISIBLE else View.INVISIBLE

        if (time > 0) {
            val menit = time / 60
            val detik = time % 60
            holder.tvTime.text = "⏱\uFE0F ${menit}:${String.format("%02d", detik)}"
            holder.tvTime.setOnClickListener { onTimeClick(item) }
            holder.tvTime.visibility = View.VISIBLE
        } else {
            holder.tvTime.visibility = View.GONE
            holder.tvTime.setOnClickListener(null)
        }

        if (!isUnlocked(position)) {
            holder.iconLock.visibility = View.VISIBLE
            holder.itemView.alpha = 0.5f
            holder.itemView.setOnClickListener { it.clickAnimation { onLockedClick() } }
        } else {
            holder.iconLock.visibility = View.GONE
            holder.itemView.alpha = 1f
            holder.itemView.setOnClickListener { it.clickAnimation { onClick(item) } }
        }
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