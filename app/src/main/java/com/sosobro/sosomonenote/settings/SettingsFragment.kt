package com.sosobro.sosomonenote.ui.settings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.sosobro.sosomonenote.database.DatabaseInstance
import com.sosobro.sosomonenote.databinding.FragmentSettingsBinding
import com.sosobro.sosomonenote.ui.category.CategorySettingActivity
import com.sosobro.sosomonenote.ui.importdata.ImportDataActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        // ✅ 導入數據
        binding.btnImportData.setOnClickListener {
            val intent = Intent(requireContext(), ImportDataActivity::class.java)
            startActivity(intent)
        }

        // ✅ 分類設定
        binding.btnCateSettings.setOnClickListener {
            val intent = Intent(requireContext(), CategorySettingActivity::class.java)
            startActivity(intent)
        }

        // ✅ 刪除所有數據
        binding.root.findViewById<View>(com.sosobro.sosomonenote.R.id.deleteAllCard)
            .setOnClickListener {
                showDeleteConfirmDialog()
            }

        return binding.root
    }

    private fun showDeleteConfirmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("⚠️ 刪除所有資料")
            .setMessage("確定要刪除所有記帳資料、帳戶與分類嗎？此操作無法復原！")
            .setPositiveButton("確定刪除") { _, _ -> deleteAllData() }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteAllData() {
        lifecycleScope.launch {
            val db = DatabaseInstance.getDatabase(requireContext())
            try {
                withContext(Dispatchers.IO) {
                    db.transactionDao().deleteAll()
                    db.accountDao().deleteAll()
                    db.categoryDao().deleteAll()
                }
                Toast.makeText(requireContext(), "✅ 已清空所有資料", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "刪除失敗：${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
