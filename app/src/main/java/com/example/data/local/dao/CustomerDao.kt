package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY totalBaki DESC, lastTransactionTime DESC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers ORDER BY id ASC")
    fun getAllCustomersSync(): List<Customer>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    fun getCustomerById(id: Long): Flow<Customer?>

    @Query("SELECT COUNT(*) FROM customers")
    fun getCustomerCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<Customer>)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("UPDATE customers SET note = :note WHERE id = :id")
    suspend fun updateCustomerNote(id: Long, note: String)

    @Query("UPDATE customers SET dueDate = :dueDate WHERE id = :id")
    suspend fun updateCustomerDueDate(id: Long, dueDate: Long?)

    @Query("UPDATE customers SET totalBaki = :newBalance WHERE id = :customerId")
    suspend fun updateCustomerBalance(customerId: Long, newBalance: Double)

    @Query("UPDATE customers SET totalBaki = totalBaki + :amount, lastTransactionTime = :timestamp WHERE id = :customerId")
    suspend fun addBaki(customerId: Long, amount: Double, timestamp: Long)

    @Query("DELETE FROM customers")
    suspend fun clearAll()
}
