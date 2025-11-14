package com.sosobro.sosomonenote.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sosobro.sosomonenote.R
import com.sosobro.sosomonenote.database.CategoryEntity
import com.sosobro.sosomonenote.databinding.ItemCategoryBinding

class CategoryPickerAdapter(
    private val onClick: (CategoryEntity) -> Unit
) : RecyclerView.Adapter<CategoryPickerAdapter.VH>() {

    private var list = emptyList<CategoryEntity>()

    fun submitList(newList: List<CategoryEntity>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]

        // 安全處理 icon，避免 null crash
        val iconRes = item.iconRes ?: R.drawable.ic_drop_down

        holder.binding.ivIcon.setImageResource(iconRes)
        holder.binding.tvCategoryName.text = item.name

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = list.size
}
