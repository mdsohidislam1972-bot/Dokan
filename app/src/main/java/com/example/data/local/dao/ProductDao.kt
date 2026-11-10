package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isDeleted = 0 AND stockQuantity <= lowStockThreshold ORDER BY stockQuantity ASC LIMIT :limit")
    fun getLowStockProducts(limit: Int): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isDeleted = 0 ORDER BY id DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isDeleted = 0 ORDER BY id DESC")
    fun getAllProductsSync(): List<Product>

    @Query("SELECT COUNT(*) FROM products WHERE isDeleted = 0")
    fun getProductCount(): Int

    @Query("""
        SELECT * FROM products 
        WHERE isDeleted = 0 
        AND (:category IS NULL OR :category = '' OR category = :category)
        AND (:query = '' OR nameBn LIKE '%' || :query || '%' OR nameEn LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%')
        ORDER BY id DESC
    """)
    fun filterProducts(query: String, category: String?): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET isDeleted = 1 WHERE id = :productId")
    suspend fun softDeleteProduct(productId: Long)

    @Query("UPDATE products SET stockQuantity = stockQuantity + :addedQty, buyPrice = :unitBuyingPrice WHERE id = :productId")
    suspend fun addStock(productId: Long, addedQty: Int, unitBuyingPrice: Double)

    @Query("UPDATE products SET stockQuantity = stockQuantity - :qty WHERE id = :productId")
    suspend fun reduceStock(productId: Long, qty: Int)

    @Query("UPDATE products SET stockQuantity = :newStock WHERE id = :productId")
    suspend fun updateStockQuantity(productId: Long, newStock: Int)

    @Query("DELETE FROM products")
    suspend fun clearAll()
}
