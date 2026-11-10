package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.BankAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface BankAccountDao {
    @Query("SELECT * FROM bank_accounts ORDER BY id ASC")
    fun getAllAccounts(): Flow<List<BankAccount>>

    @Query("SELECT * FROM bank_accounts ORDER BY id ASC")
    fun getAllAccountsSync(): List<BankAccount>

    @Query("SELECT * FROM bank_accounts WHERE id = :id LIMIT 1")
    fun getAccountById(id: Long): Flow<BankAccount?>

    @Query("SELECT * FROM bank_accounts WHERE id = :id LIMIT 1")
    fun getAccountByIdSync(id: Long): BankAccount?

    @Query("SELECT COUNT(*) FROM bank_accounts")
    fun getAccountCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: BankAccount): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<BankAccount>)

    @Update
    suspend fun updateAccount(account: BankAccount)

    @Query("UPDATE bank_accounts SET currentBalance = :newBalance, lastUpdated = :timestamp WHERE id = :id")
    suspend fun adjustBalance(id: Long, newBalance: Double, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE bank_accounts SET isDefault = 0 WHERE id != :exceptId")
    suspend fun clearOtherDefaults(exceptId: Long)

    @Query("UPDATE bank_accounts SET isDefault = 0")
    suspend fun clearAllDefaults()

    @Query("UPDATE bank_accounts SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultAccount(id: Long)

    @Delete
    suspend fun deleteAccount(account: BankAccount)

    @Query("DELETE FROM bank_accounts")
    suspend fun clearAll()
}
