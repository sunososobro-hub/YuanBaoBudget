package com.sosobro.sosomonenote.ui.report

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sosobro.sosomonenote.database.DatabaseInstance
import com.sosobro.sosomonenote.database.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReportViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = DatabaseInstance.getDatabase(application).transactionDao()

    private val _transactionsByDate =
        MutableLiveData<List<Pair<String, List<TransactionEntity>>>>()
    val transactionsByDate: LiveData<List<Pair<String, List<TransactionEntity>>>> =
        _transactionsByDate

    private val _monthlySummary = MutableLiveData<Triple<Double, Double, Double>>()
    val monthlySummary: LiveData<Triple<Double, Double, Double>> = _monthlySummary

    fun loadMonthData(year: Int, month: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val all = dao.getAllTransactions() // 取出所有交易紀錄
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            // 🔹 過濾出該月資料
            val filtered = all.filter {
                val d = runCatching { dateFormat.parse(it.date) }.getOrNull()
                d?.let { cal ->
                    val c = Calendar.getInstance().apply { time = cal }
                    c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) + 1 == month
                } ?: false
            }

            // 🔹 以日期分組（例如：2025-11-09 → [多筆交易]）
            val grouped = filtered.groupBy { it.date.substring(0, 10) }
                .toList()
                .sortedByDescending { it.first } // 日期新到舊排序

            val expense = filtered.filter { it.type.contains("支出") }.sumOf { it.amount }
            val income = filtered.filter { it.type.contains("收入") }.sumOf { it.amount }

            _transactionsByDate.postValue(grouped)
            _monthlySummary.postValue(Triple(expense, income, income - expense))
        }
    }
}
