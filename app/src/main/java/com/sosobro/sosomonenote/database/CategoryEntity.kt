package com.sosobro.sosomonenote.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String,  // ← 這個欄位會用在上面查詢
    val iconRes: Int? = null
)

