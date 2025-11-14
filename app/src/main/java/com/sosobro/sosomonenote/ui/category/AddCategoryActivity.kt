package com.sosobro.sosomonenote.ui.category

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.sosobro.sosomonenote.R
import com.sosobro.sosomonenote.database.CategoryEntity
import com.sosobro.sosomonenote.database.DatabaseInstance
import com.sosobro.sosomonenote.databinding.ActivityAddCategoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCategoryBinding
    private lateinit var adapter: IconAdapter
    private var currentType = "支出"
    private var selectedIcon: Int? = null

    private val expenseIcons = listOf(
        R.drawable.ic_airplay,
        R.drawable.ic_air_vent,
        R.drawable.ic_alarm_clock_minus,
        R.drawable.ic_alarm_clock_plus,
        R.drawable.ic_align_vertical_distribute_end,
        R.drawable.ic_align_vertical_distribute_start,
        R.drawable.ic_ambulance,
        R.drawable.ic_archive,
        R.drawable.ic_armchair,
        R.drawable.ic_badge_percent,
        R.drawable.ic_baggage_claim,
        R.drawable.ic_bike,
        R.drawable.ic_binoculars,
        R.drawable.ic_bird,
        R.drawable.ic_bluetooth,
        R.drawable.ic_bone,
        R.drawable.ic_book,
        R.drawable.ic_book_open,
        R.drawable.ic_bookmark_minus,
        R.drawable.ic_bookmark_plus,
        R.drawable.ic_box,
        R.drawable.ic_brush,
        R.drawable.ic_building,
        R.drawable.ic_bus,
        R.drawable.ic_bus_front,
        R.drawable.ic_cake,
        R.drawable.ic_camera,
        R.drawable.ic_car,
        R.drawable.ic_car_front,
        R.drawable.ic_castle,
        R.drawable.ic_cat,
        R.drawable.ic_chart_bar_big,
        R.drawable.ic_chart_bar_decreasing,
        R.drawable.ic_chart_bar_increasing,
        R.drawable.ic_chart_candlestick,
        R.drawable.ic_chart_column_big,
        R.drawable.ic_chart_line,
        R.drawable.ic_chart_scatter,
        R.drawable.ic_cherry,
        R.drawable.ic_clapperboard,
        R.drawable.ic_cloud_rain,
        R.drawable.ic_cloud_snow,
        R.drawable.ic_coffee,
        R.drawable.ic_cooking_pot,
        R.drawable.ic_crosshair,
        R.drawable.ic_dog,
        R.drawable.ic_drink,
        R.drawable.ic_dumbbell,
        R.drawable.ic_factory,
        R.drawable.ic_fan,
        R.drawable.ic_film,
        R.drawable.ic_fire_extinguisher,
        R.drawable.ic_fish,
        R.drawable.ic_flame,
        R.drawable.ic_flower,
        R.drawable.ic_fuel,
        R.drawable.ic_gamepad,
        R.drawable.ic_gift,
        R.drawable.ic_globe,
        R.drawable.ic_grape,
        R.drawable.ic_guitar,
        R.drawable.ic_hammer,
        R.drawable.ic_handshake,
        R.drawable.ic_hard_drive,
        R.drawable.ic_headphones,
        R.drawable.ic_heart,
        R.drawable.ic_heart_pulse,
        R.drawable.ic_highlighter,
        R.drawable.ic_home,
        R.drawable.ic_laptop,
        R.drawable.ic_leaf,
        R.drawable.ic_map
    )

    private val incomeIcons = listOf(
        R.drawable.ic_award,
        R.drawable.ic_bitcoin,
        R.drawable.ic_cash,
        R.drawable.ic_chart_area,
        R.drawable.ic_chart_bar,
        R.drawable.ic_chart_bar_big,
        R.drawable.ic_chart_bar_increasing,
        R.drawable.ic_chart_bar_decreasing,
        R.drawable.ic_chart_pie,
        R.drawable.ic_chart_scatter,
        R.drawable.ic_chart_line,
        R.drawable.ic_chart_column_big,
        R.drawable.ic_coins,
        R.drawable.ic_diamond,
        R.drawable.ic_gem,
        R.drawable.ic_hand_coins,
        R.drawable.ic_interest,
        R.drawable.ic_medal,
        R.drawable.ic_piggy_bank,
        R.drawable.ic_bonus,
        R.drawable.ic_invest,
        R.drawable.ic_salary,
        R.drawable.ic_scale,
        R.drawable.ic_trophy,
        R.drawable.ic_wallet
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("支出"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("收入"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentType = tab.text.toString()
                loadIcons()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        adapter = IconAdapter(emptyList()) { iconRes ->
            selectedIcon = iconRes
            binding.ivSelectedIcon.setImageResource(iconRes)
        }
        binding.recyclerIcons.layoutManager = GridLayoutManager(this, 5)
        binding.recyclerIcons.adapter = adapter

        loadIcons()

        binding.btnSave.setOnClickListener {
            val name = binding.etCategoryName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "請輸入分類名稱", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedIcon == null) {
                Toast.makeText(this, "請選擇圖示", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val db = DatabaseInstance.getDatabase(this@AddCategoryActivity)
                val dao = db.categoryDao()

                val exists = withContext(Dispatchers.IO) {
                    dao.findByNameAndType(name, currentType)
                }
                if (exists != null) {
                    Toast.makeText(this@AddCategoryActivity, "分類已存在", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val category = CategoryEntity(
                    name = name,
                    type = currentType,
                    iconRes = selectedIcon
                )

                withContext(Dispatchers.IO) { dao.insert(category) }

                Toast.makeText(this@AddCategoryActivity, "分類已新增", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun loadIcons() {
        val icons = if (currentType == "支出") expenseIcons else incomeIcons
        adapter.submitList(icons)
    }
}
