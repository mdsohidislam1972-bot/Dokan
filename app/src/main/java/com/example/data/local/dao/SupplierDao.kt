package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.Supplier
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY amountOwed DESC, createdAt DESC")
    fun getAllSuppliersOwedFirst(): Flow<List<Supplier>>

    @Query("SELECT SUM(amountOwed) FROM suppliers WHERE amountOwed > 0")
    fun getTotalAmountOwed(): Flow<Double?>

    @Query("SELECT * FROM suppliers WHERE id = :id LIMIT 1")
    fun getSupplierById(id: Long): Flow<Supplier?>

    @Query("SELECT * FROM suppliers ORDER BY id ASC")
    fun getAllSuppliersSync(): List<Supplier>

    @Query("SELECT COUNT(*) FROM suppliers")
    fun getSupplierCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(suppliers: List<Supplier>)

    @Update
    suspend fun updateSupplier(supplier: Supplier)

    @Delete
    suspend fun deleteSupplier(supplier: Supplier)

    @Query("UPDATE suppliers SET amountOwed = amountOwed + :amount WHERE id = :supplierId")
    suspend fun adjustAmountOwed(supplierId: Long, amount: Double)

    @Query("UPDATE suppliers SET amountOwed = :newBalance WHERE id = :supplierId")
    suspend fun updateSupplierBalance(supplierId: Long, newBalance: Double)

    @Query("DELETE FROM suppliers")
    suspend fun clearAll()
}
