package com.sosobro.sosomonenote.ui.report

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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

    private val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val titleFormat = SimpleDateFormat("M月 d日 EEEE", Locale.TAIWAN) // ← UI格式

    fun loadMonthData(year: Int, month: Int) {
        viewModelScope.launch(Dispatchers.IO) {

            val all = dao.getAllTransactions()

            // ✦ 過濾出該月資料
            val monthly = all.filter {
                val d = runCatching { parser.parse(it.date) }.getOrNull()
                d?.let { dd ->
                    val c = Calendar.getInstance().apply { time = dd }
                    c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) + 1 == month
                } ?: false
            }

            // ✦ 以日期分組（使用 UI 想要的 key）
            val grouped = monthly
                .sortedByDescending { it.date }
                .groupBy {
                    val d = parser.parse(it.date)
                    titleFormat.format(d!!) // ★ 這裡是 group key（例如：11月14日 星期五）
                }
                .toList()

            // 月統計
            val expense = monthly.filter { it.type.contains("支出") }.sumOf { it.amount }
            val income = monthly.filter { it.type.contains("收入") }.sumOf { it.amount }

            _transactionsByDate.postValue(grouped)
            _monthlySummary.postValue(Triple(expense, income, income - expense))
        }
    }
}
