package com.sosobro.sosomonenote.ui.account

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sosobro.sosomonenote.database.DatabaseInstance
import com.sosobro.sosomonenote.databinding.ActivityAccountDetailBinding
import com.sosobro.sosomonenote.ui.addaccount.AddAccountActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AccountDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountDetailBinding
    private lateinit var adapter: TransactionAdapter
    private var accountId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        accountId = intent.getIntExtra("accountId", -1)
        if (accountId == -1) {
            Toast.makeText(this, "無效的帳戶 ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 初始化 RecyclerView
        adapter = TransactionAdapter(emptyList())
        binding.recyclerTransactions.layoutManager = LinearLayoutManager(this)
        binding.recyclerTransactions.adapter = adapter

        // 載入帳戶資訊
        loadAccountInfo(accountId)

        // 設定月份選擇器
        setupMonthSpinner()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnEditAccount.setOnClickListener {
            val intent = Intent(this, AddAccountActivity::class.java)
            intent.putExtra("accountId", accountId)
            startActivity(intent)
        }

        binding.btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("刪除帳戶")
                .setMessage("確定要刪除此帳戶嗎？此動作無法復原。")
                .setPositiveButton("刪除") { _, _ -> deleteAccount(accountId) }
                .setNegativeButton("取消", null)
                .show()
        }
    }

    // --------------------------------------------------------------
    // ⭐ 載入帳戶資訊
    // --------------------------------------------------------------
    private fun loadAccountInfo(accountId: Int) {
        lifecycleScope.launch {
            val db = DatabaseInstance.getDatabase(this@AccountDetailActivity)

            val account = withContext(Dispatchers.IO) {
                db.accountDao().getAccountById(accountId)
            }

            if (account == null) {
                Toast.makeText(this@AccountDetailActivity, "此帳戶已不存在", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            binding.tvAccountName.text = account.name
            binding.tvBalance.text = "NT$${String.format("%,.0f", account.balance)}"
        }
    }

    // --------------------------------------------------------------
    // ⭐ 設定月份選擇器
    // --------------------------------------------------------------
    private fun setupMonthSpinner() {
        val months = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)

        // 產生 12 個月份
        for (m in 1..12) {
            months.add(String.format("%d-%02d", year, m))
        }

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = spinnerAdapter

        // 預設選當月
        val currentMonth = String.format("%d-%02d", year, calendar.get(Calendar.MONTH) + 1)
        val defaultIndex = months.indexOf(currentMonth)
        if (defaultIndex != -1) binding.spinnerMonth.setSelection(defaultIndex)

        // 監聽切換
        binding.spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val ym = months[position]
                val yearStr = ym.substring(0, 4)
                val monthStr = ym.substring(5, 7)
                loadTransactionsByMonth(yearStr, monthStr)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // --------------------------------------------------------------
    // ⭐ 依年份＋月份載入交易（你的 DAO 版本）
    // --------------------------------------------------------------
    private fun loadTransactionsByMonth(yearStr: String, monthStr: String) {
        lifecycleScope.launch {
            val db = DatabaseInstance.getDatabase(this@AccountDetailActivity)

            val list = withContext(Dispatchers.IO) {
                db.transactionDao().getValidTransactionsForMonth(yearStr, monthStr)
            }

            adapter.updateData(list)
        }
    }

    // --------------------------------------------------------------
    // ⭐ 刪除帳戶
    // --------------------------------------------------------------
    private fun deleteAccount(accountId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = DatabaseInstance.getDatabase(this@AccountDetailActivity)
                val accountDao = db.accountDao()
                val transactionDao = db.transactionDao()

                transactionDao.deleteByAccountId(accountId)
                accountDao.deleteById(accountId)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AccountDetailActivity, "帳戶已刪除", Toast.LENGTH_SHORT).show()
                    finish()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AccountDetailActivity,
                        "刪除失敗：${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
