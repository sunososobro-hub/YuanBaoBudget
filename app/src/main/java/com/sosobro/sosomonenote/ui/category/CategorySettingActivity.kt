package com.sosobro.sosomonenote.ui.category

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.sosobro.sosomonenote.database.DatabaseInstance
import com.sosobro.sosomonenote.databinding.ActivityCataSettingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategorySettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCataSettingBinding
    private lateinit var adapterExpense: CategoryAdapter
    private lateinit var adapterIncome: CategoryAdapter
    private var currentType = "支出"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCataSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        // 點 ➕ 會依據目前類型跳轉
        binding.btnAddCategory.setOnClickListener {
            val intent = Intent(this, AddCategoryActivity::class.java)
            intent.putExtra("type", currentType)
            startActivityForResult(intent, 1001)
        }

        // 設定 TabLayout（支出 / 收入）
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("支出"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("收入"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentType = tab.text.toString()
                switchAdapter(currentType)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 初始化兩個 Adapter（支出、收入各自一份）
        adapterExpense = CategoryAdapter()
        adapterIncome = CategoryAdapter()

        binding.recyclerView.layoutManager = GridLayoutManager(this, 4)
        binding.recyclerView.adapter = adapterExpense // 預設支出

        // 載入兩邊的資料
        loadCategories("支出")
        loadCategories("收入")
    }

    private fun switchAdapter(type: String) {
        binding.recyclerView.adapter =
            if (type == "支出") adapterExpense else adapterIncome
    }

    private fun loadCategories(type: String) {
        lifecycleScope.launch {
            val db = DatabaseInstance.getDatabase(this@CategorySettingActivity)
            val categories = withContext(Dispatchers.IO) {
                db.categoryDao().getCategoriesByType(type)
            }
            if (type == "支出") adapterExpense.submitList(categories)
            else adapterIncome.submitList(categories)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            loadCategories(currentType)
        }
    }
}
