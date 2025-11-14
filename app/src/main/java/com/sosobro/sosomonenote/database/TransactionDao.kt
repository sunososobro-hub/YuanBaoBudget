package com.sosobro.sosomonenote.database

import androidx.room.*

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE date >= :date ORDER BY date ASC")
    suspend fun getTransactionsAfterDate(date: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    suspend fun getTransactionsByAccount(accountId: Int): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    suspend fun getTransactionsByType(type: String): List<TransactionEntity>

    @Query("SELECT t.date FROM transactions AS t ORDER BY t.date DESC LIMIT 1")
    suspend fun getLatestDate(): String?

    @Query("""
    SELECT * FROM transactions
    WHERE strftime('%Y', date) = :yearStr
      AND strftime('%m', date) = :monthStr
""")
    suspend fun getValidTransactionsForMonth(yearStr: String, monthStr: String): List<TransactionEntity>

    @Query("DELETE FROM transactions")
    suspend fun clearAll()

    @Query("SELECT SUM(amount) FROM transactions WHERE accountId = :accountId")
    suspend fun getTotalAmountByAccountId(accountId: Int): Double?

    @Query("SELECT * FROM transactions WHERE strftime('%m', date) = :month")
    suspend fun getTransactionsByMonth(month: String): List<TransactionEntity>

    @Query("DELETE FROM transactions WHERE accountId = :accountId")
    suspend fun deleteByAccountId(accountId: Int)

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Int): TransactionEntity?

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM transactions WHERE strftime('%Y', date) = :year AND strftime('%m', date) = printf('%02d', :month)")
    suspend fun getTransactionsForMonth(year: Int, month: Int): List<TransactionEntity>


    // ⭐⭐⭐ 你缺少的 → 我幫你補上
    @Query("SELECT * FROM transactions WHERE date < :date ORDER BY date DESC")
    suspend fun getTransactionsBeforeDate(date: String): List<TransactionEntity>
}

