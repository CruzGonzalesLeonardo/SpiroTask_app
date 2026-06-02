package com.example.spire_task.data.repository

import com.example.spire_task.data.local.dao.ProfileDao
import com.example.spire_task.data.local.dao.StoreDao
import com.example.spire_task.data.local.entities.ProductEntity
import com.example.spire_task.data.local.entities.PurchaseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class StoreRepository(
    private val storeDao: StoreDao,
    private val profileDao: ProfileDao
) {

    val allProducts: Flow<List<ProductEntity>> = storeDao.getAllProducts()

    fun getUserPurchases(userId: String): Flow<List<PurchaseEntity>> = storeDao.getUserPurchases(userId)

    fun getOwnedProducts(userId: String): Flow<List<ProductEntity>> = storeDao.getOwnedProducts(userId)

    fun getActivePet(userId: String): Flow<ProductEntity?> = storeDao.getActivePet(userId)

    suspend fun purchaseProduct(userId: String, productId: String): Result<Unit> {
        val user = profileDao.obtenerPorId(userId)
            ?: profileDao.obtenerUsuarioInvitado()
            ?: return Result.failure(Exception("Usuario no encontrado en la base de datos"))

        val product = storeDao.getProductById(productId) ?: return Result.failure(Exception("Producto no encontrado"))

        if (user.monedas < product.price) {
            return Result.failure(Exception("Monedas insuficientes. ¡Sigue completando tareas para ganar más!"))
        }

        try {
            val realUserId = user.idUser
            profileDao.actualizarMonedas(realUserId, user.monedas - product.price)

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
        val existingProducts = storeDao.getAllProducts().firstOrNull()
        if (existingProducts?.isNotEmpty() == true) {
            return
        }

        val initialProducts = listOf(
            ProductEntity("pet_cat", "Gato Cósmico", "Un compañero leal de las estrellas", 100, "PET", "🐱"),
            ProductEntity("pet_dog", " Pinguino Navideño", "Siempre listo para una aventura espacial", 150, "PET", "https://example.com/drawable/pingu.xml"),
            ProductEntity("pet_dragon", "Dragón Mágico", "Un amigo que escupe fuego colorido", 200, "PET", "🐉"),
            ProductEntity("acc_hat", "Sombrero Mágico", "Te da un aire de misterio", 50, "ACCESSORY", "https://example.com/assets/hat.svg")
        )
        storeDao.insertProducts(initialProducts)
    }

    // ✅ NUEVA FUNCIÓN: Adoptar primera mascota (SIEMPRE GRATIS)
    suspend fun adoptFirstPet(userId: String, productId: String): Result<Unit> {
        return try {
            val user = profileDao.obtenerPorId(userId)
                ?: profileDao.obtenerUsuarioInvitado()
                ?: return Result.failure(Exception("Usuario no encontrado"))

            val product = storeDao.getProductById(productId)
                ?: return Result.failure(Exception("Producto no encontrado"))

            if (product.type != "PET") {
                return Result.failure(Exception("Solo puedes adoptar mascotas")
                )}

            // Verificar si ya tiene mascotas
            val hasPet = hasUserAdoptedFirstPet(userId)
            if (hasPet) {
                return Result.failure(Exception("Ya tienes una mascota. Para más mascotas, ve a la tienda."))
            }

            // Registrar compra SIN descontar monedas (GRATIS)
            val purchase = PurchaseEntity(
                idUser = user.idUser,
                idProduct = productId,
                purchaseDate = System.currentTimeMillis()
            )
            storeDao.insertPurchase(purchase)

            // Activar automáticamente la mascota
            storeDao.deactivateAllPets(user.idUser)
            storeDao.activatePet(user.idUser, productId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasUserAdoptedFirstPet(userId: String): Boolean {
        val ownedPets = storeDao.getOwnedProducts(userId).firstOrNull()
        return ownedPets?.isNotEmpty() == true
    }
}