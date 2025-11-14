package com.sosobro.sosomonenote.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    var type: String,
    var balance: Double,
    var sortOrder: Int = 0,
    val note: String? = null // ✅ 新增這行
)

