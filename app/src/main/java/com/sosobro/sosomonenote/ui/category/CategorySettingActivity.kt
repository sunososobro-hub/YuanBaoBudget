package com.sosobro.sosomonenote.ui.category

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.sosobro.sosomonenote.R
import com.sosobro.sosomonenote.database.CategoryEntity
import com.sosobro.sosomonenote.database.DatabaseInstance
import com.sosobro.sosomonenote.databinding.ActivityCataSettingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope

class CategorySettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCataSettingBinding
    private lateinit var adapter: CategoryAdapter
    private var currentType = "支出"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCataSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔙 返回
        binding.btnBack.setOnClickListener { finish() }

        // ➕ 新增分類（未實作功能時先 Toast）
        binding.btnAddCategory.setOnClickListener {
            Toast.makeText(this, "新增分類功能開發中", Toast.LENGTH_SHORT).show()
        }

        // 🔹 Tab 切換（支出 / 收入）
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("支出"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("收入"))
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentType = tab.text.toString()
                loadCategories()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 🔹 RecyclerView
        adapter = CategoryAdapter()
        binding.recyclerView.layoutManager = GridLayoutManager(this, 4)
        binding.recyclerView.adapter = adapter

        // 初始載入支出類別
        loadCategories()
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val db = DatabaseInstance.getDatabase(this@CategorySettingActivity)
            val categories = withContext(Dispatchers.IO) {
                db.categoryDao().getCategoriesByType(currentType)
            }

            adapter.submitList(categories)
        }
    }
}
