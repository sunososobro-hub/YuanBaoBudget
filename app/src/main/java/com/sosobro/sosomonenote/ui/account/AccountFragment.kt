package com.sosobro.sosomonenote.ui.account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sosobro.sosomonenote.database.AccountEntity
import com.sosobro.sosomonenote.database.DatabaseInstance
import com.sosobro.sosomonenote.databinding.FragmentAccountBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AccountAdapter
    private val prefName = "account_fragment_state"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        adapter = AccountAdapter(
            onItemClick = { openAccountDetail(it) },
            onDataChanged = { list -> saveAccountOrder(list) },
            onExpandedChanged = { state -> saveExpandedState(state) }
        )

        binding.recyclerViewAccounts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewAccounts.adapter = adapter

        setupDragAndDrop()
        loadAccounts(restoreExpanded = true)
    }

    private fun openAccountDetail(account: AccountEntity) {
        val intent = Intent(requireContext(), AccountDetailActivity::class.java)
        intent.putExtra("accountId", account.id)
        startActivity(intent)
    }

    private fun setupDragAndDrop() {
        val callback = object :
            ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

            override fun onMove(
                rv: RecyclerView, vh: RecyclerView.ViewHolder, tgt: RecyclerView.ViewHolder
            ): Boolean {
                adapter.moveItem(vh.adapterPosition, tgt.adapterPosition)
                return true
            }

            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {}

            override fun isLongPressDragEnabled() = true

            override fun clearView(rv: RecyclerView, vh: RecyclerView.ViewHolder) {
                super.clearView(rv, vh)
                saveAccountOrder(adapter.getCurrentList())
            }
        }

        ItemTouchHelper(callback).attachToRecyclerView(binding.recyclerViewAccounts)
    }

    private fun saveAccountOrder(list: List<AccountEntity>) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = DatabaseInstance.getDatabase(requireContext()).accountDao()

            list.forEachIndexed { index, acc ->
                acc.sortOrder = index
                dao.update(acc)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateAccountBalances()
    }

    private fun updateAccountBalances() {
        lifecycleScope.launch {
            val db = DatabaseInstance.getDatabase(requireContext())
            val transactionDao = db.transactionDao()
            val accountDao = db.accountDao()
            val all = withContext(Dispatchers.IO) { accountDao.getAllAccountsSorted() }

            all.forEach {
                val total =
                    withContext(Dispatchers.IO) { transactionDao.getTotalAmountByAccountId(it.id) }
                it.balance = total ?: 0.0
                withContext(Dispatchers.IO) { accountDao.update(it) }
            }
        }
    }

    private fun loadAccounts(restoreExpanded: Boolean) {
        lifecycleScope.launch {

            val db = DatabaseInstance.getDatabase(requireContext())
            val accountDao = db.accountDao()

            val all = withContext(Dispatchers.IO) { accountDao.getAllAccountsSorted() }

            val categoryOrder = listOf(
                "現金帳戶",
                "銀行帳戶",
                "電子錢包",
                "信用卡帳戶",
                "投資帳戶",
                "虛擬帳戶",
                "其他帳戶"
            )

            val grouped = linkedMapOf<String, MutableList<AccountEntity>>()
            categoryOrder.forEach { grouped[it] = mutableListOf() }

            all.forEach { acc ->
                val key = categoryOrder.firstOrNull { acc.type.contains(it) } ?: "其他帳戶"
                grouped[key]!!.add(acc)
            }

            // 每組內依 sortOrder 排序
            grouped.keys.forEach { key ->
                grouped[key] = grouped[key]!!.sortedBy { it.sortOrder }.toMutableList()
            }

            adapter.setData(grouped)

            if (restoreExpanded) {
                val prefs =
                    requireContext().getSharedPreferences(prefName, Context.MODE_PRIVATE)
                val map = mutableMapOf<String, Boolean>()
                categoryOrder.forEach { key ->
                    map[key] = prefs.getBoolean("expanded_$key", true)
                }
                adapter.restoreExpandedState(map)
            }
        }
    }

    private fun saveExpandedState(state: Map<String, Boolean>) {
        val prefs =
            requireContext().getSharedPreferences(prefName, Context.MODE_PRIVATE).edit()
        state.forEach { (key, expanded) ->
            prefs.putBoolean("expanded_$key", expanded)
        }
        prefs.apply()
    }

    override fun onPause() {
        super.onPause()
        saveExpandedState(adapter.getExpandedState())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
