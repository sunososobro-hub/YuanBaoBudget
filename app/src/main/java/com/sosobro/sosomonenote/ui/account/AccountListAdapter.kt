package com.sosobro.sosomonenote.ui.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sosobro.sosomonenote.databinding.ItemAccountCardBinding
import com.sosobro.sosomonenote.databinding.ItemAccountHeaderBinding
import com.sosobro.sosomonenote.database.AccountEntity

class AccountListAdapter(
    val items: MutableList<AccountListItem>,
    private val onItemClick: (AccountEntity) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int = items[position].type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == AccountListItem.TYPE_HEADER) {
            val binding = ItemAccountHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            HeaderViewHolder(binding)
        } else {
            val binding = ItemAccountCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            AccountViewHolder(binding)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is HeaderViewHolder) holder.bind(item.title ?: "")
        if (holder is AccountViewHolder) item.account?.let { holder.bind(it) }
    }

    // 🔹 Header
    inner class HeaderViewHolder(private val binding: ItemAccountHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.tvHeaderTitle.text = title
        }
    }

    // 🔹 Account
    inner class AccountViewHolder(private val binding: ItemAccountCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(account: AccountEntity) {
            binding.tvAccountName.text = account.name
            binding.tvAmount.text = "NT$${String.format("%,.0f", account.balance)}"
            binding.root.setOnClickListener { onItemClick(account) }
        }
    }

    // 🔹 拖曳排序 / 更新資料
    fun moveItem(from: Int, to: Int) {
        val fromItem = items.removeAt(from)
        items.add(to, fromItem)
        notifyItemMoved(from, to)

        // 如果跨分類，更新帳戶 type
        if (fromItem.type == AccountListItem.TYPE_ACCOUNT) {
            val newSection = findNearestHeaderAbove(to)
            fromItem.account?.type = newSection
        }
    }

    private fun findNearestHeaderAbove(pos: Int): String {
        for (i in pos downTo 0) {
            val item = items[i]
            if (item.type == AccountListItem.TYPE_HEADER) return item.title ?: "其他帳戶"
        }
        return "其他帳戶"
    }

    fun updateData(newItems: List<AccountListItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
