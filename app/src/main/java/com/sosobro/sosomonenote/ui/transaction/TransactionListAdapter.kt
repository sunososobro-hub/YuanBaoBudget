package com.sosobro.sosomonenote.ui.transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sosobro.sosomonenote.database.TransactionEntity
import com.sosobro.sosomonenote.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.*

class TransactionListAdapter(
    private val onItemClick: (TransactionEntity) -> Unit
) : RecyclerView.Adapter<TransactionListAdapter.ViewHolder>() {

    private val items = mutableListOf<TransactionEntity>()

    fun submitList(list: List<TransactionEntity>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemTransactionBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tx = items[position]

        // ✨ 日期
        holder.binding.tvDate.text = formatDate(tx.time)

        // ✨ 標題（分類 + 商店）
        holder.binding.tvTitle.text =
            if (!tx.note.isNullOrBlank()) "${tx.category} - ${tx.note}"
            else tx.category

        // ✨ 帳戶名稱（暫時用 accountId，之後可查 AccountEntity）
        holder.binding.tvAccount.text = "帳戶 #${tx.accountId}"

        // ✨ 金額
        val formatted = "%,.0f".format(tx.amount)

        if (tx.type.contains("支出")) {
            holder.binding.tvAmount.text = "-$formatted"
            holder.binding.tvAmount.setTextColor(0xFFC62828.toInt()) // Red
        } else {
            holder.binding.tvAmount.text = "+$formatted"
            holder.binding.tvAmount.setTextColor(0xFF2E7D32.toInt()) // Green
        }

        holder.binding.root.setOnClickListener { onItemClick(tx) }
    }

    private fun formatDate(time: Long): String {
        val sdf = SimpleDateFormat("MM月 dd", Locale.TAIWAN)
        return sdf.format(Date(time))
    }
}
