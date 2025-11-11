package com.sosobro.sosomonenote.ui.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sosobro.sosomonenote.database.AccountEntity
import com.sosobro.sosomonenote.databinding.ItemAccountCardBinding

class AccountAdapter(
    private var accounts: MutableList<AccountEntity>,
    private val onItemClick: (AccountEntity) -> Unit
) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAccountCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(account: AccountEntity) {
            binding.tvAccountName.text = account.name
            binding.tvAmount.text = "NT$${String.format("%,.0f", account.balance)}"
            binding.root.setOnClickListener { onItemClick(account) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAccountCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(accounts[position])
    }

    override fun getItemCount() = accounts.size

    fun updateData(newList: List<AccountEntity>) {
        accounts = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun moveItem(from: Int, to: Int) {
        val moved = accounts.removeAt(from)
        accounts.add(to, moved)
        notifyItemMoved(from, to)
    }

    fun getCurrentList(): List<AccountEntity> = accounts
}
