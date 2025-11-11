package com.sosobro.sosomonenote.database

import androidx.room.*

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY id ASC")
    suspend fun getCategoriesByType(type: String): List<CategoryEntity>

    // 你原本應該已有的基本操作
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    @Query("SELECT * FROM categories")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE name = :name AND type = :type LIMIT 1")
    suspend fun findByNameAndType(name: String, type: String): CategoryEntity?

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

