package com.sosobro.sosomonenote

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.sosobro.sosomonenote.database.DatabaseInstance
import com.sosobro.sosomonenote.databinding.ActivityMainBinding
import com.sosobro.sosomonenote.ui.record.RecordActivity
import com.sosobro.sosomonenote.ui.record.RecordBottomSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Toolbar
        setSupportActionBar(binding.appBarMain.toolbar)

        // ✅ Navigation 設定
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_overview,
                R.id.nav_account,
                R.id.nav_analysis,
                R.id.nav_report,
            ),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        binding.appBarMain.bottomNav.setupWithNavController(navController)

        // ✅ BottomNavigationView 點擊
        binding.appBarMain.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_overview -> {
                    navController.navigate(R.id.nav_overview)
                    true
                }
                R.id.nav_account -> {
                    navController.navigate(R.id.nav_account)
                    true
                }
                R.id.nav_analysis -> {
                    navController.navigate(R.id.nav_analysis)
                    true
                }
                R.id.nav_report -> {
                    navController.navigate(R.id.nav_report)
                    true
                }
                else -> false
            }
        }

        // ✅ 中間浮動按鈕（➕）
        val fabAdd: FloatingActionButton = findViewById(R.id.fab_add)
        fabAdd.setOnClickListener {
            val intent = Intent(this, RecordActivity::class.java)
            startActivity(intent)
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_delete_all -> {
                    showDeleteAllDialog()
                    true
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(menuItem, navController)
                    binding.drawerLayout.closeDrawers()
                    true
                }
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun showDeleteAllDialog() {
        AlertDialog.Builder(this)
            .setTitle("刪除所有資料")
            .setMessage("確定要刪除所有記帳資料嗎？此操作無法復原！")
            .setPositiveButton("刪除") { _, _ ->
                deleteAllDatabaseData()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteAllDatabaseData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = DatabaseInstance.getDatabase(this@MainActivity)
            db.clearAllTables() // ✅ 一次清空所有資料表
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "✅ 已刪除所有資料", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
