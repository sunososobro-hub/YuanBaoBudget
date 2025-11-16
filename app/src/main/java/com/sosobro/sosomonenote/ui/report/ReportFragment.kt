package com.sosobro.sosomonenote.ui.report

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.sosobro.sosomonenote.R
import com.sosobro.sosomonenote.databinding.FragmentReportBinding
import com.sosobro.sosomonenote.ui.transaction.TransactionDetailActivity
import com.sosobro.sosomonenote.ui.transaction.TransactionGroupAdapter
import java.util.*

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportViewModel by viewModels()
    private lateinit var adapter: TransactionGroupAdapter

    private val calendar = Calendar.getInstance()

    private val transactionDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) reloadCurrentMonth()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // ⭐ 正確取用 XML 的 Capsule（不用 root 也不用 include）
        val filterMonth      = binding.filterMonth
        val filterType       = binding.filterType
        val filterCategory   = binding.filterCategory
        val filterAccountBook= binding.filterAccountBook
        val filterAccount    = binding.filterAccount
        val filterTag        = binding.filterTag

        val tvFilterMonth    = binding.tvFilterNameMonth

        // ⭐ 月份顯示（初始值）
        tvFilterMonth.text = "${calendar.get(Calendar.MONTH) + 1}月"

        // ⭐ Month Capsule 點擊 → 打開日期選擇器
        filterMonth.setOnClickListener {
            openMonthPickerManual()
        }

        // ⭐ 其他 Filter
        filterType.setOnClickListener { openFilterDialog("類型") }
        filterCategory.setOnClickListener { openFilterDialog("類別") }
        filterAccountBook.setOnClickListener { openFilterDialog("帳本") }
        filterAccount.setOnClickListener { openFilterDialog("帳戶") }
        filterTag.setOnClickListener { openFilterDialog("標籤") }


        // ⭐ RecyclerView
        adapter = TransactionGroupAdapter { txn ->
            val intent = Intent(requireContext(), TransactionDetailActivity::class.java)
            intent.putExtra("transactionId", txn.id)
            transactionDetailLauncher.launch(intent)
        }

        binding.recyclerViewReport.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewReport.adapter = adapter

        // ⭐ ViewModel 監聽
        viewModel.transactionsByDate.observe(viewLifecycleOwner) {
            adapter.submitData(it)
        }

        reloadCurrentMonth() // 初始載入
    }

    private fun openMonthPickerManual() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.month_picker_dialog, null)
        dialog.setContentView(view)

        val yearPicker = view.findViewById<NumberPicker>(R.id.npYear)
        val monthPicker = view.findViewById<NumberPicker>(R.id.npMonth)
        val btnCancel = view.findViewById<TextView>(R.id.btnCancel)
        val btnConfirm = view.findViewById<TextView>(R.id.btnConfirm)

        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1

        yearPicker.minValue = currentYear - 10
        yearPicker.maxValue = currentYear + 10
        yearPicker.value = currentYear

        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value = currentMonth

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            val year = yearPicker.value
            val month = monthPicker.value

            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month - 1)

            binding.tvFilterNameMonth.text = "${month}月"
            viewModel.loadMonthData(year, month)

            dialog.dismiss()
        }

        dialog.show()
    }

    // --------------------------------------------------------
    // ⭐ FILTER 彈窗
    // --------------------------------------------------------
    private fun openFilterDialog(type: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("選擇：$type")
            .setMessage("未來會加入 $type 選項")
            .setPositiveButton("OK", null)
            .show()
    }


    // --------------------------------------------------------
    // ⭐ 載入該月份資料
    // --------------------------------------------------------
    private fun reloadCurrentMonth() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        viewModel.loadMonthData(year, month)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
