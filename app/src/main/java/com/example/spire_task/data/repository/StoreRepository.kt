package com.example.spire_task.data.repository

import com.example.spire_task.data.local.dao.StoreDao
import com.example.spire_task.data.local.entities.ProductEntity
import com.example.spire_task.data.local.entities.PurchaseEntity
import kotlinx.coroutines.flow.Flow

class StoreRepository(private val storeDao: StoreDao) {

    val allProducts: Flow<List<ProductEntity>> = storeDao.getAllProducts()

    fun getUserPurchases(userId: String): Flow<List<PurchaseEntity>> = storeDao.getUserPurchases(userId)

    fun getOwnedProducts(userId: String): Flow<List<ProductEntity>> = storeDao.getOwnedProducts(userId)

    fun getActivePet(userId: String): Flow<ProductEntity?> = storeDao.getActivePet(userId)

    suspend fun purchaseProduct(userId: String, productId: String) {
        val purchase = PurchaseEntity(
            idUser = userId,
            idProduct = productId,
            purchaseDate = System.currentTimeMillis()
        )
        storeDao.insertPurchase(purchase)
    }

    suspend fun activatePet(userId: String, productId: String) {
        storeDao.deactivateAllPets(userId)
        storeDao.activatePet(userId, productId)
    }

    suspend fun initStoreCatalog() {
        // Initial catalog for the store
        val initialProducts = listOf(
            ProductEntity("pet_cat", "Gato Cósmico", "Un compañero leal de las estrellas", 100, "PET", "animations/cat.json"),
            ProductEntity("pet_dog", "Perro Galáctico", "Siempre listo para una aventura espacial", 150, "PET", "animations/dog.json"),
            ProductEntity("acc_hat", "Sombrero Mágico", "Te da un aire de misterio", 50, "ACCESSORY", "images/hat.png")
        )
        storeDao.insertProducts(initialProducts)
    }
}
