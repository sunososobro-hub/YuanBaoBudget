package com.sosobro.sosomonenote.ui.analysis

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sosobro.sosomonenote.database.DatabaseInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class AnalysisViewModel : ViewModel() {

    private val _dailyAssets = MutableLiveData<List<Pair<String, Double>>>()
    val dailyAssets: LiveData<List<Pair<String, Double>>> = _dailyAssets

    fun loadMonthlyAssets(context: android.content.Context, year: Int, month: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = DatabaseInstance.getDatabase(context)
            val dao = db.transactionDao()

            // 取得所有交易紀錄
            val allTransactions = dao.getAllTransactions()

            // 過濾出指定月份的資料
            val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val filtered = allTransactions.filter {
                it.date.startsWith(String.format("%04d-%02d", year, month))
            }

            // 根據日期分組並計算每日總資產（收入 - 支出）
            val grouped = filtered.groupBy { it.date }.map { (date, list) ->
                val total = list.sumOf {
                    if (it.type.contains("支出")) -it.amount else it.amount
                }
                date to total
            }.sortedBy { it.first }

            _dailyAssets.postValue(grouped)
        }
    }
}
