package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.SupplierPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierPaymentDao {
    @Query("SELECT * FROM supplier_payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<SupplierPayment>>

    @Query("SELECT * FROM supplier_payments ORDER BY timestamp DESC")
    fun getAllPaymentsSync(): List<SupplierPayment>

    @Query("SELECT * FROM supplier_payments WHERE supplierId = :supplierId ORDER BY timestamp DESC")
    fun getPaymentsForSupplier(supplierId: Long): Flow<List<SupplierPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: SupplierPayment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payments: List<SupplierPayment>)

    @Query("DELETE FROM supplier_payments")
    suspend fun clearAll()
}
