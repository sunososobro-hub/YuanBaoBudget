package com.sosobro.sosomonenote.ui.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sosobro.sosomonenote.databinding.ItemTransactionBinding
import com.sosobro.sosomonenote.database.TransactionEntity

class TransactionAdapter(private var transactions: List<TransactionEntity>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tx = transactions[position]
        holder.binding.tvCategory.text = tx.category
        holder.binding.tvNote.text = tx.note
        holder.binding.tvAmount.text = if (tx.amount >= 0)
            "NT$${String.format("%,.0f", tx.amount)}"
        else
            "-NT$${String.format("%,.0f", -tx.amount)}"
    }

    override fun getItemCount() = transactions.size

    fun updateData(newList: List<TransactionEntity>) {
        transactions = newList
        notifyDataSetChanged()
    }
}
