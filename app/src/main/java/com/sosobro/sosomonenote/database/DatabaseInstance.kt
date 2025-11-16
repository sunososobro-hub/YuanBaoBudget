package com.sosobro.sosomonenote.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ⭐ 全版本適用的 Migration（1 → 18）
val MIGRATION_ALL = object : Migration(1, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE accounts 
            ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0
            """
        )
    }
}

object DatabaseInstance {

    @Volatile
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {

            // 再次檢查，避免多執行緒重複建立
            instance ?: buildDatabase(context).also { instance = it }
        }
    }

    private fun buildDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "main_database"
        )
            .addMigrations(MIGRATION_ALL)
            .addCallback(object : RoomDatabase.Callback() {

                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    // ⭐ 若在這裡直接呼叫 getDatabase 會造成循環！！
                    // 因此使用 instance（已建立）
                    instance?.let { database ->

                        CoroutineScope(Dispatchers.IO).launch {

                            val dao = database.categoryDao()

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
                }
            })
            .build()
    }
}
