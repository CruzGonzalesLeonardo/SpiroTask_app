package com.example.spire_task.feature.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spire_task.data.local.database.DatosIniciales
import com.example.spire_task.data.local.database.SpiroDatabase
import com.example.spire_task.data.local.entidades.MascotaBaseEntity
import com.example.spire_task.data.local.entidades.MascotaUsuarioEntity
import com.example.spire_task.data.local.entidades.PerfilUsuarioEntity
import com.example.spire_task.data.remote.auth.GoogleSignInManager
import com.example.spire_task.data.sync.UserSyncService
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val paso: PasoOnboarding = PasoOnboarding.NOMBRE,
    val nombre: String = "",
    val mascotasDisponibles: List<MascotaBaseEntity> = emptyList(),
    val mascotaSeleccionada: MascotaBaseEntity? = null,
    val monedasIniciales: Int = DatosIniciales.MONEDAS_INICIALES,
    val estaCargando: Boolean = false,
    val error: String? = null,
    val onboardingCompletado: Boolean = false
)

enum class PasoOnboarding {
    NOMBRE, ELEGIR_MASCOTA
}

class OnboardingViewModel(
    private val database: SpiroDatabase,
    private val googleSignInManager: GoogleSignInManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private lateinit var userSyncService: UserSyncService

    init {
        Log.d("ONBOARDING", "📱 Inicializando OnboardingViewModel")
        cargarMascotasDisponibles()
    }

    private fun cargarMascotasDisponibles() {
        viewModelScope.launch {
            try {
                Log.d("ONBOARDING", "🔄 Cargando mascotas disponibles...")
                userSyncService = UserSyncService(database)
                val todasLasMascotas = database.mascotaBaseDao().obtenerTodasDirecto()
                val saldoInicial = DatosIniciales.MONEDAS_INICIALES
                val mascotasFiltradas = todasLasMascotas.filter {
                    it.precio_monedas <= saldoInicial
                }
                Log.d("ONBOARDING", "✅ Mascotas cargadas: ${mascotasFiltradas.size}")
                _uiState.value = _uiState.value.copy(
                    mascotasDisponibles = mascotasFiltradas
                )
            } catch (e: Exception) {
                Log.e("ONBOARDING", "❌ Error al cargar mascotas: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    error = "Error al cargar mascotas: ${e.message}"
                )
            }
        }
    }

    fun actualizarNombre(nombre: String) {
        _uiState.value = _uiState.value.copy(
            nombre = nombre,
            error = null
        )
    }

    fun avanzarAElegirMascota() {
        val nombre = _uiState.value.nombre.trim()
        if (nombre.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "Por favor ingresa tu nombre"
            )
            return
        }
        Log.d("ONBOARDING", "➡️ Avanzando a elegir mascota, nombre: $nombre")
        _uiState.value = _uiState.value.copy(
            paso = PasoOnboarding.ELEGIR_MASCOTA,
            nombre = nombre,
            error = null
        )
    }

    fun seleccionarMascota(mascota: MascotaBaseEntity) {
        Log.d("ONBOARDING", "🐾 Mascota seleccionada: ${mascota.nombre_especie}")
        _uiState.value = _uiState.value.copy(
            mascotaSeleccionada = mascota,
            error = null
        )
    }

    fun confirmarMascota() {
        val mascota = _uiState.value.mascotaSeleccionada
        if (mascota == null) {
            _uiState.value = _uiState.value.copy(
                error = "Por favor elige una mascota"
            )
            return
        }

        viewModelScope.launch {
            try {
                Log.d("ONBOARDING", "✅ Confirmando mascota: ${mascota.nombre_especie}")
                _uiState.update { it.copy(estaCargando = true) }

                val nombre = _uiState.value.nombre.trim()
                val precioMascota = mascota.precio_monedas
                val monedasFinales = DatosIniciales.MONEDAS_INICIALES - precioMascota

                // 1. Insertar o Actualizar Perfil de Usuario
                val perfil = PerfilUsuarioEntity(
                    nombre = nombre,
                    monedas = monedasFinales
                )
                database.perfilUsuarioDao().insertarOActualizar(perfil)

                // 2. Insertar la nueva Mascota en el inventario del usuario
                val nuevaMascota = MascotaUsuarioEntity(
                    id_mascota_base = mascota.id_mascota_base,
                    nombre_personalizado = mascota.nombre_especie,
                    nivel = 1,
                    experiencia = 0,
                    esta_activa = true,
                    fecha_obtencion = System.currentTimeMillis(),
                    felicidad_actual = 60, // Inicia al máximo de felicidad (100)
                    ultima_interaccion = System.currentTimeMillis()
                )
                database.mascotaUsuarioDao().insertar(nuevaMascota)

                // 🔥 3. NUEVO: Actualizar las piezas en el catálogo base de la mascota a 10 de 10
                // Esto evita que aparezca con 0/10 en la tienda o ruleta
                database.mascotaBaseDao().actualizarProgresoRompecabezas(
                    id = mascota.id_mascota_base,
                    nuevosFragmentos = 10
                )

                Log.d("ONBOARDING", "🎉 Onboarding completado: Mascota guardada y rompecabezas sincronizado a 10/10")
                _uiState.update {
                    it.copy(
                        estaCargando = false,
                        onboardingCompletado = true
                    )
                }
            } catch (e: Exception) {
                Log.e("ONBOARDING", "❌ Error al crear perfil: ${e.message}")
                _uiState.update {
                    it.copy(
                        estaCargando = false,
                        error = "Error al crear perfil: ${e.message}"
                    )
                }
            }
        }
    }

    fun volverANombre() {
        Log.d("ONBOARDING", "⬅️ Volviendo a paso nombre")
        _uiState.value = _uiState.value.copy(
            paso = PasoOnboarding.NOMBRE,
            mascotaSeleccionada = null,
            error = null
        )
    }

    fun crearPerfilConGoogle(googleAccount: GoogleSignInAccount) {
        viewModelScope.launch {
            try {
                Log.d("ONBOARDING", "🚀 INICIO crearPerfilConGoogle")
                Log.d("ONBOARDING", "Google ID: ${googleAccount.id}")
                Log.d("ONBOARDING", "Google Email: ${googleAccount.email}")
                Log.d("ONBOARDING", "Google Name: ${googleAccount.displayName}")

                _uiState.value = _uiState.value.copy(estaCargando = true, error = null)

                // 1. Verificar si ya existe un perfil con este Google ID en la nube
                Log.d("ONBOARDING", "📡 Verificando perfil en nube...")
                val perfilExistente = userSyncService.verificarPerfilEnNube(
                    googleAccount.id ?: "",
                    googleAccount.email ?: ""
                )

                Log.d("ONBOARDING", "📡 Resultado verificación: $perfilExistente")

                if (perfilExistente != null) {
                    Log.d("ONBOARDING", "✅ Perfil existe en nube, descargando datos...")

                    // Descargar los datos del usuario
                    val resultado = userSyncService.descargarDatosUsuario()
                    Log.d("ONBOARDING", "📡 Resultado descarga: $resultado")

                    when (resultado) {
                        is UserSyncService.SyncResult.Success -> {
                            Log.d("ONBOARDING", "✅ Descarga exitosa! Registros actualizados: ${resultado.updatedCount}")

                            // Recargar mascotas desde la base de datos local
                            val mascotas = database.mascotaUsuarioDao().obtenerTodosDirecto()
                            Log.d("ONBOARDING", "Mascotas encontradas localmente: ${mascotas.size}")

                            mascotas.forEach { mascota ->
                                Log.d("ONBOARDING", "  - Mascota ID: ${mascota.id_mascota_usuario}, Activa: ${mascota.esta_activa}")
                            }

                            if (mascotas.isNotEmpty()) {
                                Log.d("ONBOARDING", "🎉 Tiene mascota, completando onboarding directamente!")
                                _uiState.value = _uiState.value.copy(
                                    estaCargando = false,
                                    onboardingCompletado = true
                                )
                            } else {
                                Log.d("ONBOARDING", "⚠️ No tiene mascota, ir a selección")
                                _uiState.value = _uiState.value.copy(
                                    nombre = perfilExistente.nombre,
                                    estaCargando = false,
                                    paso = PasoOnboarding.ELEGIR_MASCOTA,
                                    error = null
                                )
                            }
                        }
                        is UserSyncService.SyncResult.Error -> {
                            Log.e("ONBOARDING", "❌ Error en descarga: ${resultado.message}")
                            _uiState.value = _uiState.value.copy(
                                estaCargando = false,
                                error = "Error al descargar datos: ${resultado.message}"
                            )
                        }
                    }
                } else {
                    Log.d("ONBOARDING", "🆕 No existe perfil en nube, creando nuevo...")
                    // Crear nuevo perfil
                    val perfil = PerfilUsuarioEntity(
                        nombre = googleAccount.displayName ?: "Usuario",
                        id_google = googleAccount.id,
                        google_email = googleAccount.email,
                        monedas = DatosIniciales.MONEDAS_INICIALES
                    )
                    database.perfilUsuarioDao().insertarOActualizar(perfil)
                    Log.d("ONBOARDING", "✅ Perfil creado localmente con ID: ${perfil.id}, monedas: ${perfil.monedas}")

                    _uiState.value = _uiState.value.copy(
                        nombre = perfil.nombre,
                        estaCargando = false,
                        paso = PasoOnboarding.ELEGIR_MASCOTA,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("ONBOARDING", "❌ Excepción: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    estaCargando = false,
                    error = "Error con Google: ${e.message}"
                )
            }
        }
    }
}