package com.sosobro.sosomonenote.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sosobro.sosomonenote.R
import com.sosobro.sosomonenote.database.CategoryEntity
import com.sosobro.sosomonenote.databinding.ItemCategoryBinding

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var list = listOf<CategoryEntity>()

    fun submitList(newList: List<CategoryEntity>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.tvCategoryName.text = item.name
        holder.binding.ivIcon.setImageResource(R.drawable.ic_drop_down)
    }

    override fun getItemCount(): Int = list.size
}
