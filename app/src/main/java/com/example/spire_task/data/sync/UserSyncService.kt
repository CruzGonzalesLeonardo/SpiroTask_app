package com.example.spire_task.data.sync

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.example.spire_task.data.local.database.SpiroDatabase
import com.example.spire_task.data.local.entidades.PerfilUsuarioEntity
import com.example.spire_task.data.local.entidades.TableroEntity
import com.example.spire_task.data.local.entidades.TareaEntity
import com.example.spire_task.data.local.entidades.SubtareaEntity
import com.example.spire_task.data.local.entidades.MascotaUsuarioEntity
import kotlinx.coroutines.tasks.await

class UserSyncService(
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
     * Verificar si existe un perfil vinculado a este Google ID en la nube
     * @return PerfilUsuarioEntity si existe, null si no
     */
    suspend fun verificarPerfilEnNube(googleId: String, googleEmail: String): PerfilUsuarioEntity? {
        return try {
            Log.d("USER_SYNC", "🔍 Buscando perfil con googleId: $googleId")

            // Método 1: Buscar directamente en la ruta del usuario actual
            val userRef = getUserRef()
            if (userRef != null) {
                val perfilSnapshot = userRef.child("perfil").get().await()
                val perfilData = perfilSnapshot.value as? Map<*, *>

                if (perfilData != null) {
                    val idGoogle = perfilData["id_google"] as? String
                    Log.d("USER_SYNC", "📡 id_google en perfil: $idGoogle")

                    if (idGoogle == googleId) {
                        val perfil = PerfilUsuarioEntity(
                            id = (perfilData["id_perfil"] as? Long)?.toInt() ?: 1,
                            nombre = perfilData["nombre"] as? String ?: "Usuario",
                            google_email = perfilData["google_email"] as? String ?: googleEmail,
                            experiencia_perfil = (perfilData["experiencia"] as? Long)?.toInt() ?: 0,
                            nivel_perfil = (perfilData["nivel"] as? Long)?.toInt() ?: 1,
                            monedas = (perfilData["monedas"] as? Long)?.toInt() ?: 100,
                            id_google = idGoogle,
                            ultima_sincronizacion = perfilData["ultima_sincronizacion"] as? Long ?: System.currentTimeMillis()
                        )
                        Log.d("USER_SYNC", "✅ Perfil encontrado en ruta usuarios/{uid}/perfil")
                        return perfil
                    }
                }
            }

            // Método 2: Buscar en toda la colección (como fallback)
            Log.d("USER_SYNC", "🔍 Buscando en toda la colección usuarios...")
            val usuariosRef = firebaseDb.reference.child("usuarios")
            val snapshot = usuariosRef.get().await()

            for (usuarioSnapshot in snapshot.children) {
                val perfilSnap = usuarioSnapshot.child("perfil")
                val perfilData = perfilSnap.value as? Map<*, *>
                val idGoogle = perfilData?.get("id_google") as? String

                if (idGoogle == googleId) {
                    val uid = usuarioSnapshot.key
                    Log.d("USER_SYNC", "📡 UID encontrado: $uid, currentUserId: $currentUserId")

                    if (uid == currentUserId) {
                        val perfil = PerfilUsuarioEntity(
                            id = (perfilData["id_perfil"] as? Long)?.toInt() ?: 1,
                            nombre = perfilData["nombre"] as? String ?: "Usuario",
                            google_email = perfilData["google_email"] as? String ?: googleEmail,
                            experiencia_perfil = (perfilData["experiencia"] as? Long)?.toInt() ?: 0,
                            nivel_perfil = (perfilData["nivel"] as? Long)?.toInt() ?: 1,
                            monedas = (perfilData["monedas"] as? Long)?.toInt() ?: 100,
                            id_google = idGoogle,
                            ultima_sincronizacion = perfilData["ultima_sincronizacion"] as? Long ?: System.currentTimeMillis()
                        )
                        Log.d("USER_SYNC", "✅ Perfil encontrado en búsqueda global")
                        return perfil
                    }
                }
            }

            Log.d("USER_SYNC", "❌ No se encontraron usuarios con ese googleId en la nube")
            null
        } catch (e: Exception) {
            Log.e("USER_SYNC", "❌ Error en verificarPerfilEnNube: ${e.message}", e)
            null
        }
    }

    /**
     * Descargar todos los datos del usuario desde Firebase
     */
    @SuppressLint("RestrictedApi")
    suspend fun descargarDatosUsuario(): SyncResult {
        return try {
            Log.d("USER_SYNC", "📥 Iniciando descarga de datos...")
            val userRef = getUserRef() ?: return SyncResult.Error("Usuario no autenticado")

            Log.d("USER_SYNC", "📡 UserRef path: ${userRef.path}")
            val snapshot = userRef.get().await()
            val remoteData = snapshot.value as? Map<*, *>

            if (remoteData == null) {
                return SyncResult.Error("No hay datos en la nube")
            }

            var datosActualizados = 0

            // Descargar perfil
            val remotePerfilMap = remoteData["perfil"] as? Map<*, *>
            if (remotePerfilMap != null) {
                val perfil = PerfilUsuarioEntity(
                    id = (remotePerfilMap["id_perfil"] as? Long)?.toInt() ?: 1,
                    nombre = remotePerfilMap["nombre"] as? String ?: "Usuario",
                    google_email = remotePerfilMap["google_email"] as? String ?: "",
                    experiencia_perfil = (remotePerfilMap["experiencia"] as? Long)?.toInt() ?: 0,
                    nivel_perfil = (remotePerfilMap["nivel"] as? Long)?.toInt() ?: 1,
                    monedas = (remotePerfilMap["monedas"] as? Long)?.toInt() ?: 0,
                    id_google = remotePerfilMap["id_google"] as? String,
                    ultima_sincronizacion = System.currentTimeMillis()
                )
                database.perfilUsuarioDao().insertarOActualizar(perfil)
                datosActualizados++
                Log.d("USER_SYNC", "✅ Perfil descargado: ${perfil.nombre}")
            }

            // Descargar tableros
            val remoteTableros = remoteData["tableros"] as? List<Map<*, *>>
            remoteTableros?.forEach { tableroMap ->
                val tablero = TableroEntity(
                    id_tablero = (tableroMap["id_tablero"] as? Long)?.toInt() ?: 0,
                    nombre = tableroMap["nombre"] as? String ?: "",
                    color_hex = tableroMap["color_hex"] as? String ?: "#4A90E2",
                    id_mascota_mentora = (tableroMap["id_mascota_mentora"] as? Long)?.toInt() ?: 1
                )
                database.tableroDao().insertar(tablero)
                datosActualizados++
            }

            // Descargar tareas
            val remoteTareas = remoteData["tareas"] as? List<Map<*, *>>
            remoteTareas?.forEach { tareaMap ->
                val tarea = TareaEntity(
                    id_tarea = (tareaMap["id_tarea"] as? Long)?.toInt() ?: 0,
                    id_tablero = (tareaMap["id_tablero"] as? Long)?.toInt() ?: 1,
                    titulo = tareaMap["titulo"] as? String ?: "",
                    descripcion = tareaMap["descripcion"] as? String,
                    estado = tareaMap["estado"] as? String ?: "POR_HACER",
                    prioridad = (tareaMap["prioridad"] as? Long)?.toInt() ?: 1,
                    fecha_limite = (tareaMap["fecha_limite"] as? Long),
                    fecha_creacion = tareaMap["fecha_creacion"] as? Long ?: System.currentTimeMillis(),
                    fecha_completado = (tareaMap["fecha_completado"] as? Long)?.takeIf { it > 0 }
                )
                database.tareaDao().insertar(tarea)
                datosActualizados++
            }

            // Descargar subtareas
            val remoteSubtareas = remoteData["subtareas"] as? List<Map<*, *>>
            remoteSubtareas?.forEach { subtareaMap ->
                val subtarea = SubtareaEntity(
                    id_subtarea = (subtareaMap["id_subtarea"] as? Long)?.toInt() ?: 0,
                    id_tarea = (subtareaMap["id_tarea"] as? Long)?.toInt() ?: 1,
                    descripcion = subtareaMap["descripcion"] as? String ?: "",
                    completada = subtareaMap["completada"] as? Boolean ?: false
                )
                database.subtareaDao().insertar(subtarea)
                datosActualizados++
            }

            // Descargar mascotas
            val remoteMascotas = remoteData["mascotas"] as? List<Map<*, *>>
            remoteMascotas?.forEach { mascotaMap ->
                val mascota = MascotaUsuarioEntity(
                    id_mascota_usuario = (mascotaMap["id_mascota_usuario"] as? Long)?.toInt() ?: 0,
                    id_mascota_base = (mascotaMap["id_mascota_base"] as? Long)?.toInt() ?: 1,
                    nombre_personalizado = mascotaMap["nombre"] as? String,
                    nivel = (mascotaMap["nivel"] as? Long)?.toInt() ?: 1,
                    experiencia = (mascotaMap["experiencia"] as? Long)?.toInt() ?: 0,
                    felicidad_actual = (mascotaMap["felicidad_actual"] as? Long)?.toInt() ?: 100,
                    esta_activa = mascotaMap["esta_activa"] as? Boolean ?: true
                )
                database.mascotaUsuarioDao().insertar(mascota)
                datosActualizados++
            }

            Log.d("USER_SYNC", "✅ Descarga completada! Registros: $datosActualizados")
            SyncResult.Success("✅ Datos descargados: $datosActualizados registros", System.currentTimeMillis(), datosActualizados)
        } catch (e: Exception) {
            Log.e("USER_SYNC", "❌ Error al descargar: ${e.message}", e)
            SyncResult.Error("❌ Error al descargar: ${e.message}")
        }
    }

    sealed class SyncResult {
        data class Success(val message: String, val timestamp: Long, val updatedCount: Int = 0) : SyncResult()
        data class Error(val message: String) : SyncResult()
    }
}