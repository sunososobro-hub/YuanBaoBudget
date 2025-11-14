package com.sosobro.sosomonenote.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseInstance {

    @Volatile
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {

            val newInstance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "main_database"
            )
                .addMigrations(MIGRATION_1_2) // ✅ 使用正確 Migration
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)

                        // ⭐ 初始化預設分類
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).categoryDao()

                            val defaultCategories = listOf(
                                CategoryEntity(name = "食物", type = "支出"),
                                CategoryEntity(name = "交通", type = "支出"),
                                CategoryEntity(name = "娛樂", type = "支出"),
                                CategoryEntity(name = "購物", type = "支出"),
                                CategoryEntity(name = "醫療", type = "支出"),
                                CategoryEntity(name = "薪資", type = "收入"),
                                CategoryEntity(name = "投資獲利", type = "收入"),
                                CategoryEntity(name = "轉帳", type = "轉帳"),
                                CategoryEntity(name = "未分類", type = "支出")
                            )

                            dao.insertAll(defaultCategories)
                        }
                    }
                })
                .build()

            instance = newInstance
            newInstance
        }
    }
}
