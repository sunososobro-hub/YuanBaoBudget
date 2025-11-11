package com.sosobro.sosomonenote.ui.home

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import com.sosobro.sosomonenote.R
import java.util.*

class MonthPickerDialog(
    private val ctx: Context,
    private val onMonthSelected: (year: Int, month: Int) -> Unit,
    private val currentYear: Int,
    private val currentMonth: Int
) {
    fun show() {
        val inflater = LayoutInflater.from(ctx)
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 24)
        }

        val title = TextView(ctx).apply {
            text = "選擇年月"
            textSize = 18f
            setPadding(0, 0, 0, 16)
        }

        val yearSpinner = Spinner(ctx)
        val monthSpinner = Spinner(ctx)

        val years = (2020..2035).toList()
        val months = (1..12).map { "${it}月" }

        val yearAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, years)
        val monthAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, months)

        yearSpinner.adapter = yearAdapter
        monthSpinner.adapter = monthAdapter

        // 設定目前年份、月份預設值
        yearSpinner.setSelection(years.indexOf(currentYear))
        monthSpinner.setSelection(currentMonth - 1)

        layout.addView(title)
        layout.addView(yearSpinner)
        layout.addView(monthSpinner)

        AlertDialog.Builder(ctx)
            .setView(layout)
            .setPositiveButton("確定") { _, _ ->
                val year = yearSpinner.selectedItem as Int
                val month = monthSpinner.selectedItemPosition + 1
                onMonthSelected(year, month)
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
