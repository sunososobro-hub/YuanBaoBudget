package com.sosobro.sosomonenote

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
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
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.sosobro.sosomonenote.database.DatabaseInstance
import com.sosobro.sosomonenote.databinding.ActivityMainBinding
import com.sosobro.sosomonenote.ui.record.RecordActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

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

        // ✅ 初始化 Google 登入
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // ✅ Navigation Header 控制項
        val headerView = binding.navView.getHeaderView(0)
        val imageView = headerView.findViewById<ImageView>(R.id.imageView)
        val nameView = headerView.findViewById<TextView>(R.id.textView)
        val emailView = headerView.findViewById<TextView>(R.id.textView2)
        val tipView = headerView.findViewById<TextView>(R.id.nav_header_title)

        // ✅ 自動顯示登入狀態
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            updateUserUI(account, imageView, nameView, emailView)
        }

        // ✅ 點擊頭像：登入 / 登出
        imageView.setOnClickListener {
            val currentAccount = GoogleSignIn.getLastSignedInAccount(this)
            val tipView = headerView.findViewById<TextView>(R.id.nav_header_title)
            if (currentAccount != null) {
                // 已登入 → 登出
                googleSignInClient.signOut().addOnCompleteListener {
                    nameView.text = "未登入"
                    emailView.text = ""
                    imageView.setImageResource(R.mipmap.ic_launcher_round)
                    tipView.text = "點擊頭像以登入"
                    Toast.makeText(this, "已登出", Toast.LENGTH_SHORT).show()
                }
            } else {
                // 未登入 → 登入
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }


        tipView.text = "點擊頭像以登出"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val headerView = binding.navView.getHeaderView(0)
            val imageView = headerView.findViewById<ImageView>(R.id.imageView)
            val nameView = headerView.findViewById<TextView>(R.id.textView)
            val emailView = headerView.findViewById<TextView>(R.id.textView2)
            val tipView = headerView.findViewById<TextView>(R.id.nav_header_title)

            updateUserUI(account, imageView, nameView, emailView)
            tipView.text = "點擊頭像以登出"
            Toast.makeText(this, "登入成功：${account.displayName}", Toast.LENGTH_SHORT).show()
        } catch (e: ApiException) {
            Toast.makeText(this, "登入失敗：${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUserUI(
        account: GoogleSignInAccount?,
        imageView: ImageView,
        nameView: TextView,
        emailView: TextView
    ) {
        nameView.text = account?.displayName ?: "訪客"
        emailView.text = account?.email ?: ""
        val photoUrl = account?.photoUrl
        if (photoUrl != null) {
            Glide.with(this)
                .load(photoUrl)
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round)
                .circleCrop()
                .into(imageView)
        } else {
            imageView.setImageResource(R.mipmap.ic_launcher_round)
        }
    }

    private fun showDeleteAllDialog() {
        AlertDialog.Builder(this)
            .setTitle("刪除所有資料")
            .setMessage("確定要刪除所有記帳資料嗎？此操作無法復原！")
            .setPositiveButton("刪除") { _, _ -> deleteAllDatabaseData() }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteAllDatabaseData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = DatabaseInstance.getDatabase(this@MainActivity)
            db.clearAllTables()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "✅ 已刪除所有資料", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
