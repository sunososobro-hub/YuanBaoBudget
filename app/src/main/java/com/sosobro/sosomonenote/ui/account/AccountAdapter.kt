package com.sosobro.sosomonenote.ui.account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sosobro.sosomonenote.R
import com.sosobro.sosomonenote.database.AccountEntity
import com.sosobro.sosomonenote.databinding.ItemAccountCardBinding

class AccountAdapter(
    private val onItemClick: (AccountEntity) -> Unit,
    private val onDataChanged: (List<AccountEntity>) -> Unit,
    private val onExpandedChanged: (Map<String, Boolean>) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val allData = linkedMapOf<String, MutableList<AccountEntity>>()
    private val expandedMap = mutableMapOf<String, Boolean>()
    private val items = mutableListOf<ListItem>()

    sealed class ListItem {
        data class Header(val title: String, val expanded: Boolean) : ListItem()
        data class Account(val account: AccountEntity, val category: String) : ListItem()
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ACCOUNT = 1
    }

    fun setData(grouped: Map<String, List<AccountEntity>>) {
        allData.clear()
        allData.putAll(grouped.mapValues { it.value.toMutableList() })
        grouped.keys.forEach { key ->
            if (!expandedMap.containsKey(key)) expandedMap[key] = true
        }
        rebuildVisibleList()
    }

    private fun rebuildVisibleList() {
        items.clear()
        allData.forEach { (category, list) ->
            items.add(ListItem.Header(category, expandedMap[category] == true))
            if (expandedMap[category] == true) {
                list.forEach { items.add(ListItem.Account(it, category)) }
            }
        }
        notifyDataSetChanged()
        onDataChanged(getCurrentList())
    }

    fun moveItem(from: Int, to: Int) {
        if (from !in items.indices || to !in items.indices) return
        val moved = items[from]
        if (moved !is ListItem.Account) return

        items.removeAt(from)
        items.add(to, moved)

        var newCategory = moved.category
        for (i in to downTo 0) {
            val item = items[i]
            if (item is ListItem.Header) {
                newCategory = item.title
                break
            }
        }

        val oldList = allData[moved.category]!!
        oldList.remove(moved.account)

        val newList = allData[newCategory]!!
        newList.add(moved.account)

        moved.account.type = newCategory

        allData.forEach { (_, list) ->
            list.forEachIndexed { index, acc ->
                acc.sortOrder = index
            }
        }

        notifyItemMoved(from, to)

        onDataChanged(getCurrentList())
    }

    fun getCurrentList(): List<AccountEntity> =
        allData.values.flatten()

    override fun getItemViewType(position: Int): Int =
        when (items[position]) {
            is ListItem.Header -> TYPE_HEADER
            is ListItem.Account -> TYPE_ACCOUNT
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_account_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val binding = ItemAccountCardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            AccountViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is ListItem.Account -> (holder as AccountViewHolder).bind(item.account)
        }
    }

    override fun getItemCount() = items.size

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvHeader = view.findViewById<TextView>(R.id.tvHeaderTitle)
        private val ivToggle = view.findViewById<ImageView>(R.id.ivHeaderToggle)

        fun bind(header: ListItem.Header) {
            tvHeader.text = header.title
            ivToggle.rotation = if (header.expanded) 0f else -90f

            itemView.setOnClickListener {
                val now = expandedMap[header.title] ?: true
                expandedMap[header.title] = !now
                rebuildVisibleList()
                onExpandedChanged(expandedMap)
            }
        }
    }

    inner class AccountViewHolder(private val binding: ItemAccountCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(account: AccountEntity) {
            binding.tvAccountName.text = account.name
            binding.tvAmount.text = "NT$${String.format("%,.0f", account.balance)}"
            binding.root.setOnClickListener { onItemClick(account) }
        }
    }

    fun getExpandedState(): Map<String, Boolean> = expandedMap

    fun restoreExpandedState(map: Map<String, Boolean>) {
        expandedMap.clear()
        expandedMap.putAll(map)
        rebuildVisibleList()
    }
}
