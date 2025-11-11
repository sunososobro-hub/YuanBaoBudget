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

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    suspend fun getTransactionsByAccount(accountId: Int): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    suspend fun getTransactionsByType(type: String): List<TransactionEntity>

    @Query("DELETE FROM transactions")
    suspend fun clearAll()

    @Query("SELECT SUM(amount) FROM transactions WHERE accountId = :accountId")
    suspend fun getTotalAmountByAccountId(accountId: Int): Double?

    @Query("SELECT * FROM transactions WHERE strftime('%m', date) = :month")
    suspend fun getTransactionsByMonth(month: String): List<TransactionEntity>

    @Query("DELETE FROM transactions WHERE accountId = :accountId")
    suspend fun deleteByAccountId(accountId: Int)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Int): TransactionEntity?

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM transactions WHERE strftime('%Y', date) = :year AND strftime('%m', date) = printf('%02d', :month)")
    suspend fun getTransactionsForMonth(year: Int, month: Int): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE date < :date")
    suspend fun getTransactionsBeforeDate(date: String): List<TransactionEntity>
}
