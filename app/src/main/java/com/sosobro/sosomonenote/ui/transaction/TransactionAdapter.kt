package com.sosobro.sosomonenote.ui.transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sosobro.sosomonenote.databinding.ItemTransactionBinding
import com.sosobro.sosomonenote.database.TransactionEntity

class TransactionAdapter(
    private val onClick: (TransactionEntity) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    private var list: List<TransactionEntity> = emptyList()

    inner class ViewHolder(val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val txn = list[position]

        holder.binding.tvDate.text = txn.date.substring(5)
        holder.binding.tvTitle.text = txn.category + (txn.note?.let { " - $it" } ?: "")
        holder.binding.tvAccount.text = txn.book ?: ""

        holder.binding.tvAmount.text = (if (txn.type.contains("支出")) "-NT$" else "+NT$") +
                String.format("%,.0f", txn.amount)

        holder.binding.tvAmount.setTextColor(
            if (txn.type.contains("支出"))
                0xFFC62828.toInt()
            else
                0xFF2E7D32.toInt()
        )

        holder.itemView.setOnClickListener { onClick(txn) }
    }

    fun updateData(newList: List<TransactionEntity>) {
        list = newList
        notifyDataSetChanged()
    }
}
