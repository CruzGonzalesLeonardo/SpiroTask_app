package com.example.spire_task.feature.settings

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spire_task.SpiroTaskApplication
import com.example.spire_task.data.local.database.SpiroDatabase
import com.example.spire_task.data.local.entidades.*
import com.example.spire_task.data.remote.auth.GoogleSignInManager
import com.example.spire_task.data.sync.SyncManager
import com.example.spire_task.data.sync.UserSyncService
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.spire_task.data.local.database.DatosIniciales

data class MascotaInfo(
    val id: Int,
    val nombreEspecie: String,
    val emoji: String,
    val rutaAsset: String?,
    val nivel: Int,
    val experiencia: Int,
    val estaActiva: Boolean
)

data class SettingsUiState(
    val perfil: PerfilUsuarioEntity? = null,
    val mascotas: List<MascotaInfo> = emptyList(),
    val estaCargando: Boolean = true,
    val estaVinculadoGoogle: Boolean = false,
    val googleEmail: String? = null,
    val estaCargandoGoogle: Boolean = false,
    val mostrarDialogoEliminarDatos: Boolean = false,
    val sesionCerrada: Boolean = false,
    val error: String? = null,
    val estaSincronizando: Boolean = false,
    val mensajeSincronizacion: String? = null,
    val mostrarDialogoRestaurar: Boolean = false,
    val googleAccountTemp: GoogleSignInAccount? = null,
    // NUEVOS: Estados requeridos por la UI mejorada
    val recordatorioDiarioActivo: Boolean = true,
    val modoOscuroActivo: Boolean = false,
    val sincronizacionAutomatica: Boolean = false
)

class SettingsViewModel(
    private val database: SpiroDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private lateinit var googleSignInManager: GoogleSignInManager
    private lateinit var syncManager: SyncManager
    private lateinit var userSyncService: UserSyncService
    private lateinit var context: Context

    init {
        inicializarGoogle()
        inicializarSync()
        inicializarUserSync()
        cargarDatos()
        cargarPreferenciasLocales()
    }

    private fun inicializarGoogle() {
        try {
            context = SpiroTaskApplication.instance
            googleSignInManager = GoogleSignInManager(context)
            verificarVinculacionGoogle()
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Error al inicializar Google: ${e.message}") }
        }
    }

    private fun inicializarSync() {
        try {
            syncManager = SyncManager(database)
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Error al inicializar sincronización: ${e.message}") }
        }
    }

    private fun inicializarUserSync() {
        try {
            userSyncService = UserSyncService(database)
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Error al inicializar servicio de usuario: ${e.message}") }
        }
    }

    private fun verificarVinculacionGoogle() {
        viewModelScope.launch {
            val perfil = database.perfilUsuarioDao().obtenerPerfilDirecto()
            _uiState.update {
                it.copy(
                    estaVinculadoGoogle = perfil?.id_google != null,
                    googleEmail = perfil?.google_email
                )
            }
        }
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            try {
                val perfil = database.perfilUsuarioDao().obtenerPerfilDirecto()
                database.mascotaUsuarioDao().obtenerTodas().collect { mascotas ->
                    val mascotasInfo = mascotas.map { mascota ->
                        val especie = database.mascotaBaseDao().obtenerPorId(mascota.id_mascota_base)
                        MascotaInfo(
                            id = mascota.id_mascota_usuario,
                            nombreEspecie = especie?.nombre_especie ?: "Desconocida",
                            emoji = especie?.emoji ?: "🐾",
                            rutaAsset = especie?.ruta_asset_base,
                            nivel = mascota.nivel,
                            experiencia = mascota.experiencia,
                            estaActiva = mascota.esta_activa
                        )
                    }
                    _uiState.update {
                        it.copy(
                            perfil = perfil,
                            mascotas = mascotasInfo,
                            estaCargando = false,
                            estaVinculadoGoogle = perfil?.id_google != null,
                            googleEmail = perfil?.google_email
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(estaCargando = false, error = "Error al cargar datos: ${e.message}") }
            }
        }
    }

    // NUEVO: Carga los estados iniciales de los switches desde SharedPreferences
    private fun cargarPreferenciasLocales() {
        val prefs = SpiroTaskApplication.instance.getSharedPreferences("spiro_prefs", Context.MODE_PRIVATE)
        _uiState.update {
            it.copy(
                recordatorioDiarioActivo = prefs.getBoolean("pref_daily_reminder", true),
                modoOscuroActivo = prefs.getBoolean("pref_dark_mode", false),
                sincronizacionAutomatica = prefs.getBoolean("pref_auto_sync", false)
            )
        }
    }

    // NUEVO: Modifica y persiste el estado del recordatorio diario de tareas
    fun setRecordatorioDiario(activo: Boolean) {
        _uiState.update { it.copy(recordatorioDiarioActivo = activo) }
        val prefs = SpiroTaskApplication.instance.getSharedPreferences("spiro_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("pref_daily_reminder", activo).apply()

        // Aquí podrías cancelar o agendar alarmas en el WorkManager si lo requieres en el futuro
    }

    // NUEVO: Modifica y persiste el estado del modo oscuro
    fun setModoOscuro(activo: Boolean) {
        _uiState.update { it.copy(modoOscuroActivo = activo) }
        val prefs = SpiroTaskApplication.instance.getSharedPreferences("spiro_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("pref_dark_mode", activo).apply()
    }

    fun setSincronizacionAutomatica(activo: Boolean) {
        _uiState.update { it.copy(sincronizacionAutomatica = activo) }
        val prefs = SpiroTaskApplication.instance.getSharedPreferences("spiro_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("pref_auto_sync", activo).apply()

        if (activo) {
            // Si lo activa, podemos lanzar una sincronización inmediata de respaldo
            syncData()
        }
    }

    fun getGoogleSignInIntent(): Intent {
        return googleSignInManager.getSignInIntent()
    }

    suspend fun handleGoogleSignInResult(data: Intent?) {
        _uiState.update { it.copy(estaCargandoGoogle = true) }

        try {
            val result = googleSignInManager.handleSignInResult(data)
            result.onSuccess { googleAccount ->
                val authResult = googleSignInManager.firebaseAuthWithGoogle(googleAccount)
                authResult.onSuccess {
                    val perfilExistente = userSyncService.verificarPerfilEnNube(
                        googleAccount.id ?: "",
                        googleAccount.email ?: ""
                    )

                    if (perfilExistente != null) {
                        _uiState.update {
                            it.copy(
                                googleAccountTemp = googleAccount,
                                mostrarDialogoRestaurar = true,
                                estaCargandoGoogle = false
                            )
                        }
                    } else {
                        vincularPerfilConGoogle(googleAccount)
                    }
                }.onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            estaCargandoGoogle = false,
                            error = "Error de autenticación: ${exception.message}"
                        )
                    }
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        estaCargandoGoogle = false,
                        error = "Error con Google: ${exception.message}"
                    )
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    estaCargandoGoogle = false,
                    error = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    fun restaurarDatosDesdeNube() {
        viewModelScope.launch {
            val googleAccount = _uiState.value.googleAccountTemp ?: return@launch

            _uiState.update {
                it.copy(
                    mostrarDialogoRestaurar = false,
                    estaCargandoGoogle = true,
                    mensajeSincronizacion = "Descargando datos desde la nube..."
                )
            }

            val resultado = userSyncService.descargarDatosUsuario()

            when (resultado) {
                is UserSyncService.SyncResult.Success -> {
                    vincularPerfilConGoogle(googleAccount)
                    _uiState.update {
                        it.copy(
                            googleAccountTemp = null,
                            mensajeSincronizacion = "✅ Datos restaurados desde la nube"
                        )
                    }
                    cargarDatos()
                }
                is UserSyncService.SyncResult.Error -> {
                    _uiState.update {
                        it.copy(
                            googleAccountTemp = null,
                            estaCargandoGoogle = false,
                            error = "Error al restaurar: ${resultado.message}"
                        )
                    }
                }
            }
        }
    }

    fun subirDatosLocalesANube() {
        viewModelScope.launch {
            val googleAccount = _uiState.value.googleAccountTemp ?: return@launch

            _uiState.update {
                it.copy(
                    mostrarDialogoRestaurar = false,
                    estaCargandoGoogle = true,
                    mensajeSincronizacion = "Subiendo datos locales a la nube..."
                )
            }

            val resultado = syncManager.uploadAllData()

            when (resultado) {
                is SyncManager.SyncResult.Success -> {
                    vincularPerfilConGoogle(googleAccount)
                    _uiState.update {
                        it.copy(
                            googleAccountTemp = null,
                            mensajeSincronizacion = "✅ Datos locales subidos a la nube"
                        )
                    }
                }
                is SyncManager.SyncResult.Error -> {
                    _uiState.update {
                        it.copy(
                            googleAccountTemp = null,
                            estaCargandoGoogle = false,
                            error = "Error al subir: ${resultado.message}"
                        )
                    }
                }
            }
        }
    }

    fun cancelarRestauracion() {
        _uiState.update {
            it.copy(
                mostrarDialogoRestaurar = false,
                googleAccountTemp = null,
                estaCargandoGoogle = false,
                mensajeSincronizacion = null
            )
        }
    }

    private suspend fun vincularPerfilConGoogle(googleAccount: GoogleSignInAccount) {
        try {
            val perfilActual = database.perfilUsuarioDao().obtenerPerfilDirecto()
            if (perfilActual != null) {
                val perfilActualizado = perfilActual.copy(
                    id_google = googleAccount.id,
                    google_email = googleAccount.email,
                    ultima_sincronizacion = System.currentTimeMillis()
                )
                database.perfilUsuarioDao().insertarOActualizar(perfilActualizado)

                _uiState.update {
                    it.copy(
                        estaVinculadoGoogle = true,
                        googleEmail = googleAccount.email,
                        estaCargandoGoogle = false,
                        perfil = perfilActualizado,
                        mensajeSincronizacion = "✅ Cuenta vinculada correctamente"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        estaCargandoGoogle = false,
                        error = "No se encontró el perfil para vincular"
                    )
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    estaCargandoGoogle = false,
                    error = "Error al vincular cuenta: ${e.message}"
                )
            }
        }
    }

    fun desvincularGoogle() {
        viewModelScope.launch {
            try {
                val perfilActual = database.perfilUsuarioDao().obtenerPerfilDirecto()
                setSincronizacionAutomatica(false)
                if (perfilActual != null && perfilActual.id_google != null) {
                    val perfilActualizado = perfilActual.copy(
                        id_google = null,
                        google_email = null,
                        ultima_sincronizacion = null
                    )
                    database.perfilUsuarioDao().insertarOActualizar(perfilActualizado)

                    _uiState.update {
                        it.copy(
                            estaVinculadoGoogle = false,
                            googleEmail = null,
                            perfil = perfilActualizado,
                            mensajeSincronizacion = "✅ Cuenta desvinculada"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al desvincular cuenta: ${e.message}") }
            }
        }
    }

    fun syncData() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(estaSincronizando = true, mensajeSincronizacion = null, error = null)
            }

            val perfil = database.perfilUsuarioDao().obtenerPerfilDirecto()
            if (perfil?.id_google == null) {
                _uiState.update {
                    it.copy(
                        estaSincronizando = false,
                        error = "Primero debes vincular tu cuenta de Google"
                    )
                }
                return@launch
            }

            val result = syncManager.uploadAllData()

            when (result) {
                is SyncManager.SyncResult.Success -> {
                    _uiState.update {
                        it.copy(
                            estaSincronizando = false,
                            mensajeSincronizacion = result.message
                        )
                    }
                }
                is SyncManager.SyncResult.Error -> {
                    _uiState.update {
                        it.copy(
                            estaSincronizando = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun downloadData() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(estaSincronizando = true, mensajeSincronizacion = null, error = null)
            }

            val perfil = database.perfilUsuarioDao().obtenerPerfilDirecto()
            if (perfil?.id_google == null) {
                _uiState.update {
                    it.copy(
                        estaSincronizando = false,
                        error = "Primero debes vincular tu cuenta de Google"
                    )
                }
                return@launch
            }

            val result = userSyncService.descargarDatosUsuario()

            when (result) {
                is UserSyncService.SyncResult.Success -> {
                    _uiState.update {
                        it.copy(
                            estaSincronizando = false,
                            mensajeSincronizacion = result.message
                        )
                    }
                    cargarDatos()
                }
                is UserSyncService.SyncResult.Error -> {
                    _uiState.update {
                        it.copy(
                            estaSincronizando = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun limpiarMensajeSincronizacion() {
        _uiState.update { it.copy(mensajeSincronizacion = null) }
    }

    fun toggleDialogoEliminarDatos(mostrar: Boolean) {
        _uiState.update { it.copy(mostrarDialogoEliminarDatos = mostrar) }
    }

    fun eliminarTodosLosDatos() {
        viewModelScope.launch {
            try {
                database.tareaDao().eliminarTodas()
                database.tableroDao().eliminarTodos()
                database.mascotaUsuarioDao().eliminarTodas()
                database.perfilUsuarioDao().eliminarPerfil()

                database.mascotaBaseDao().insertarTodas(DatosIniciales.mascotasBase)

                _uiState.update {
                    it.copy(
                        mostrarDialogoEliminarDatos = false,
                        sesionCerrada = true
                    )
                }

                try {
                    googleSignInManager.signOut()
                } catch (e: Exception) { }

                android.os.Process.killProcess(android.os.Process.myPid())
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al eliminar datos: ${e.message}") }
            }
        }
    }

    fun activarMascota(idMascotaUsuario: Int) {
        viewModelScope.launch {
            try {
                database.mascotaUsuarioDao().cambiarMascotaActiva(idMascotaUsuario)
                cargarDatos()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al activar mascota: ${e.message}") }
            }
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
}