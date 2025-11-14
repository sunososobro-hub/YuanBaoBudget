package com.sosobro.sosomonenote.ui.record

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.sosobro.sosomonenote.R
import com.sosobro.sosomonenote.database.TransactionEntity
import com.sosobro.sosomonenote.databinding.ActivityRecordBinding
import com.sosobro.sosomonenote.ui.category.CategoryPickerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordBinding
    private lateinit var categoryAdapter: CategoryPickerAdapter
    private var currentType = "支出"
    private var selectedCategory: String? = null
    private var selectedIconRes: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tabs
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("支出"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("收入"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("轉帳"))

        // RecyclerView
        categoryAdapter = CategoryPickerAdapter { category ->
            selectedCategory = category.name
            selectedIconRes = category.iconRes
            binding.tvCategory.text = "分類：${category.name}"
        }

        binding.recyclerCategories.layoutManager = GridLayoutManager(this, 4)
        binding.recyclerCategories.adapter = categoryAdapter

        loadCategories()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentType = tab.text.toString()

                if (currentType == "轉帳") {
                    binding.recyclerCategories.visibility = View.GONE
                    binding.layoutTransfer.visibility = View.VISIBLE
                } else {
                    binding.recyclerCategories.visibility = View.VISIBLE
                    binding.layoutTransfer.visibility = View.GONE
                    loadCategories()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.btnConfirm.setOnClickListener { saveRecord() }
    }

    private fun loadCategories() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = com.sosobro.sosomonenote.database.DatabaseInstance.getDatabase(this@RecordActivity)
            val dao = db.categoryDao()

            val categories = dao.getCategoriesByType(currentType)

            withContext(Dispatchers.Main) {
                categoryAdapter.submitList(categories)
            }
        }
    }

    private fun saveRecord() {
        val amountText = binding.etAmount.text.toString().trim()
        val note = binding.etNote.text.toString().trim()
        val amount = amountText.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            Toast.makeText(this, "請輸入正確金額", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentType != "轉帳" && selectedCategory == null) {
            Toast.makeText(this, "請選擇分類", Toast.LENGTH_SHORT).show()
            return
        }

        val db = com.sosobro.sosomonenote.database.DatabaseInstance.getDatabase(this)
        val transactionDao = db.transactionDao()
        val accountDao = db.accountDao()

        lifecycleScope.launch(Dispatchers.IO) {
            when (currentType) {
                "支出", "收入" -> {
                    val accountName = binding.tvAccount.text.toString().removePrefix("帳戶：")
                    val account = accountDao.getAccountByName(accountName)

                    if (account != null) {
                        val transaction = TransactionEntity(
                            accountId = account.id,
                            date = getTodayDate(),
                            category = selectedCategory!!,
                            type = currentType,
                            amount = if (currentType == "支出") -amount else amount,
                            time = System.currentTimeMillis(),   // ⭐ 必填補上！
                            note = note
                        )
                        transactionDao.insert(transaction)
                    }
                }

                "轉帳" -> { /* Same as你的原始邏輯 */ }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@RecordActivity, "記帳完成：$currentType", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun getTodayDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}
