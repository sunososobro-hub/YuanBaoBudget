package com.sosobro.sosomonenote.ui.transaction

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sosobro.sosomonenote.database.TransactionEntity
import com.sosobro.sosomonenote.databinding.ItemTransactionGroupBinding

class TransactionGroupAdapter(
    private val onTransactionClick: (TransactionEntity) -> Unit
) : RecyclerView.Adapter<TransactionGroupAdapter.GroupViewHolder>() {

    private val data = mutableListOf<Pair<String, List<TransactionEntity>>>()

    fun submitData(newData: List<Pair<String, List<TransactionEntity>>>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    inner class GroupViewHolder(val binding: ItemTransactionGroupBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemTransactionGroupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GroupViewHolder(binding)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val (date, transactions) = data[position]

        // 日期標題
        holder.binding.tvDate.text = date

        // 清空舊資料
        holder.binding.containerTransactions.removeAllViews()

        transactions.forEachIndexed { index, txn ->
            val itemView = LayoutInflater
                .from(holder.itemView.context)
                .inflate(
                    com.sosobro.sosomonenote.R.layout.item_transaction_row,
                    holder.binding.containerTransactions,
                    false
                )

            val tvTitle = itemView.findViewById<TextView>(com.sosobro.sosomonenote.R.id.tvTitle)
            val tvAccount = itemView.findViewById<TextView>(com.sosobro.sosomonenote.R.id.tvAccount)
            val tvAmount = itemView.findViewById<TextView>(com.sosobro.sosomonenote.R.id.tvAmount)

            tvTitle.text = txn.category
            tvAccount.text = txn.note ?: ""

            val isExpense = txn.type.contains("支出")
            val amountText = (if (isExpense) "-" else "+") + "NT$" +
                    String.format("%,.0f", txn.amount)

            tvAmount.text = amountText
            tvAmount.setTextColor(if (isExpense) Color.parseColor("#C62828") else Color.parseColor("#2E7D32"))

            itemView.setOnClickListener { onTransactionClick(txn) }

            holder.binding.containerTransactions.addView(itemView)

            if (index < transactions.size - 1) {
                val divider = View(holder.itemView.context).apply {
                    setBackgroundColor(Color.parseColor("#DDDDDD"))
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1
                    )
                }
                holder.binding.containerTransactions.addView(divider)
            }
        }
    }
}
