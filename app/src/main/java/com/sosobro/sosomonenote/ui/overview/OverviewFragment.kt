package com.sosobro.sosomonenote.ui.overview

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.tabs.TabLayout
import com.sosobro.sosomonenote.databinding.FragmentOverviewBinding
import com.sosobro.sosomonenote.database.DatabaseInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class OverviewFragment : Fragment() {

    private var _binding: FragmentOverviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupLineChart(binding.lineChart)
        setupTabs()

        loadSummary()
        load30DayOverview()
        setupBudgetTanks()

        parentFragmentManager.setFragmentResultListener("DATA_UPDATED", this) { _, _ ->
            loadSummary()
            load30DayOverview()
        }
    }

    /** ----------------------------
     *  淨資產 Summary 計算
     *  ---------------------------- */
    private fun loadSummary() {
        lifecycleScope.launch {
            val db = DatabaseInstance.getDatabase(requireContext())
            val accounts = withContext(Dispatchers.IO) { db.accountDao().getAllAccounts() }

            var asset = 0.0
            var debt = 0.0

            accounts.forEach {
                if (it.balance >= 0) asset += it.balance
                else debt += it.balance
            }

            binding.layoutSummary.tvAssets.text = "NT$${"%,.0f".format(asset)}"
            binding.layoutSummary.tvDebt.text = "NT$${"%,.0f".format(debt)}"
            binding.layoutSummary.tvNetAssets.text = "NT$${"%,.0f".format(asset + debt)}"
        }
    }

    /** ----------------------------
     *  Tab 設定
     *  ---------------------------- */
    private fun setupTabs() {
        binding.tabMode.removeAllTabs()
        binding.tabMode.addTab(binding.tabMode.newTab().setText("近30日"))
        binding.tabMode.addTab(binding.tabMode.newTab().setText("上個月"))

        binding.tabMode.setSelectedTabIndicatorColor(Color.parseColor("#F7D774"))
        binding.tabMode.setTabTextColors(Color.parseColor("#4A3B2A"), Color.parseColor("#F7D774"))

        binding.tabMode.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> load30DayOverview()
                    1 -> loadLastMonthOverview()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    /** ----------------------------
     *  折線圖初始化
     *  ---------------------------- */
    private fun setupLineChart(chart: LineChart) {
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            axisRight.isEnabled = false

            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.parseColor("#4A3B2A")
            xAxis.setDrawGridLines(false)

            axisLeft.textColor = Color.parseColor("#4A3B2A")
            axisLeft.gridColor = Color.parseColor("#E0E0E0")
            axisLeft.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "NT$${"%,.0f".format(value)}"
                }
            }
        }
    }

    /** ----------------------------
     *  折線圖：近 30 日
     *  ---------------------------- */
    private fun load30DayOverview() {
        lifecycleScope.launch {
            val db = DatabaseInstance.getDatabase(requireContext())

            val accounts = withContext(Dispatchers.IO) { db.accountDao().getAllAccounts() }
            var balance = accounts.sumOf { it.balance }.toFloat()

            val labels = mutableListOf<String>()
            val values = mutableListOf<Float>()

            for (i in 29 downTo 0) {
                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -i) }
                labels.add("${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}")

                val dateStr = "%04d-%02d-%02d".format(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH)
                )

                val daily = withContext(Dispatchers.IO) { db.transactionDao().getTransactionsByDate(dateStr) }
                val income = daily.filter { it.type == "收入" }.sumOf { it.amount }
                val expense = daily.filter { it.type == "支出" }.sumOf { it.amount }

                balance += (income - expense).toFloat()
                values.add(balance)
            }

            drawLineChart(values, labels)
        }
    }

    /** ----------------------------
     *  折線圖：上個月
     *  ---------------------------- */
    private fun loadLastMonthOverview() {
        lifecycleScope.launch {
            val db = DatabaseInstance.getDatabase(requireContext())

            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1
            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

            val accounts = withContext(Dispatchers.IO) { db.accountDao().getAllAccounts() }
            var balance = accounts.sumOf { it.balance }.toFloat()

            val labels = mutableListOf<String>()
            val values = mutableListOf<Float>()

            for (day in 1..maxDay) {
                val dateStr = "%04d-%02d-%02d".format(year, month, day)
                labels.add("$month/$day")

                val daily = withContext(Dispatchers.IO) { db.transactionDao().getTransactionsByDate(dateStr) }
                val income = daily.filter { it.type == "收入" }.sumOf { it.amount }
                val expense = daily.filter { it.type == "支出" }.sumOf { it.amount }

                balance += (income - expense).toFloat()
                values.add(balance)
            }

            drawLineChart(values, labels)
        }
    }

    /** ----------------------------
     *  折線圖繪製
     *  ---------------------------- */
    private fun drawLineChart(values: List<Float>, labels: List<String>) {
        val entries = values.mapIndexed { index, v -> Entry(index.toFloat(), v) }

        val dataSet = LineDataSet(entries, "").apply {
            color = Color.parseColor("#5B6FC7")
            setCircleColor(Color.parseColor("#5B6FC7"))
            lineWidth = 3f
            circleRadius = 4.5f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            valueTextColor = Color.TRANSPARENT

            setDrawFilled(true)
            fillColor = Color.parseColor("#5B6FC7")
            fillAlpha = 80
        }

        binding.lineChart.apply {
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            data = LineData(dataSet)
            invalidate()
        }
    }

    /** ----------------------------
     *  預算水缸
     *  ---------------------------- */
    private fun setupBudgetTanks() {
        binding.waterTankMonth.setLevel(0.6f)
        binding.waterTankBiMonth.setLevel(0.4f)
        binding.waterTankQuarter.setLevel(0.75f)
        binding.waterTankYear.setLevel(0.3f)

        // ✅ 點擊事件
        binding.waterTankMonth.setOnClickListener {
            navigateToBudget("month")
        }
        binding.waterTankBiMonth.setOnClickListener {
            navigateToBudget("bimonth")
        }
        binding.waterTankQuarter.setOnClickListener {
            navigateToBudget("quarter")
        }
        binding.waterTankYear.setOnClickListener {
            navigateToBudget("year")
        }
    }

    private fun navigateToBudget(type: String) {
        val intent = android.content.Intent(requireContext(), com.sosobro.sosomonenote.ui.budget.BudgetSettingActivity::class.java)
        intent.putExtra("BUDGET_TYPE", type)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
