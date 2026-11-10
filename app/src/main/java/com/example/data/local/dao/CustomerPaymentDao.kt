package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CustomerPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerPaymentDao {
    @Query("SELECT * FROM customer_payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<CustomerPayment>>

    @Query("SELECT * FROM customer_payments ORDER BY timestamp DESC")
    fun getAllPaymentsSync(): List<CustomerPayment>

    @Query("SELECT * FROM customer_payments WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getPaymentsForCustomer(customerId: Long): Flow<List<CustomerPayment>>

    @Query("SELECT * FROM customer_payments WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getPaymentsForCustomerSync(customerId: Long): List<CustomerPayment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: CustomerPayment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payments: List<CustomerPayment>)

    @Update
    suspend fun updatePayment(payment: CustomerPayment)

    @Query("DELETE FROM customer_payments WHERE id = :paymentId")
    suspend fun deletePaymentById(paymentId: Long)

    @Query("DELETE FROM customer_payments")
    suspend fun clearAll()
}
