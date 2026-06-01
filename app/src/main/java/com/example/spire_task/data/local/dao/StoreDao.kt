package com.example.spire_task.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.spire_task.data.local.entities.ProductEntity
import com.example.spire_task.data.local.entities.PurchaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    @Query("SELECT * FROM tblProductos")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM tblProductos WHERE idProduct = :productId LIMIT 1")
    suspend fun getProductById(productId: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity)

    @Query("SELECT * FROM tblCompras WHERE idUser = :userId")
    fun getUserPurchases(userId: String): Flow<List<PurchaseEntity>>

    @Transaction
    @Query("SELECT * FROM tblProductos WHERE idProduct IN (SELECT idProduct FROM tblCompras WHERE idUser = :userId)")
    fun getOwnedProducts(userId: String): Flow<List<ProductEntity>>

    @Query("UPDATE tblCompras SET isActive = 0 WHERE idUser = :userId AND idProduct IN (SELECT idProduct FROM tblProductos WHERE type = 'PET')")
    suspend fun deactivateAllPets(userId: String)

    @Query("UPDATE tblCompras SET isActive = 1 WHERE idUser = :userId AND idProduct = :productId")
    suspend fun activatePet(userId: String, productId: String)

    @Query("SELECT * FROM tblProductos WHERE idProduct IN (SELECT idProduct FROM tblCompras WHERE idUser = :userId AND isActive = 1) LIMIT 1")
    fun getActivePet(userId: String): Flow<ProductEntity?>
}
