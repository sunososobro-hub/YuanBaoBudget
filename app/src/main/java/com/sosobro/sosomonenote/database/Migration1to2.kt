package com.sosobro.sosomonenote.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {

        // ➊ 新增 time 欄位（如果不存在）
        database.execSQL(
            "ALTER TABLE transactions ADD COLUMN time INTEGER NOT NULL DEFAULT 0"
        )

        // ➋ 用 date 補上 time（毫秒 timestamp）
        database.execSQL(
            """
            UPDATE transactions
            SET time = strftime('%s', date) * 1000
            WHERE time = 0 OR time IS NULL
            """
        )
    }
}
