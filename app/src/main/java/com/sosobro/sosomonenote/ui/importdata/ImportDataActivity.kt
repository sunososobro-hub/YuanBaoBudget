package com.sosobro.sosomonenote.ui.importdata

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sosobro.sosomonenote.R
import com.sosobro.sosomonenote.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class ImportDataActivity : AppCompatActivity() {

    private var loadingDialog: AlertDialog? = null

    private val pickCsvFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                importCsvToDatabase(uri)
            } else {
                Toast.makeText(this, "未選擇檔案", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_data)

        val btnSelectFile = findViewById<Button>(R.id.btnSelectFile)
        btnSelectFile.setOnClickListener {
            pickCsvFile.launch("text/*")
        }
    }

    private fun importCsvToDatabase(uri: Uri) {
        showLoadingDialog()

        lifecycleScope.launch {
            try {
                val db = DatabaseInstance.getDatabase(this@ImportDataActivity)
                val transactionDao = db.transactionDao()
                val accountDao = db.accountDao()
                val categoryDao = db.categoryDao()

                val inputStream = contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream!!, Charsets.UTF_8))
                val lines = reader.readLines().map { it.replace("\uFEFF", "") }.drop(1)

                val transactions = mutableListOf<TransactionEntity>()

                for (line in lines) {
                    val parts = line.split(",")
                    if (parts.size < 5) continue

                    // 帳戶名稱
                    val accountName = parts.getOrNull(7)?.trim()?.takeIf { it.isNotEmpty() } ?: "預設帳戶"

                    val accountId = withContext(Dispatchers.IO) {
                        val existing = accountDao.findByName(accountName)
                        if (existing != null) existing.id
                        else {
                            val newAcc = AccountEntity(
                                name = accountName,
                                type = "自動匯入",
                                balance = 0.0
                            )
                            accountDao.insertAccount(newAcc).toInt()
                        }
                    }

                    // 分類
                    var categoryName = parts.getOrNull(1)?.trim()?.ifEmpty { "未分類" } ?: "未分類"
                    val type = parts.getOrNull(3)?.trim()?.ifEmpty { "支出" } ?: "支出"

                    // 去掉(收入)(支出)
                    categoryName = categoryName.replace("(支出)", "")
                        .replace("(收入)", "")
                        .trim()

                    if (categoryName != "未分類") {
                        withContext(Dispatchers.IO) {
                            val existingCategory = categoryDao.findByNameAndType(categoryName, type)
                            if (existingCategory == null) {
                                val newCategory = CategoryEntity(name = categoryName, type = type)
                                categoryDao.insert(newCategory)
                            }
                        }
                    }

                    // ⭐⭐⭐ 正確日期處理
                    val rawDate = parts.getOrNull(0)?.trim() ?: ""

                    val formattedDate = try {
                        val sdfInput = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val sdfOutput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val parsed = sdfInput.parse(rawDate)
                        sdfOutput.format(parsed!!)
                    } catch (e: Exception) {
                        rawDate.take(10) // yyyy-MM-dd
                    }

                    // 建立交易資料
                    val transaction = TransactionEntity(
                        accountId = accountId,
                        date = formattedDate,
                        category = categoryName,
                        subCategory = parts.getOrNull(2)?.trim(),
                        type = type,
                        amount = parts.getOrNull(4)?.toDoubleOrNull() ?: 0.0,
                        currency = parts.getOrNull(5)?.trim() ?: "TWD",
                        note = parts.getOrNull(6)?.trim(),
                        book = parts.getOrNull(8)?.trim(),
                        tag = parts.getOrNull(9)?.trim(),
                        includeInBudget = parts.getOrNull(10)?.trim() == "1",
                        time = System.currentTimeMillis(),
                        image = parts.getOrNull(12)?.trim()
                    )

                    transactions.add(transaction)
                }

                // 寫入資料庫
                withContext(Dispatchers.IO) {
                    transactionDao.insertAll(transactions)
                }

                hideLoadingDialog()
                Toast.makeText(
                    this@ImportDataActivity,
                    "✅ 匯入完成，共 ${transactions.size} 筆資料",
                    Toast.LENGTH_LONG
                ).show()
                finish()

            } catch (e: Exception) {
                hideLoadingDialog()
                Toast.makeText(this@ImportDataActivity, "❌ 匯入失敗：${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showLoadingDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null)
        loadingDialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()
        loadingDialog?.show()
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }
}
