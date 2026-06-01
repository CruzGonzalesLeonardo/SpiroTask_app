package com.example.spire_task.data.repository

import com.example.spire_task.data.local.dao.ProfileDao
import com.example.spire_task.data.local.dao.StoreDao
import com.example.spire_task.data.local.entities.ProductEntity
import com.example.spire_task.data.local.entities.PurchaseEntity
import kotlinx.coroutines.flow.Flow

class StoreRepository(
    private val storeDao: StoreDao,
    private val profileDao: ProfileDao
) {

    val allProducts: Flow<List<ProductEntity>> = storeDao.getAllProducts()

    fun getUserPurchases(userId: String): Flow<List<PurchaseEntity>> = storeDao.getUserPurchases(userId)

    fun getOwnedProducts(userId: String): Flow<List<ProductEntity>> = storeDao.getOwnedProducts(userId)

    fun getActivePet(userId: String): Flow<ProductEntity?> = storeDao.getActivePet(userId)

    suspend fun purchaseProduct(userId: String, productId: String): Result<Unit> {
        // Intentar obtener el usuario por ID, si falla intentar obtener el usuario invitado/local
        val user = profileDao.obtenerPorId(userId) 
            ?: profileDao.obtenerUsuarioInvitado() 
            ?: return Result.failure(Exception("Usuario no encontrado en la base de datos"))
            
        val product = storeDao.getProductById(productId) ?: return Result.failure(Exception("Producto no encontrado"))

        if (user.monedas < product.price) {
            return Result.failure(Exception("Monedas insuficientes. ¡Sigue completando tareas para ganar más!"))
        }

        try {
            // Deducir monedas usando el ID real encontrado
            val realUserId = user.idUser
            profileDao.actualizarMonedas(realUserId, user.monedas - product.price)
            
            // Registrar compra
            val purchase = PurchaseEntity(
                idUser = realUserId,
                idProduct = productId,
                purchaseDate = System.currentTimeMillis()
            )
            storeDao.insertPurchase(purchase)
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun activatePet(userId: String, productId: String) {
        storeDao.deactivateAllPets(userId)
        storeDao.activatePet(userId, productId)
    }

    suspend fun initStoreCatalog() {
        // Initial catalog for the store
        val initialProducts = listOf(
            ProductEntity("pet_cat", "Gato Cósmico", "Un compañero leal de las estrellas", 100, "PET", "https://example.com/assets/cat.svg"),
            ProductEntity("pet_dog", "Perro Galáctico", "Siempre listo para una aventura espacial", 150, "PET", "https://example.com/assets/dog.svg"),
            ProductEntity("acc_hat", "Sombrero Mágico", "Te da un aire de misterio", 50, "ACCESSORY", "https://example.com/assets/hat.svg")
        )
        storeDao.insertProducts(initialProducts)
    }
}
