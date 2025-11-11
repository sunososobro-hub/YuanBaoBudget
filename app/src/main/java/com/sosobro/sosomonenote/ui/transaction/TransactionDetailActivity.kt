package com.sosobro.sosomonenote.ui.transaction

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sosobro.sosomonenote.database.DatabaseInstance
import com.sosobro.sosomonenote.databinding.ActivityTransactionDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransactionDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val transactionId = intent.getIntExtra("transactionId", -1)
        if (transactionId == -1) {
            finish()
            return
        }

        // 載入交易詳情
        loadTransaction(transactionId)

        // 🔹 刪除按鈕
        binding.btnDeleteTransaction.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("刪除交易")
                .setMessage("確定要刪除此筆交易嗎？此動作無法復原。")
                .setPositiveButton("刪除") { _, _ ->
                    deleteTransaction(transactionId)
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }

    private fun loadTransaction(transactionId: Int) {
        lifecycleScope.launch {
            val db = DatabaseInstance.getDatabase(this@TransactionDetailActivity)
            val transaction = withContext(Dispatchers.IO) {
                db.transactionDao().getTransactionById(transactionId)
            }

            if (transaction == null) {
                Toast.makeText(this@TransactionDetailActivity, "交易不存在", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            binding.tvCategory.text = transaction.category
            binding.tvAmount.text =
                "NT$${"%,.0f".format(transaction.amount)} (${transaction.type})"
            binding.tvDate.text = transaction.date
            binding.tvNote.text = transaction.note ?: "（無備註）"
        }
    }

    private fun deleteTransaction(transactionId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = DatabaseInstance.getDatabase(this@TransactionDetailActivity)
            val transactionDao = db.transactionDao()

            transactionDao.deleteById(transactionId)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@TransactionDetailActivity, "交易已刪除", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}
