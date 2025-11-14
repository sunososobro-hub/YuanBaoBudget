package com.sosobro.sosomonenote.ui.budget

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.sosobro.sosomonenote.R

class BudgetSettingActivity : AppCompatActivity() {

    private lateinit var tvBudget: TextView
    private lateinit var tvRemain: TextView
    private lateinit var tvPercent: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_setting)

        val budgetType = intent.getStringExtra("BUDGET_TYPE") ?: "month"

        // 綁定元件
        tvBudget = findViewById(R.id.tvBudget)
        tvRemain = findViewById(R.id.tvRemain)
        tvPercent = findViewById(R.id.tvProgressPercent)
        progressBar = findViewById(R.id.progressBudget)

        // 🔙 返回
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // ➕ 新增預算
        findViewById<ImageView>(R.id.btnAddBudget).setOnClickListener {
            val intent = Intent(this, AddBudgetActivity::class.java)
            startActivity(intent)
        }

        // ✅ 下拉選單設定
        val spinner = findViewById<Spinner>(R.id.spinnerPeriod)
        val periodOptions = listOf("月度預算", "雙月預算", "季度預算", "年度預算")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, periodOptions)
        spinner.adapter = adapter

        val defaultIndex = when (budgetType) {
            "bimonth" -> 1
            "quarter" -> 2
            "year" -> 3
            else -> 0
        }
        spinner.setSelection(defaultIndex)

        // 初始載入對應預算
        loadBudgetData(periodOptions[defaultIndex])

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val selected = periodOptions[position]
                Toast.makeText(this@BudgetSettingActivity, "切換為：$selected", Toast.LENGTH_SHORT).show()
                loadBudgetData(selected)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadBudgetData(type: String) {
        // 模擬不同預算資料（可改為讀取資料庫或 API）
        val (budget, remain) = when (type) {
            "月度預算" -> Pair(20000, 8500)
            "雙月預算" -> Pair(40000, 23000)
            "季度預算" -> Pair(60000, 39000)
            "年度預算" -> Pair(240000, 180000)
            else -> Pair(0, 0)
        }

        val used = budget - remain
        val percent = if (budget == 0) 0 else (used * 100 / budget)

        // 更新畫面
        tvBudget.text = budget.toString()
        tvRemain.text = remain.toString()
        tvPercent.text = "$percent%"
        progressBar.progress = percent
    }
}
