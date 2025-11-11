package com.sosobro.sosomonenote.ui.accounttype

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.sosobro.sosomonenote.R
import com.sosobro.sosomonenote.databinding.ActivityAccountTypeBinding

class AccountTypeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountTypeBinding
    private lateinit var adapterSaving: AccountTypeAdapter
    private lateinit var adapterCredit: AccountTypeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔙 返回鍵
        binding.btnBack.setOnClickListener { finish() }

        // 🪙 模擬帳戶資料
        val savingAccounts = listOf(
            AccountTypeItem("街口支付", R.drawable.ic_menu_camera),
            AccountTypeItem("iPass 一卡通", R.drawable.ic_menu_camera),
            AccountTypeItem("Line Pay", R.drawable.ic_menu_camera),
            AccountTypeItem("支付寶", R.drawable.ic_menu_camera),
            AccountTypeItem("WeChat Pay", R.drawable.ic_menu_camera),
            AccountTypeItem("悠遊卡", R.drawable.ic_menu_camera),
            AccountTypeItem("八達通", R.drawable.ic_menu_camera),
            AccountTypeItem("八達通錢包", R.drawable.ic_menu_camera),
            AccountTypeItem("icash", R.drawable.ic_menu_camera),
            AccountTypeItem("現金", R.drawable.ic_menu_camera),
            AccountTypeItem("儲蓄卡", R.drawable.ic_menu_camera),
            AccountTypeItem("其他", R.drawable.ic_menu_camera)
        )

        val creditAccounts = listOf(
            AccountTypeItem("欠款", R.drawable.ic_menu_camera),
            AccountTypeItem("信用卡", R.drawable.ic_menu_camera),
            AccountTypeItem("其他", R.drawable.ic_menu_camera)
        )

        // ✅ 儲蓄帳戶 Adapter
        adapterSaving = AccountTypeAdapter(savingAccounts) { selectedItem ->
            returnSelection(selectedItem.name)
        }

        // ✅ 信用帳戶 Adapter
        adapterCredit = AccountTypeAdapter(creditAccounts) { selectedItem ->
            returnSelection(selectedItem.name)
        }

        // 🔹 設定 RecyclerView
        binding.recyclerSaving.apply {
            layoutManager = GridLayoutManager(this@AccountTypeActivity, 2)
            adapter = adapterSaving
        }

        binding.recyclerCredit.apply {
            layoutManager = GridLayoutManager(this@AccountTypeActivity, 2)
            adapter = adapterCredit
        }
    }

    // ✅ 點選回傳結果
    private fun returnSelection(accountName: String) {
        val resultIntent = Intent()
        resultIntent.putExtra("accountType", accountName)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
