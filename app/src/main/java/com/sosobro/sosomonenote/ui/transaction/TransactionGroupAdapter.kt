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
        holder.binding.tvDate.text = date
        holder.binding.containerTransactions.removeAllViews()

        transactions.forEachIndexed { index, txn ->
            val tv = TextView(holder.itemView.context).apply {
                text =
                    "${txn.category}：${if (txn.type.contains("支出")) "-" else "+"}NT$${"%,.0f".format(txn.amount)}"
                setTextColor(Color.parseColor("#4A3B2A"))
                textSize = 16f
                setPadding(8, 8, 8, 8)

                // ✅ 告訴 Fragment 有人點擊了這筆交易
                setOnClickListener { onTransactionClick(txn) }
            }

            holder.binding.containerTransactions.addView(tv)

            if (index < transactions.size - 1) {
                val divider = View(holder.itemView.context).apply {
                    setBackgroundColor(Color.parseColor("#DDDDDD"))
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        2
                    )
                }
                holder.binding.containerTransactions.addView(divider)
            }
        }
    }
}
