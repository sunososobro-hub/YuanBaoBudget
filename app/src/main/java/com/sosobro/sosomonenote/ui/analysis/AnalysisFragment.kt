package com.sosobro.sosomonenote.ui.analysis

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.sosobro.sosomonenote.R
import com.sosobro.sosomonenote.databinding.FragmentAnalysisBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AnalysisFragment  : Fragment() {

    private var _binding: FragmentAnalysisBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalysisViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 🔹 改成這樣
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val calendarInstance = Calendar.getInstance()
        val yearNow = calendarInstance.get(Calendar.YEAR)
        val monthNow = calendarInstance.get(Calendar.MONTH) + 1

        viewModel.loadMonthlyAssets(requireContext(), yearNow, monthNow)

        viewModel.dailyAssets.observe(viewLifecycleOwner) { data ->
            setupLineChart(binding.lineChart, data)
        }
    }


    private fun setupLineChart(chart: LineChart, data: List<Pair<String, Double>>) {
        val entries = data.mapIndexed { index, (date, value) ->
            Entry(index.toFloat(), value.toFloat())
        }

        val dataSet = LineDataSet(entries, "每日總資產").apply {
            color = Color.parseColor("#5B6FC7")
            setCircleColor(Color.parseColor("#5B6FC7"))
            lineWidth = 2f
            circleRadius = 4f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#C5CAE9")
            setDrawValues(false) // ✅ 不顯示數字
        }

        chart.apply {
            this.data = LineData(dataSet)
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            axisRight.isEnabled = false

            // ✅ X軸顯示 yyyy-MM-dd
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)

                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(
                    data.map {
                        try {
                            val parsed = inputFormat.parse(it.first)
                            outputFormat.format(parsed ?: it.first)
                        } catch (e: Exception) {
                            it.first.take(10)
                        }
                    }
                )
                textColor = Color.DKGRAY
                labelRotationAngle = -45f
                granularity = 1f
            }

            axisLeft.apply {
                textColor = Color.DKGRAY
                setDrawGridLines(true)
            }

            legend.isEnabled = false
            animateX(700)

            // ✅ 加入 MarkerView 顯示日期與金額
            val marker = CustomMarkerView(
                requireContext(),
                R.layout.marker_view,
                data.map {
                    try {
                        it.first.substring(0, 10)
                    } catch (e: Exception) {
                        it.first
                    }
                }
            )
            marker.chartView = this
            this.marker = marker

            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}