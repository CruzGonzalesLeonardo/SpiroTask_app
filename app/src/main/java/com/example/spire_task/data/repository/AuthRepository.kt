package com.example.spire_task.data.repository

import android.content.Context
import com.example.spire_task.data.local.SessionManager
import com.example.spire_task.data.local.dao.ProfileDao
import com.example.spire_task.data.local.database.SpiroDatabase
import com.example.spire_task.data.local.entities.ProfileEntity
import com.example.spire_task.data.remote.auth.GoogleSignInManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth

class AuthRepository(private val context: Context) {

    private val database = SpiroDatabase.getDatabase(context)
    private val profileDao = database.profileDao()
    private val sessionManager = SessionManager(context)

    private var _currentUserId: String? = null
    private var _currentUserName: String? = null

    init {
        // Cargar sesión guardada al iniciar
        if (sessionManager.isLoggedIn()) {
            _currentUserId = sessionManager.getUserId()
            _currentUserName = sessionManager.getUserName()
        }
    }

    fun getCurrentUserId(): String? = _currentUserId
    fun getCurrentUserName(): String? = _currentUserName
    fun isUserLoggedIn(): Boolean = _currentUserId != null

    // Verificar si ya existe un usuario invitado en la base de datos
    suspend fun existeUsuarioInvitado(): Boolean {
        return withContext(Dispatchers.IO) {
            val invitado = profileDao.obtenerUsuarioInvitado()
            invitado != null
        }
    }

    // Obtener el usuario invitado existente
    suspend fun getUsuarioInvitado(): ProfileEntity? {
        return withContext(Dispatchers.IO) {
            profileDao.obtenerUsuarioInvitado()
        }
    }

    // Login como invitado - si ya existe, usa ese; si no, crea uno nuevo
    suspend fun loginAsGuest(userName: String): Result<ProfileEntity> {
        return withContext(Dispatchers.IO) {
            try {
                // Buscar si ya existe un usuario invitado
                val existingGuest = profileDao.obtenerUsuarioInvitado()

                if (existingGuest != null) {
                    // Ya existe un invitado, usarlo
                    _currentUserId = existingGuest.idUser
                    _currentUserName = existingGuest.userName
                    sessionManager.saveSession(existingGuest.idUser, existingGuest.userName)
                    return@withContext Result.success(existingGuest)
                } else {
                    // No existe invitado, crear uno nuevo
                    val userId = "guest_${UUID.randomUUID()}"
                    val profile = ProfileEntity(
                        idUser = userId,
                        userName = userName,
                        authProvider = "local"   // ← Cambia "guest" por "local"
                    )

                    profileDao.insertar(profile)
                    _currentUserId = userId
                    _currentUserName = userName
                    sessionManager.saveSession(userId, userName)
                    return@withContext Result.success(profile)
                }

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Login con Google (para múltiples cuentas)
    suspend fun loginWithGoogle(googleUserId: String, userName: String, email: String): Result<ProfileEntity> {
        return withContext(Dispatchers.IO) {
            try {
                // Buscar si ya existe este usuario de Google
                var existing = profileDao.obtenerPorId(googleUserId)

                if (existing == null) {
                    // Usuario de Google nuevo, crear perfil
                    val profile = ProfileEntity(
                        idUser = googleUserId,
                        userName = userName,
                        email = email,
                        authProvider = "google"
                    )
                    profileDao.insertar(profile)
                    existing = profile
                }

                _currentUserId = existing.idUser
                _currentUserName = existing.userName
                sessionManager.saveSession(existing.idUser, existing.userName)
                Result.success(existing)

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Cerrar sesión
    fun logout() {
        _currentUserId = null
        _currentUserName = null
        sessionManager.clearSession()
    }

    suspend fun logoutWithGoogle() {
        val googleManager = GoogleSignInManager(context)
        googleManager.signOut()
        _currentUserId = null
        _currentUserName = null
        sessionManager.clearSession()
    }

    suspend fun getUserById(userId: String): ProfileEntity? {
        return withContext(Dispatchers.IO) {
            profileDao.obtenerPorId(userId)
        }
    }
    suspend fun registerLocalUser(userName: String): Result<ProfileEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val existing = profileDao.obtenerPorUserName(userName)
                if (existing != null) {
                    return@withContext Result.failure(Exception("El nombre de usuario ya existe"))
                }

                val userId = "local_${UUID.randomUUID()}"
                val profile = ProfileEntity(
                    idUser = userId,
                    userName = userName,
                    email = "",
                    authProvider = "local"
                )

                profileDao.insertar(profile)
                _currentUserId = userId
                _currentUserName = userName
                sessionManager.saveSession(userId, userName, "", "local")
                Result.success(profile)

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun loginWithGoogleAccount(account: GoogleSignInAccount): Result<ProfileEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                val userId = firebaseUser?.uid ?: account.id ?: return@withContext Result.failure(Exception("No se pudo obtener el ID"))
                val userName = account.displayName ?: account.email?.split("@")?.first() ?: "Usuario"
                val email = account.email ?: ""

                // Buscar si ya existe este usuario de Google
                var existing = profileDao.obtenerPorId(userId)

                if (existing == null) {
                    // Usuario de Google nuevo, crear perfil
                    val profile = ProfileEntity(
                        idUser = userId,
                        userName = userName,
                        email = email,
                        authProvider = "google"
                    )
                    profileDao.insertar(profile)
                    existing = profile
                }

                _currentUserId = existing.idUser
                _currentUserName = existing.userName
                sessionManager.saveSession(existing.idUser, existing.userName)
                Result.success(existing)

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    fun getCurrentUserEmail(): String? {
        return sessionManager.getUserEmail()
    }

    fun getCurrentAuthProvider(): String? {
        return sessionManager.getAuthProvider()
    }

}