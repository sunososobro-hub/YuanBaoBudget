package com.sosobro.sosomonenote.ui.transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sosobro.sosomonenote.database.TransactionEntity
import com.sosobro.sosomonenote.databinding.ItemTransactionBinding

class TransactionListAdapter(private val transactions: List<TransactionEntity>) :
    RecyclerView.Adapter<TransactionListAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val t = transactions[position]
        holder.binding.tvCategory.text = t.category
        holder.binding.tvAmount.text = "NT$${"%,.0f".format(t.amount)}"
    }

    override fun getItemCount() = transactions.size
}
