package com.sosobro.sosomonenote.ui.budget

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.sosobro.sosomonenote.R
import com.sosobro.sosomonenote.database.DatabaseInstance
import com.sosobro.sosomonenote.database.BudgetEntity
import com.sosobro.sosomonenote.databinding.ActivityAddBudgetBinding
import com.sosobro.sosomonenote.ui.category.IconAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddBudgetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBudgetBinding
    private lateinit var adapter: IconAdapter
    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 返回按鈕
        binding.btnBack.setOnClickListener { finish() }

        // 類別列表
        val categories = listOf("餐飲", "交通", "娛樂", "房租", "購物")
        val icons = listOf(
            R.drawable.ic_food, R.drawable.ic_car, R.drawable.ic_interest,
            R.drawable.ic_home, R.drawable.ic_shopping
        )

        adapter = IconAdapter(icons) { iconRes ->
            val index = icons.indexOf(iconRes)
            selectedCategory = categories[index]
            Toast.makeText(this, "已選擇分類：${selectedCategory}", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerCategories.layoutManager = GridLayoutManager(this, 4)
        binding.recyclerCategories.adapter = adapter

        // 儲存預算
        binding.btnSaveBudget.setOnClickListener {
            val amountText = binding.etBudgetAmount.text.toString()
            val amount = amountText.toDoubleOrNull()

            if (selectedCategory == null) {
                Toast.makeText(this, "請選擇分類", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amount == null || amount <= 0) {
                Toast.makeText(this, "請輸入有效金額", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val db = DatabaseInstance.getDatabase(this@AddBudgetActivity)
                val newBudget = BudgetEntity(
                    category = selectedCategory!!,
                    amount = amount,
                    spent = 0.0
                )
                db.budgetDao().insert(newBudget)
                runOnUiThread {
                    Toast.makeText(this@AddBudgetActivity, "預算新增成功", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
