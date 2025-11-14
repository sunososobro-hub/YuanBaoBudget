package com.sosobro.sosomonenote.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sosobro.sosomonenote.databinding.ItemIconBinding

class IconAdapter(
    private var icons: List<Int>,
    private val onIconClick: (Int) -> Unit
) : RecyclerView.Adapter<IconAdapter.ViewHolder>() {

    private var selectedPos = -1

    fun submitList(newList: List<Int>) {
        icons = newList
        selectedPos = -1
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemIconBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIconBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val iconRes = icons[position]
        holder.binding.ivIcon.setImageResource(iconRes)

        // ✅ 選中效果
        holder.binding.root.alpha = if (selectedPos == position) 1f else 0.5f

        holder.binding.root.setOnClickListener {
            selectedPos = position
            notifyDataSetChanged()
            onIconClick(iconRes)
        }
    }

    override fun getItemCount(): Int = icons.size
}
