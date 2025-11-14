package com.sosobro.sosomonenote.ui.report

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.sosobro.sosomonenote.databinding.FragmentReportBinding
import com.sosobro.sosomonenote.ui.transaction.TransactionGroupAdapter
import com.sosobro.sosomonenote.ui.transaction.TransactionDetailActivity
import java.util.*

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportViewModel by viewModels()
    private lateinit var adapter: TransactionGroupAdapter
    private val calendar = Calendar.getInstance()

    // ✅ 用新版 Activity Result API 取代 onActivityResult()
    private val transactionDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                reloadCurrentMonth()
            }
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
        // ✅ 傳入交易點擊事件
        adapter = TransactionGroupAdapter { txn ->
            val intent = Intent(requireContext(), TransactionDetailActivity::class.java)
            intent.putExtra("transactionId", txn.id)
            transactionDetailLauncher.launch(intent)
        }

        binding.recyclerViewReport.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewReport.adapter = adapter

        viewModel.transactionsByDate.observe(viewLifecycleOwner) {
            adapter.submitData(it)
        }

        viewModel.monthlySummary.observe(viewLifecycleOwner) { summary ->
            binding.tvMonth.text = "${calendar.get(Calendar.MONTH) + 1}月"
            binding.tvExpense.text = "月支出：NT$${"%,.0f".format(summary.first)}"
            binding.tvIncome.text = "月收入：NT$${"%,.0f".format(summary.second)}"
            binding.tvBalance.text = "月結餘：NT$${"%,.0f".format(summary.third)}"
        }

        reloadCurrentMonth()
    }

    private fun reloadCurrentMonth() {
        viewModel.loadMonthData(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}