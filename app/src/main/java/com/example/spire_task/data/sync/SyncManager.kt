package com.example.spire_task.data.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.example.spire_task.data.local.database.SpiroDatabase
import kotlinx.coroutines.tasks.await
import com.example.spire_task.data.local.entidades.PerfilUsuarioEntity
import kotlinx.coroutines.flow.first

class SyncManager(
    private val database: SpiroDatabase
) {
    private val auth = FirebaseAuth.getInstance()
    private val firebaseDb = Firebase.database

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    private fun getUserRef() = currentUserId?.let {
        firebaseDb.reference.child("usuarios").child(it)
    }

    /**
     * Subir todos los datos locales a Firebase
     */
    suspend fun uploadAllData(): SyncResult {
        return try {
            val userRef = getUserRef() ?: return SyncResult.Error("Usuario no autenticado")

            // Obtener datos locales - usando .first() para obtener el valor del Flow
            val perfil = database.perfilUsuarioDao().obtenerPerfilDirecto()

            // ✅ Usar .first() para obtener la lista del Flow
            val tableros = database.tableroDao().obtenerTodosDirecto()  // Sin .first()
            val tareas = database.tareaDao().obtenerTodasActivasDirecto()  // Sin .first()
            val mascotas = database.mascotaUsuarioDao().obtenerTodosDirecto()  // Sin .first()

            val dataMap = mutableMapOf<String, Any>()

            // Perfil
            perfil?.let {
                dataMap["perfil"] = mapOf(
                    "id_perfil" to it.id,
                    "nombre" to it.nombre,
                    "email" to it.google_email,
                    "experiencia" to it.experiencia_perfil,
                    "nivel" to it.nivel_perfil,
                    "monedas" to it.monedas,
                    "id_google" to (it.id_google ?: ""),
                    "google_email" to (it.google_email ?: ""),
                    "ultima_sincronizacion" to System.currentTimeMillis()
                )
            }

            // Tableros
            if (tableros.isNotEmpty()) {
                val tablerosList = mutableListOf<Map<String, Any>>()
                for (tablero in tableros) {
                    tablerosList.add(
                        mapOf(
                            "id_tablero" to tablero.id_tablero,
                            "nombre" to tablero.nombre,
                            "color_hex" to (tablero.color_hex ?: "#4A90E2"),
                            "id_mascota_mentora" to tablero.id_mascota_mentora
                        )
                    )
                }
                dataMap["tableros"] = tablerosList
            }

            // Tareas
            if (tareas.isNotEmpty()) {
                val tareasList = mutableListOf<Map<String, Any>>()
                for (tarea in tareas) {
                    tareasList.add(
                        mapOf(
                            "id_tarea" to tarea.id_tarea,
                            "id_tablero" to tarea.id_tablero,
                            "titulo" to tarea.titulo,
                            "descripcion" to (tarea.descripcion ?: ""),
                            "estado" to tarea.estado,
                            "prioridad" to tarea.prioridad,
                            "fecha_limite" to (tarea.fecha_limite ?: 0),
                            "fecha_creacion" to tarea.fecha_creacion,
                            "fecha_completado" to (tarea.fecha_completado ?: 0)
                        )
                    )
                }
                dataMap["tareas"] = tareasList
            }

            // Mascotas
            if (mascotas.isNotEmpty()) {
                val mascotasList = mutableListOf<Map<String, Any>>()
                for (mascota in mascotas) {
                    mascotasList.add(
                        mapOf(
                            "id_mascota_usuario" to mascota.id_mascota_usuario,
                            "id_mascota_base" to mascota.id_mascota_base,
                            "nombre" to (mascota.nombre_personalizado ?: ""),
                            "nivel" to mascota.nivel,
                            "experiencia" to mascota.experiencia,
                            "felicidad_actual" to mascota.felicidad_actual,
                            "esta_activa" to mascota.esta_activa
                        )
                    )
                }
                dataMap["mascotas"] = mascotasList
            }

            dataMap["ultima_sincronizacion"] = System.currentTimeMillis()

            userRef.setValue(dataMap).await()

            SyncResult.Success(" Datos subidos exitosamente", System.currentTimeMillis())
        } catch (e: Exception) {
            SyncResult.Error(" Error al subir datos: ${e.message}")
        }
    }

    /**
     * Descargar datos desde Firebase
     */
    suspend fun downloadAndMergeData(): SyncResult {
        return try {
            val userRef = getUserRef() ?: return SyncResult.Error("Usuario no autenticado")

            val snapshot = userRef.get().await()
            val remoteData = snapshot.value as? Map<*, *>

            if (remoteData == null) {
                return SyncResult.Error("No hay datos en la nube")
            }

            var datosActualizados = 0

            // Sincronizar perfil
            val remotePerfilMap = remoteData["perfil"] as? Map<*, *>
            if (remotePerfilMap != null) {
                val perfil = PerfilUsuarioEntity(
                    id = (remotePerfilMap["id_perfil"] as? Long)?.toInt() ?: 1,
                    nombre = remotePerfilMap["nombre"] as? String ?: "Usuario",
                    google_email = remotePerfilMap["email"] as? String ?: "",
                    experiencia_perfil = (remotePerfilMap["experiencia"] as? Long)?.toInt() ?: 0,
                    nivel_perfil = (remotePerfilMap["nivel"] as? Long)?.toInt() ?: 1,
                    monedas = (remotePerfilMap["monedas"] as? Long)?.toInt() ?: 0,
                    id_google = remotePerfilMap["id_google"] as? String,
                    ultima_sincronizacion = System.currentTimeMillis()
                )
                database.perfilUsuarioDao().insertarOActualizar(perfil)
                datosActualizados++
            }

            SyncResult.Success("✅ Datos descargados", System.currentTimeMillis(), datosActualizados)
        } catch (e: Exception) {
            SyncResult.Error("❌ Error al descargar datos: ${e.message}")
        }
    }

    sealed class SyncResult {
        data class Success(val message: String, val timestamp: Long, val updatedCount: Int = 0) : SyncResult()
        data class Error(val message: String) : SyncResult()
    }
}