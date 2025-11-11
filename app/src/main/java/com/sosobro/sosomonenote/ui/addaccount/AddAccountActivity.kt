package com.sosobro.sosomonenote.ui.addaccount

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sosobro.sosomonenote.database.AccountEntity
import com.sosobro.sosomonenote.database.DatabaseInstance
import com.sosobro.sosomonenote.databinding.ActivityAddAccountBinding
import com.sosobro.sosomonenote.ui.accounttype.AccountTypeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAccountBinding
    private var editingAccountId: Int = -1   // 🔸 判斷是否為編輯模式

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = DatabaseInstance.getDatabase(this)
        val accountDao = db.accountDao()

        // 🟣 接收是否有帶入帳戶ID
        editingAccountId = intent.getIntExtra("accountId", -1)

        // 🔙 返回
        binding.btnBack.setOnClickListener { finish() }

        // ✏️ 如果是編輯模式 → 載入原資料
        if (editingAccountId != -1) {
            binding.tvTitle.text = "修改帳戶"

            lifecycleScope.launch(Dispatchers.IO) {
                val account = accountDao.getAccountById(editingAccountId)
                withContext(Dispatchers.Main) {
                    account?.let {
                        binding.etAccountName.setText(it.name)
                        binding.tvSelectAccountType.text = it.type
                        binding.etBalance.setText(it.balance.toString())
                        binding.etNote.setText(it.note ?: "")
                        binding.tvSelectAccountType.setTextColor(android.graphics.Color.parseColor("#4A3B2A"))
                    }
                }
            }
        } else {
            binding.tvTitle.text = "新增帳戶"
        }

        // 💾 儲存按鈕（新增或更新）
        binding.btnSave.setOnClickListener {
            val accountName = binding.etAccountName.text.toString()
            val accountType = binding.tvSelectAccountType.text.toString()
            val accountBalance = binding.etBalance.text.toString().toDoubleOrNull() ?: 0.0
            val note = binding.etNote.text.toString()

            if (accountName.isBlank() || accountType.isBlank()) {
                Toast.makeText(this, "請輸入帳戶名稱與選擇類型", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                if (editingAccountId == -1) {
                    // 🟢 新增帳戶
                    val newAccount = AccountEntity(
                        name = accountName,
                        type = accountType,
                        balance = accountBalance,
                        note = note
                    )
                    accountDao.insertAccount(newAccount)
                } else {
                    // 🟠 更新帳戶
                    val existing = accountDao.getAccountById(editingAccountId)
                    if (existing != null) {
                        val updatedAccount = existing.copy(
                            name = accountName,
                            type = accountType,
                            balance = accountBalance,
                            note = note
                        )
                        accountDao.update(updatedAccount)
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AddAccountActivity,
                        if (editingAccountId == -1) "帳戶已新增" else "帳戶已更新",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }

        // 🟣 點擊選擇帳戶類型 → 開啟選擇頁面
        binding.tvSelectAccountType.setOnClickListener {
            val intent = Intent(this, AccountTypeActivity::class.java)
            startActivityForResult(intent, 1001)
        }
    }

    // ✅ 接收從 AccountTypeActivity 回傳的資料
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            val selectedType = data?.getStringExtra("accountType")
            if (!selectedType.isNullOrEmpty()) {
                binding.tvSelectAccountType.text = selectedType
                binding.tvSelectAccountType.setTextColor(android.graphics.Color.parseColor("#4A3B2A"))
            }
        }
    }
}
