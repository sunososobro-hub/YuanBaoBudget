package com.sosobro.sosomonenote.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // 🔹 分類名稱，例如「餐飲」「交通」
    val category: String,

    // 🔹 預算金額（設定的上限）
    val amount: Double,

    // 🔹 已支出金額
    val spent: Double = 0.0,

    // 🔹 幣別（預設 TWD）
    val currency: String = "TWD",

    // 🔹 所屬帳本（可選）
    val book: String? = null,

    // 🔹 預算起始與結束日期（可擴充月度、季度等）
    val startDate: String? = null,
    val endDate: String? = null,

    // 🔹 備註
    val note: String? = null
)
