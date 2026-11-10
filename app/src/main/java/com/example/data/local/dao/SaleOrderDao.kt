package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SaleOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleOrderDao {
    @Query("SELECT * FROM sale_orders ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSales(limit: Int): Flow<List<SaleOrder>>

    @Query("SELECT SUM(dueAmount) FROM sale_orders WHERE dueAmount > 0")
    fun getTotalUnpaidBaki(): Flow<Double?>

    @Query("SELECT * FROM sale_orders ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleOrder>>

    @Query("SELECT * FROM sale_orders ORDER BY timestamp DESC")
    fun getAllSalesSync(): List<SaleOrder>

    @Query("SELECT COUNT(*) FROM sale_orders")
    fun getSalesCount(): Int

    @Query("SELECT * FROM sale_orders WHERE customerId = :customerId AND dueAmount > 0 ORDER BY timestamp DESC")
    fun getCreditSalesForCustomer(customerId: Long): Flow<List<SaleOrder>>

    @Query("SELECT * FROM sale_orders WHERE customerId = :customerId AND dueAmount > 0 ORDER BY timestamp DESC")
    fun getCreditSalesForCustomerSync(customerId: Long): List<SaleOrder>

    @Query("SELECT * FROM sale_orders WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getSalesBetween(startTime: Long, endTime: Long): Flow<List<SaleOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(order: SaleOrder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sales: List<SaleOrder>)

    @Update
    suspend fun updateSale(order: SaleOrder)

    @Query("DELETE FROM sale_orders WHERE id = :saleId")
    suspend fun deleteSaleById(saleId: Long)

    @Query("DELETE FROM sale_orders")
    suspend fun clearAll()
}
