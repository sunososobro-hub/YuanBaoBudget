package com.sosobro.sosomonenote.ui.analysis

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.tabs.TabLayout
import com.sosobro.sosomonenote.R
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

    private var expensesView: View? = null
    private var incomeView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {

            val context = requireContext()
            val db = DatabaseInstance.getDatabase(context)
            val transactionDao = db.transactionDao()
            val categoryDao = db.categoryDao()

            val allCategories = withContext(Dispatchers.IO) { categoryDao.getAll() }

            // ---- 本月年月字串 ----
            val calendar = Calendar.getInstance()
            val yearStr = calendar.get(Calendar.YEAR).toString()
            val monthStr = String.format("%02d", calendar.get(Calendar.MONTH) + 1)

            // ---- 查詢本月資料 ----
            val monthTransactions = withContext(Dispatchers.IO) {
                transactionDao.getValidTransactionsForMonth(yearStr, monthStr)
            }

            val expenses = monthTransactions.filter { it.type == "支出" }
            val incomes = monthTransactions.filter { it.type == "收入" }

            setupTabs(expenses, incomes, monthTransactions, allCategories)
        }
    }

    // -------------------------------- Tabs --------------------------------
    private fun setupTabs(
        expenses: List<com.sosobro.sosomonenote.database.TransactionEntity>,
        incomes: List<com.sosobro.sosomonenote.database.TransactionEntity>,
        allTransactions: List<com.sosobro.sosomonenote.database.TransactionEntity>,
        categories: List<com.sosobro.sosomonenote.database.CategoryEntity>
    ) {

        val inflater = LayoutInflater.from(requireContext())

        // Load views once
        if (expensesView == null)
            expensesView = inflater.inflate(R.layout.tab_expense, null)
        if (incomeView == null)
            incomeView = inflater.inflate(R.layout.tab_income, null)

        // 預設：支出
        binding.tabContainer.removeAllViews()
        binding.tabContainer.addView(expensesView)

        // 設定圖表
        setupPieChartExpenses(expenses, categories)
        setupPieChartIncome(incomes, categories)
        setupLineChartExpense(expensesView!!, allTransactions)
        setupLineChartIncome(incomeView!!, allTransactions)

        // -------- Tab 切換 --------
        binding.tabMode.removeAllTabs()
        binding.tabMode.addTab(binding.tabMode.newTab().setText("支出"))
        binding.tabMode.addTab(binding.tabMode.newTab().setText("收入"))

        binding.tabMode.setSelectedTabIndicatorColor(Color.parseColor("#F7D774"))
        binding.tabMode.setTabTextColors(Color.parseColor("#4A3B2A"), Color.parseColor("#F7D774"))

        binding.tabMode.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.tabContainer.removeAllViews()
                when (tab.position) {
                    0 -> binding.tabContainer.addView(expensesView)
                    1 -> binding.tabContainer.addView(incomeView)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    // -------------------------------- 圓餅圖：支出 --------------------------------
    private fun setupPieChartExpenses(
        expenses: List<com.sosobro.sosomonenote.database.TransactionEntity>,
        categories: List<com.sosobro.sosomonenote.database.CategoryEntity>
    ) {
        val total = expenses.sumOf { kotlin.math.abs(it.amount) }
        if (total == 0.0 || expensesView == null) return

        val pie = expensesView!!.findViewById<com.github.mikephil.charting.charts.PieChart>(R.id.pieExpense)

        val grouped = expenses.groupBy { it.category }
            .map { (cat, list) -> PieEntry(list.sumOf { kotlin.math.abs(it.amount) }.toFloat(), cat) }

        val dataSet = PieDataSet(grouped, "").apply {
            colors = listOf(
                Color.parseColor("#F94144"), Color.parseColor("#F3722C"), Color.parseColor("#F8961E"),
                Color.parseColor("#F9C74F"), Color.parseColor("#90BE6D"), Color.parseColor("#43AA8B")
            )
            sliceSpace = 2f
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            valueLinePart1OffsetPercentage = 80f
            valueLinePart1Length = 0.4f
            valueLinePart2Length = 0.6f
            valueLineColor = Color.GRAY
        }

        pie.apply {
            data = PieData(dataSet).apply {
                setValueTextSize(10f)
                setValueTextColor(Color.BLACK)
                setValueFormatter(PercentFormatter(pie))
            }
            setUsePercentValues(true)
            description.isEnabled = false
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            animateY(800)
            invalidate()
        }
    }

    // -------------------------------- 圓餅圖：收入 --------------------------------
    private fun setupPieChartIncome(
        incomes: List<com.sosobro.sosomonenote.database.TransactionEntity>,
        categories: List<com.sosobro.sosomonenote.database.CategoryEntity>
    ) {
        val total = incomes.sumOf { it.amount }
        if (total == 0.0 || incomeView == null) return

        val pie = incomeView!!.findViewById<com.github.mikephil.charting.charts.PieChart>(R.id.pieIncome)

        val grouped = incomes.groupBy { it.category }
            .map { (cat, list) -> PieEntry(list.sumOf { it.amount }.toFloat(), cat) }

        val dataSet = PieDataSet(grouped, "").apply {
            colors = listOf(
                Color.parseColor("#4cc9f0"), Color.parseColor("#4895ef"), Color.parseColor("#4361ee"),
                Color.parseColor("#3a0ca3"), Color.parseColor("#7209b7"), Color.parseColor("#f72585")
            )
            sliceSpace = 2f
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            valueLinePart1OffsetPercentage = 80f
            valueLinePart1Length = 0.4f
            valueLinePart2Length = 0.6f
            valueLineColor = Color.GRAY
        }

        pie.apply {
            data = PieData(dataSet).apply {
                setValueTextSize(10f)
                setValueTextColor(Color.BLACK)
                setValueFormatter(PercentFormatter(pie))
            }
            setUsePercentValues(true)
            description.isEnabled = false
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            animateY(800)
            invalidate()
        }
    }

    // -------------------------------- 折線圖：支出 --------------------------------
    private fun setupLineChartExpense(view: View, all: List<com.sosobro.sosomonenote.database.TransactionEntity>) {
        val chart = view.findViewById<com.github.mikephil.charting.charts.LineChart>(R.id.lineChartExpense)
        setupLineChartInternal(chart, all)
    }

    // -------------------------------- 折線圖：收入 --------------------------------
    private fun setupLineChartIncome(view: View, all: List<com.sosobro.sosomonenote.database.TransactionEntity>) {
        val chart = view.findViewById<com.github.mikephil.charting.charts.LineChart>(R.id.lineChartIncome)
        setupLineChartInternal(chart, all)
    }

    // -------------------------------- 共享折線圖 --------------------------------
    private fun setupLineChartInternal(
        chart: com.github.mikephil.charting.charts.LineChart,
        list: List<com.sosobro.sosomonenote.database.TransactionEntity>
    ) {
        if (list.isEmpty()) {
            chart.clear()
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

        chart.apply {
            data = LineData(expenseSet, incomeSet)
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
