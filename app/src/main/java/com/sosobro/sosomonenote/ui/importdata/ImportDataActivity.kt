package com.sosobro.sosomonenote.ui.importdata

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.opencsv.CSVReader
import com.sosobro.sosomonenote.R
import com.sosobro.sosomonenote.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class ImportDataActivity : AppCompatActivity() {

    private var loadingDialog: AlertDialog? = null

    private val pickCsvFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) importCsvToDatabase(uri)
            else Toast.makeText(this, "未選擇檔案", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_data)

        findViewById<Button>(R.id.btnSelectFile).setOnClickListener {
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

                val reader = CSVReader(
                    InputStreamReader(
                        contentResolver.openInputStream(uri),
                        Charsets.UTF_8
                    )
                )

                val rows = reader.readAll()
                if (rows.isEmpty()) {
                    hideLoadingDialog()
                    Toast.makeText(this@ImportDataActivity, "檔案為空", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val dataRows = rows.drop(1)
                val sdfInput = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val sdfOutput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                val transactions = mutableListOf<TransactionEntity>()

                for (parts in dataRows) {
                    if (parts.size < 8) continue

                    // 日期轉換
                    val rawDate = parts[0].trim()
                    val formattedDate = try {
                        val parsed = sdfInput.parse(rawDate)
                        sdfOutput.format(parsed!!)
                    } catch (e: Exception) {
                        rawDate.take(10)
                    }

                    // 類別
                    var categoryName = parts[1].trim().ifEmpty { "未分類" }
                    val subCategory = parts.getOrNull(2)?.trim()
                    val type = parts.getOrNull(3)?.trim() ?: "支出"
                    val amount = parts.getOrNull(4)?.toDoubleOrNull() ?: 0.0
                    val currency = parts.getOrNull(5)?.trim() ?: "TWD"
                    val note = parts.getOrNull(6)?.trim()

                    // ⭐ 帳戶
                    val accountName =
                        parts.getOrNull(7)?.trim()?.ifEmpty { "預設帳戶" } ?: "預設帳戶"

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

                    // ⭐ 分類處理
                    categoryName = categoryName
                        .replace("(收入)", "")
                        .replace("(支出)", "")
                        .trim()

                    if (categoryName != "未分類") {
                        withContext(Dispatchers.IO) {
                            val exist = categoryDao.findByNameAndType(categoryName, type)
                            if (exist == null) {
                                val newCat = CategoryEntity(name = categoryName, type = type)
                                categoryDao.insert(newCat)
                            }
                        }
                    }

                    // 建立交易
                    transactions.add(
                        TransactionEntity(
                            accountId = accountId,
                            date = formattedDate,
                            category = categoryName,
                            subCategory = subCategory,
                            type = type,
                            amount = amount,
                            currency = currency,
                            note = note,
                            book = parts.getOrNull(8)?.trim(),
                            tag = parts.getOrNull(9)?.trim(),
                            includeInBudget = parts.getOrNull(10)?.trim() == "1",
                            time = System.currentTimeMillis(),
                            image = parts.getOrNull(12)?.trim()
                        )
                    )
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

                // ⭐ 通知 MainActivity 更新資料（關鍵）
                val result = Intent()
                result.putExtra("DATA_UPDATED", true)
                setResult(RESULT_OK, result)

                finish()

            } catch (e: Exception) {
                hideLoadingDialog()

                Toast.makeText(
                    this@ImportDataActivity,
                    "❌ 匯入失敗：${e.message}",
                    Toast.LENGTH_LONG
                ).show()
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
