package com.sosobro.sosomonenote.ui.analysis

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.components.XAxis
import com.sosobro.sosomonenote.database.DatabaseInstance
import com.sosobro.sosomonenote.databinding.FragmentAnalysisBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AnalysisFragment : Fragment() {

    private var _binding: FragmentAnalysisBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadAnalysis()
    }

    private fun loadAnalysis() {
        lifecycleScope.launch {

            val context = requireContext()
            val db = DatabaseInstance.getDatabase(context)
            val transactionDao = db.transactionDao()
            val categoryDao = db.categoryDao()

            val allCategories = withContext(Dispatchers.IO) {
                categoryDao.getAll()
            }

            // ---- 本月年月字串 ----
            val calendar = Calendar.getInstance()
            val yearStr = calendar.get(Calendar.YEAR).toString()
            val monthStr = String.format("%02d", calendar.get(Calendar.MONTH) + 1)

            // ---- 查詢本月資料 ----
            val monthTransactions = withContext(Dispatchers.IO) {
                transactionDao.getValidTransactionsForMonth(yearStr, monthStr)
            }

            Log.d("SSS", "查詢 year=$yearStr, month=$monthStr, 共=${monthTransactions.size} 筆")

            val expenses = monthTransactions.filter { it.type == "支出" }
            val incomes = monthTransactions.filter { it.type == "收入" }

            setupPieChartExpenses(expenses, allCategories)
            setupPieChartIncome(incomes, allCategories)
            setupLineChart(monthTransactions)
        }
    }

    // --------------------------- Pie Chart: 支出 ----------------------------
    private fun setupPieChartExpenses(
        expenses: List<com.sosobro.sosomonenote.database.TransactionEntity>,
        categories: List<com.sosobro.sosomonenote.database.CategoryEntity>
    ) {
        val total = expenses.sumOf { kotlin.math.abs(it.amount) }
        if (total == 0.0) {
            binding.pieExpense.data = null
            binding.pieExpense.invalidate()
            return
        }

        val grouped = expenses.groupBy { it.category }
            .map { (cat, list) ->
                val sum = list.sumOf { kotlin.math.abs(it.amount) }
                PieEntry(sum.toFloat(), cat)
            }

        val dataSet = PieDataSet(grouped, "")
        dataSet.colors = listOf(
            Color.parseColor("#F94144"), Color.parseColor("#F3722C"), Color.parseColor("#F8961E"),
            Color.parseColor("#F9C74F"), Color.parseColor("#90BE6D"), Color.parseColor("#43AA8B")
        )
        dataSet.sliceSpace = 2f

        // ⭐ 標籤移到外面 + 指示線
        dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.valueLinePart1OffsetPercentage = 80f
        dataSet.valueLinePart1Length = 0.4f
        dataSet.valueLinePart2Length = 0.6f
        dataSet.valueLineColor = Color.GRAY

        val pieData = PieData(dataSet)
        pieData.setValueTextSize(10f)
        pieData.setValueTextColor(Color.BLACK)
        pieData.setValueFormatter(PercentFormatter(binding.pieExpense))

        binding.pieExpense.apply {
            data = pieData
            setUsePercentValues(true)
            description.isEnabled = false
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            animateY(800)
            invalidate()
        }
    }

    // --------------------------- Pie Chart: 收入 ----------------------------
    private fun setupPieChartIncome(
        incomes: List<com.sosobro.sosomonenote.database.TransactionEntity>,
        categories: List<com.sosobro.sosomonenote.database.CategoryEntity>
    ) {
        val total = incomes.sumOf { it.amount }
        if (total == 0.0) {
            binding.pieIncome.data = null
            binding.pieIncome.invalidate()
            return
        }

        val grouped = incomes.groupBy { it.category }
            .map { (cat, list) ->
                val sum = list.sumOf { it.amount }
                PieEntry(sum.toFloat(), cat)
            }

        val dataSet = PieDataSet(grouped, "")
        dataSet.colors = listOf(
            Color.parseColor("#4cc9f0"), Color.parseColor("#4895ef"), Color.parseColor("#4361ee"),
            Color.parseColor("#3a0ca3"), Color.parseColor("#7209b7"), Color.parseColor("#f72585")
        )
        dataSet.sliceSpace = 2f

        // ⭐ 標籤移到外面 + 指示線
        dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.valueLinePart1OffsetPercentage = 80f
        dataSet.valueLinePart1Length = 0.4f
        dataSet.valueLinePart2Length = 0.6f
        dataSet.valueLineColor = Color.GRAY

        val pieData = PieData(dataSet)
        pieData.setValueTextSize(10f)
        pieData.setValueTextColor(Color.BLACK)
        pieData.setValueFormatter(PercentFormatter(binding.pieIncome))

        binding.pieIncome.apply {
            data = pieData
            setUsePercentValues(true)
            description.isEnabled = false
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            animateY(800)
            invalidate()
        }
    }

    // --------------------------- Line Chart: 支出 + 收入 ----------------------------
    private fun setupLineChart(
        list: List<com.sosobro.sosomonenote.database.TransactionEntity>
    ) {
        if (list.isEmpty()) {
            binding.lineChart.clear()
            return
        }

        val df = SimpleDateFormat("dd", Locale.getDefault())

        val expenseEntries = ArrayList<Entry>()
        val incomeEntries = ArrayList<Entry>()

        list.groupBy { df.format(Date(it.time)).toInt() }.forEach { (day, transactions) ->
            val dailyExpense = transactions.filter { it.type == "支出" }
                .sumOf { kotlin.math.abs(it.amount) }.toFloat()

            val dailyIncome = transactions.filter { it.type == "收入" }
                .sumOf { it.amount }.toFloat()

            expenseEntries.add(Entry(day.toFloat(), dailyExpense))
            incomeEntries.add(Entry(day.toFloat(), dailyIncome))
        }

        val expenseSet = LineDataSet(expenseEntries, "支出").apply {
            color = Color.RED
            circleRadius = 3f
            setCircleColor(Color.RED)
            lineWidth = 2f
        }

        val incomeSet = LineDataSet(incomeEntries, "收入").apply {
            color = Color.BLUE
            circleRadius = 3f
            setCircleColor(Color.BLUE)
            lineWidth = 2f
        }

        val lineData = LineData(expenseSet, incomeSet)

        binding.lineChart.apply {
            data = lineData
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisRight.isEnabled = false
            animateX(800)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
