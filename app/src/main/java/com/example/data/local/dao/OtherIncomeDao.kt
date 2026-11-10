package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.OtherIncome
import kotlinx.coroutines.flow.Flow

@Dao
interface OtherIncomeDao {
    @Query("SELECT * FROM other_incomes ORDER BY timestamp DESC")
    fun getAllIncomes(): Flow<List<OtherIncome>>

    @Query("SELECT * FROM other_incomes ORDER BY timestamp DESC")
    fun getAllIncomesSync(): List<OtherIncome>

    @Query("SELECT COUNT(*) FROM other_incomes")
    fun getIncomeCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: OtherIncome): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(incomes: List<OtherIncome>)

    @Update
    suspend fun updateIncome(income: OtherIncome)

    @Delete
    suspend fun deleteIncome(income: OtherIncome)

    @Query("DELETE FROM other_incomes WHERE id = :id")
    suspend fun deleteIncomeById(id: Long)

    @Query("UPDATE other_incomes SET accountId = :toAccountId WHERE accountId = :fromAccountId")
    suspend fun relinkAccount(fromAccountId: Long, toAccountId: Long)

    @Query("DELETE FROM other_incomes")
    suspend fun clearAll()
}
