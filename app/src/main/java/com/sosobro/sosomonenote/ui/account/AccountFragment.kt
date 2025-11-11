package com.sosobro.sosomonenote.ui.account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sosobro.sosomonenote.R
import com.sosobro.sosomonenote.database.AccountEntity
import com.sosobro.sosomonenote.database.DatabaseInstance
import com.sosobro.sosomonenote.databinding.FragmentAccountBinding
import com.sosobro.sosomonenote.ui.addaccount.AddAccountActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private data class AccountSection(
        val title: String,
        val headerLayout: LinearLayout,
        val recyclerView: RecyclerView,
        val toggleIcon: ImageView,
        val adapter: AccountAdapter,
        var expanded: Boolean = true
    )

    private lateinit var sections: List<AccountSection>

    // 各分類 Adapter
    private lateinit var cashAdapter: AccountAdapter
    private lateinit var bankAdapter: AccountAdapter
    private lateinit var ewalletAdapter: AccountAdapter
    private lateinit var creditAdapter: AccountAdapter
    private lateinit var investAdapter: AccountAdapter
    private lateinit var virtualAdapter: AccountAdapter
    private lateinit var otherAdapter: AccountAdapter

    companion object {
        private const val REQUEST_ADD_ACCOUNT = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdaptersAndSections()
        setupToggles()
        loadAccounts()
    }

    private fun setupAdaptersAndSections() {
        // 初始化 Adapter（點擊可進入帳戶詳情）
        cashAdapter = AccountAdapter(mutableListOf()) { openAccountDetail(it) }
        bankAdapter = AccountAdapter(mutableListOf()) { openAccountDetail(it) }
        ewalletAdapter = AccountAdapter(mutableListOf()) { openAccountDetail(it) }
        creditAdapter = AccountAdapter(mutableListOf()) { openAccountDetail(it) }
        investAdapter = AccountAdapter(mutableListOf()) { openAccountDetail(it) }
        virtualAdapter = AccountAdapter(mutableListOf()) { openAccountDetail(it) }
        otherAdapter = AccountAdapter(mutableListOf()) { openAccountDetail(it) }

        // 綁定對應的 layout & recycler
        sections = listOf(
            AccountSection(
                "現金帳戶",
                binding.sectionCash.root.findViewById(R.id.layoutHeader),
                binding.sectionCash.root.findViewById(R.id.recyclerView),
                binding.sectionCash.root.findViewById(R.id.ivToggle),
                cashAdapter
            ),
            AccountSection(
                "銀行帳戶",
                binding.sectionBank.root.findViewById(R.id.layoutHeader),
                binding.sectionBank.root.findViewById(R.id.recyclerView),
                binding.sectionBank.root.findViewById(R.id.ivToggle),
                bankAdapter
            ),
            AccountSection(
                "電子錢包",
                binding.sectionEwallet.root.findViewById(R.id.layoutHeader),
                binding.sectionEwallet.root.findViewById(R.id.recyclerView),
                binding.sectionEwallet.root.findViewById(R.id.ivToggle),
                ewalletAdapter
            ),
            AccountSection(
                "信用卡帳戶",
                binding.sectionCredit.root.findViewById(R.id.layoutHeader),
                binding.sectionCredit.root.findViewById(R.id.recyclerView),
                binding.sectionCredit.root.findViewById(R.id.ivToggle),
                creditAdapter
            ),
            AccountSection(
                "投資帳戶",
                binding.sectionInvest.root.findViewById(R.id.layoutHeader),
                binding.sectionInvest.root.findViewById(R.id.recyclerView),
                binding.sectionInvest.root.findViewById(R.id.ivToggle),
                investAdapter
            ),
            AccountSection(
                "虛擬帳戶",
                binding.sectionVirtual.root.findViewById(R.id.layoutHeader),
                binding.sectionVirtual.root.findViewById(R.id.recyclerView),
                binding.sectionVirtual.root.findViewById(R.id.ivToggle),
                virtualAdapter
            ),
            AccountSection(
                "其他帳戶",
                binding.sectionOther.root.findViewById(R.id.layoutHeader),
                binding.sectionOther.root.findViewById(R.id.recyclerView),
                binding.sectionOther.root.findViewById(R.id.ivToggle),
                otherAdapter
            )
        )

        // 初始化 RecyclerView
        sections.forEach { section ->
            section.recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = section.adapter
            }
            setupDragAndDrop(section.recyclerView, section.adapter, section.title)
        }
    }

    private fun setupToggles() {
        sections.forEach { section ->
            val titleView = section.headerLayout.findViewById<TextView>(R.id.tvTitle)
            titleView.text = section.title

            section.headerLayout.setOnClickListener {
                section.expanded = !section.expanded
                section.recyclerView.visibility = if (section.expanded) View.VISIBLE else View.GONE
                section.toggleIcon.rotation = if (section.expanded) 0f else -90f
            }
        }
    }

    private fun openAccountDetail(account: AccountEntity) {
        val intent = Intent(requireContext(), AccountDetailActivity::class.java)
        intent.putExtra("accountId", account.id)
        intent.putExtra("accountName", account.name)
        startActivity(intent)
    }

    private fun setupDragAndDrop(recyclerView: RecyclerView, adapter: AccountAdapter, type: String) {
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val from = vh.adapterPosition
                val to = target.adapterPosition
                adapter.moveItem(from, to)
                return true
            }

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {}
            override fun isLongPressDragEnabled(): Boolean = true

            override fun clearView(rv: RecyclerView, vh: RecyclerView.ViewHolder) {
                super.clearView(rv, vh)
                saveAccountOrder(adapter.getCurrentList(), type)
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }

    private fun saveAccountOrder(list: List<AccountEntity>, type: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = DatabaseInstance.getDatabase(requireContext())
            val dao = db.accountDao()
            list.forEachIndexed { index, acc ->
                acc.sortOrder = index
                dao.update(acc)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadAccounts()
    }

    private fun loadAccounts() {

        lifecycleScope.launch {
            val db = DatabaseInstance.getDatabase(requireContext())
            val accountDao = db.accountDao()
            val transactionDao = db.transactionDao()

            val allAccounts = withContext(Dispatchers.IO) { accountDao.getAllAccountsSorted() }

            val cashList = mutableListOf<AccountEntity>()
            val bankList = mutableListOf<AccountEntity>()
            val ewalletList = mutableListOf<AccountEntity>()
            val creditList = mutableListOf<AccountEntity>()
            val investList = mutableListOf<AccountEntity>()
            val virtualList = mutableListOf<AccountEntity>()
            val otherList = mutableListOf<AccountEntity>()

            var totalAssets = 0.0
            var totalDebt = 0.0

            for (account in allAccounts) {
                val totalAmount = withContext(Dispatchers.IO) {
                    transactionDao.getTotalAmountByAccountId(account.id)
                }
                account.balance = totalAmount ?: 0.0
                withContext(Dispatchers.IO) { accountDao.update(account) }

                when {
                    account.type.contains("現金") -> cashList.add(account)
                    account.type.contains("銀行") || account.type.contains("儲蓄") -> bankList.add(account)
                    account.type.contains("電子") || account.type.contains("Pay") -> ewalletList.add(account)
                    account.type.contains("信用") || account.type.contains("卡") -> creditList.add(account)
                    account.type.contains("投資") || account.type.contains("股票") -> investList.add(account)
                    account.type.contains("虛擬") || account.type.contains("加密") -> virtualList.add(account)
                    else -> otherList.add(account)
                }

                val allAccounts = withContext(Dispatchers.IO) { accountDao.getAllAccountsSorted() }
                Log.d("AccountCheck", "總筆數=${allAccounts.size}")
                allAccounts.forEachIndexed { index, acc ->
                    Log.d("AccountCheck", "第${index + 1}筆：${acc.id}｜${acc.name}｜${acc.type}")
                }


                if (account.balance >= 0) totalAssets += account.balance else totalDebt += account.balance
            }

            binding.tvAssets.text = "NT$${String.format("%,.0f", totalAssets)}"
            binding.tvDebt.text = "NT$${String.format("%,.0f", totalDebt)}"

            cashAdapter.updateData(cashList)
            bankAdapter.updateData(bankList)
            ewalletAdapter.updateData(ewalletList)
            creditAdapter.updateData(creditList)
            investAdapter.updateData(investList)
            virtualAdapter.updateData(virtualList)
            otherAdapter.updateData(otherList)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_account, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_account -> {
                val intent = Intent(requireContext(), AddAccountActivity::class.java)
                startActivityForResult(intent, REQUEST_ADD_ACCOUNT)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_ACCOUNT && resultCode == Activity.RESULT_OK) {
            loadAccounts()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
