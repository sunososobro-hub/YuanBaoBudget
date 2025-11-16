package com.sosobro.sosomonenote.ui.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sosobro.sosomonenote.databinding.ItemTransactionBinding
import com.sosobro.sosomonenote.database.TransactionEntity
import java.text.SimpleDateFormat
import java.util.*

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

        // 日期（使用 time 欄位）
        holder.binding.tvDate.text = formatDate(tx.time)

        // 標題（分類 + 備註）
        holder.binding.tvTitle.text =
            if (!tx.note.isNullOrBlank()) "${tx.category} - ${tx.note}"
            else tx.category

        // 帳戶顯示（暫時僅用 accountId，若你有 AccountEntity 再補完整名稱）
        holder.binding.tvAccount.text = "帳戶 #${tx.accountId}"

        // 金額
        val formattedAmount = "%,.0f".format(tx.amount)

        if (tx.type.contains("支出")) {
            holder.binding.tvAmount.text = "-$formattedAmount"
            holder.binding.tvAmount.setTextColor(0xFFC62828.toInt())  // 紅色
        } else {
            holder.binding.tvAmount.text = "+$formattedAmount"
            holder.binding.tvAmount.setTextColor(0xFF2E7D32.toInt())  // 綠色
        }
    }

    override fun getItemCount() = transactions.size

    fun updateData(newList: List<TransactionEntity>) {
        transactions = newList
        notifyDataSetChanged()
    }

    // 時間格式轉換
    private fun formatDate(time: Long): String {
        val sdf = SimpleDateFormat("MM月 dd日", Locale.TAIWAN)
        return sdf.format(Date(time))
    }
}
