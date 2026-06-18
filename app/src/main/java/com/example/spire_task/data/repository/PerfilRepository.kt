package com.example.spire_task.data.repository

import com.example.spire_task.data.local.daos.PerfilUsuarioDao
import com.example.spire_task.data.local.entidades.PerfilUsuarioEntity
import com.example.spire_task.data.remote.auth.GoogleSignInManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.firstOrNull

class PerfilRepository(
    private val perfilDao: PerfilUsuarioDao,
    private val googleSignInManager: GoogleSignInManager
) {

    // Obtener perfil actual
    suspend fun obtenerPerfil(): PerfilUsuarioEntity? {
        return perfilDao.obtenerPerfilDirecto()
    }

    // Vincular cuenta de Google al perfil existente
    suspend fun vincularConGoogle(googleAccount: GoogleSignInAccount): Boolean {
        val perfilExistente = perfilDao.obtenerPerfilDirecto()

        return if (perfilExistente != null) {
            val perfilActualizado = perfilExistente.copy(
                id_google = googleAccount.id,  // Aquí asignas la ID de Google
                google_email = googleAccount.email,
                ultima_sincronizacion = System.currentTimeMillis()
            )
            perfilDao.insertarOActualizar(perfilActualizado)
            true
        } else {
            false
        }
    }

    // Crear nuevo perfil con ID de Google
    suspend fun crearPerfilConGoogle(googleAccount: GoogleSignInAccount): PerfilUsuarioEntity {
        val nuevoPerfil = PerfilUsuarioEntity(
            id = 1,
            nombre = googleAccount.displayName ?: "Usuario",
            id_google = googleAccount.id,  // Asignar ID de Google
            google_email = googleAccount.email,
            monedas = 100, // o DatosIniciales.MONEDAS_INICIALES
            nivel_perfil = 1,
            experiencia_perfil = 0,
            fecha_creacion = System.currentTimeMillis(),
            ultima_sincronizacion = System.currentTimeMillis()
        )
        perfilDao.insertarOActualizar(nuevoPerfil)
        return nuevoPerfil
    }

    // Actualizar solo la ID de Google
    suspend fun actualizarGoogleId(googleId: String, googleEmail: String? = null) {
        val perfil = perfilDao.obtenerPerfilDirecto()
        perfil?.let {
            val perfilActualizado = it.copy(
                id_google = googleId,
                google_email = googleEmail ?: it.google_email,
                ultima_sincronizacion = System.currentTimeMillis()
            )
            perfilDao.insertarOActualizar(perfilActualizado)
        }
    }

    // Obtener la ID de Google del perfil actual
    suspend fun getGoogleId(): String? {
        return perfilDao.obtenerPerfilDirecto()?.id_google
    }

    // Verificar si el perfil está vinculado a Google
    suspend fun isVinculadoAGoogle(): Boolean {
        return perfilDao.obtenerPerfilDirecto()?.id_google != null
    }
}