package com.sosobro.sosomonenote.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val accountId: Int,
    val recordId: String? = null,
    val date: String,                 // ← 本來要存 yyyy-MM-dd
    val category: String,
    val subCategory: String? = null,
    val type: String,
    val amount: Double,
    val currency: String = "TWD",
    val note: String? = null,
    val book: String? = null,
    val tag: String? = null,
    val includeInBudget: Boolean = true,
    val time: Long,                   // ← 正確的交易日期
    val image: String? = null
)

