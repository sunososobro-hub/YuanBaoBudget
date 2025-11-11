package com.sosobro.sosomonenote.ui.overview

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.sosobro.sosomonenote.R
import com.sosobro.sosomonenote.database.DatabaseInstance
import com.sosobro.sosomonenote.databinding.FragmentOverviewBinding
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
        super.onViewCreated(view, savedInstanceState)
        setupLineChart(binding.lineChart)
        loadOverviewData()
        setupBudgetTanks()
    }

    private fun setupLineChart(chart: LineChart) {
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            axisRight.isEnabled = false
            extraBottomOffset = 10f

            val daysInMonth = 31
            val dayLabels = (1..daysInMonth).map { "${it}日" }

            marker = CustomMarkerView(requireContext(), R.layout.marker_view, dayLabels)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = Color.parseColor("#4A3B2A")
                textSize = 12f
                granularity = 1f
                axisLineColor = Color.parseColor("#CCCCCC")
                valueFormatter = IndexAxisValueFormatter(dayLabels)
            }

            axisLeft.apply {
                textColor = Color.parseColor("#4A3B2A")
                setDrawGridLines(true)
                gridColor = Color.parseColor("#DDDDDD")
                axisLineColor = Color.parseColor("#CCCCCC")
                setLabelCount(6, true)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value >= 1_000_000) {
                            "NT$${String.format("%.1fM", value / 1_000_000)}"
                        } else {
                            "NT$${"%,.0f".format(value)}"
                        }
                    }
                }
            }

            setViewPortOffsets(80f, 40f, 80f, 40f)
        }
    }

    private fun loadOverviewData() {
        lifecycleScope.launch {
            val db = DatabaseInstance.getDatabase(requireContext())

            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            val allAccounts = withContext(Dispatchers.IO) { db.accountDao().getAllAccounts() }
            val startOfMonth = "%04d-%02d-01".format(year, month)

            val previousTransactions = withContext(Dispatchers.IO) {
                db.transactionDao().getTransactionsBeforeDate(startOfMonth)
            }

            var initialTotal = allAccounts.sumOf { it.balance }
            previousTransactions.forEach {
                if (it.type == "收入") initialTotal -= it.amount
                else if (it.type == "支出") initialTotal += it.amount
            }

            val monthTransactions = withContext(Dispatchers.IO) {
                db.transactionDao().getTransactionsForMonth(year, month)
            }

            val assetPerDay = mutableListOf<Float>()
            var runningTotal = initialTotal.toFloat()

            for (day in 1..daysInMonth) {
                val dayStr = "%04d-%02d-%02d".format(year, month, day)
                val dailyTransactions = monthTransactions.filter { it.date.startsWith(dayStr) }

                val income = dailyTransactions.filter { it.type == "收入" }.sumOf { it.amount }
                val expense = dailyTransactions.filter { it.type == "支出" }.sumOf { it.amount }

                runningTotal += (income - expense).toFloat()
                assetPerDay.add(runningTotal)
            }

            withContext(Dispatchers.Main) {
                binding.tvTotalAssets.text = "NT$${"%,.0f".format(runningTotal)}"

                val entries = assetPerDay.mapIndexed { index, value -> Entry(index.toFloat(), value) }
                if (entries.isEmpty()) {
                    binding.lineChart.clear()
                    binding.lineChart.invalidate()
                    return@withContext
                }

                val dataSet = LineDataSet(entries, "本月資產變化").apply {
                    color = Color.parseColor("#5B6FC7")
                    setCircleColor(Color.parseColor("#A67C52"))
                    lineWidth = 2f
                    circleRadius = 3f
                    valueTextColor = Color.TRANSPARENT
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }

                binding.lineChart.data = LineData(dataSet)
                val minY = assetPerDay.minOrNull() ?: 0f
                val maxY = assetPerDay.maxOrNull() ?: 0f
                binding.lineChart.axisLeft.axisMinimum = minY * 0.98f
                binding.lineChart.axisLeft.axisMaximum = maxY * 1.02f
                binding.lineChart.invalidate()
            }
        }
    }

    /** 💧四個水缸預算 */
    private fun setupBudgetTanks() {
        binding.waterTankMonth.setLevel(0.6f)   // 月度預算60%
        binding.waterTankBiMonth.setLevel(0.4f) // 兩月預算40%
        binding.waterTankQuarter.setLevel(0.75f) // 季度預算75%
        binding.waterTankYear.setLevel(0.3f)    // 年度預算30%
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
