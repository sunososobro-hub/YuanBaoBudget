package com.sosobro.sosomonenote.ui.account

import android.content.Intent
import android.os.Bundle
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

class AccountDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountDetailBinding
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val accountId = intent.getIntExtra("accountId", -1)
        if (accountId == -1) {
            Toast.makeText(this, "無效的帳戶 ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 初始化交易清單
        adapter = TransactionAdapter(emptyList())
        binding.recyclerTransactions.layoutManager = LinearLayoutManager(this)
        binding.recyclerTransactions.adapter = adapter

        // 載入帳戶資料與交易紀錄
        loadAccountDetails(accountId)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnEditAccount.setOnClickListener {
            val intent = Intent(this, AddAccountActivity::class.java)
            intent.putExtra("accountId", accountId) // 傳帳戶ID過去
            startActivity(intent)
        }


        binding.btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("刪除帳戶")
                .setMessage("確定要刪除此帳戶嗎？此動作無法復原。")
                .setPositiveButton("刪除") { _, _ ->
                    deleteAccount(accountId)
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }

    private fun loadAccountDetails(accountId: Int) {
        lifecycleScope.launch {
            val db = DatabaseInstance.getDatabase(this@AccountDetailActivity)

            val account = withContext(Dispatchers.IO) {
                db.accountDao().getAccountById(accountId)
            }

            // 🔹 若帳戶不存在，結束 Activity
            if (account == null) {
                Toast.makeText(this@AccountDetailActivity, "此帳戶已不存在", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            val transactions = withContext(Dispatchers.IO) {
                db.transactionDao().getTransactionsByAccount(accountId)
            }

            binding.tvAccountName.text = account.name
            binding.tvBalance.text = "NT$${String.format("%,.0f", account.balance)}"

            adapter.updateData(transactions)
        }
    }

    private fun deleteAccount(accountId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = DatabaseInstance.getDatabase(this@AccountDetailActivity)
                val accountDao = db.accountDao()
                val transactionDao = db.transactionDao()

                // 🔹 刪除該帳戶的所有交易
                transactionDao.deleteByAccountId(accountId)

                // 🔹 刪除帳戶本身
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
