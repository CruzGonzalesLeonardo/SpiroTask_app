package com.example.spire_task.data.sync

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SyncTestService {

    companion object {
        private const val TAG = "SyncTestService"
    }

    private val database = Firebase.database
    private val auth = FirebaseAuth.getInstance()

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * Prueba completa de conexión
     */
    suspend fun testCompleteConnection(): TestResult {
        return try {
            // 1. Verificar autenticación
            val user = auth.currentUser
            if (user == null) {
                return TestResult.Error("❌ Usuario no autenticado", "auth")
            }

            // 2. Probar conexión a la base de datos
            val connectedRef = database.getReference(".info/connected")

            val isConnected = suspendCancellableCoroutine { continuation ->
                connectedRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val connected = snapshot.getValue(Boolean::class.java) ?: false
                        if (connected) {
                            continuation.resume(Unit)
                            connectedRef.removeEventListener(this)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(Exception(error.message))
                    }
                })
            }

            // 3. Probar escritura
            val testRef = database.reference.child("test").child(user.uid)
            val testData = mapOf(
                "timestamp" to System.currentTimeMillis(),
                "uid" to user.uid,
                "email" to user.email,
                "test_message" to "Conexión exitosa"
            )
            testRef.child("test_write").setValue(testData).await()

            // 4. Probar lectura
            val snapshot = testRef.child("test_write").get().await()
            val readData = snapshot.value as? Map<*, *>

            TestResult.Success(
                message = "✅ Conexión exitosa!",
                details = """
                    Usuario: ${user.email}
                    UID: ${user.uid}
                    Datos leídos: ${readData != null}
                    Timestamp: ${System.currentTimeMillis()}
                """.trimIndent()
            )
        } catch (e: Exception) {
            TestResult.Error("❌ Error: ${e.message}", "database")
        }
    }

    /**
     * Probar solo autenticación
     */
    suspend fun testAuth(): TestResult {
        return try {
            val user = auth.currentUser
            if (user != null) {
                TestResult.Success(
                    message = "✅ Usuario autenticado",
                    details = """
                        Email: ${user.email}
                        UID: ${user.uid}
                        Proveedor: ${user.providerId}
                    """.trimIndent()
                )
            } else {
                TestResult.Error("❌ No hay usuario autenticado", "auth")
            }
        } catch (e: Exception) {
            TestResult.Error("❌ Error de autenticación: ${e.message}", "auth")
        }
    }

    /**
     * Probar escritura/lectura en la base de datos
     */
    suspend fun testDatabaseReadWrite(): TestResult {
        return try {
            val user = auth.currentUser ?: return TestResult.Error("Usuario no autenticado", "auth")

            val testRef = database.reference.child("test").child(user.uid)
            val testKey = "test_${System.currentTimeMillis()}"
            val testData = mapOf(
                "message" to "Test desde la app",
                "timestamp" to System.currentTimeMillis()
            )

            // Escritura
            testRef.child(testKey).setValue(testData).await()

            // Lectura
            val snapshot = testRef.child(testKey).get().await()
            val readData = snapshot.value as? Map<*, *>

            if (readData != null) {
                TestResult.Success(
                    message = "✅ Escritura y lectura exitosa",
                    details = """
                        Clave: $testKey
                        Datos: $readData
                    """.trimIndent()
                )
            } else {
                TestResult.Error("No se pudieron leer los datos", "database")
            }
        } catch (e: Exception) {
            TestResult.Error("❌ Error: ${e.message}", "database")
        }
    }

    sealed class TestResult {
        data class Success(val message: String, val details: String) : TestResult()
        data class Error(val message: String, val type: String) : TestResult()
    }
}