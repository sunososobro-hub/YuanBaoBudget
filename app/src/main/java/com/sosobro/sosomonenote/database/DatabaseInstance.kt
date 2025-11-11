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

            // 🔹 定義 Migration（可擴充）
            // 若未提供 migration，Room 會根據 fallback 設定處理
            val builder = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "history_database"
            )
                // ✅ 開發用：若版本改變，直接清除重建（避免 Room crash）
                .fallbackToDestructiveMigration()

                // ✅ 可選：監聽資料庫建立完成（如預載資料）
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            // 這裡可以插入預設分類或帳戶
                            val dao = getDatabase(context).categoryDao()
                            // 範例：插入初始資料
                            // dao.insert(CategoryEntity(name = "未分類", type = "支出"))
                        }
                    }
                })

            val newInstance = builder.build()
            instance = newInstance
            newInstance
        }
    }
}
