package com.example.spire_task.feature.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spire_task.data.local.database.SpiroDatabase
import com.example.spire_task.data.local.entidades.MascotaBaseEntity
import com.example.spire_task.data.local.entidades.MascotaUsuarioEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed class ResultadoHuevo {
    data class NuevaMascota(val especie: MascotaBaseEntity) : ResultadoHuevo()
    data class ExperienciaGanada(val especie: MascotaBaseEntity, val xp: Int) : ResultadoHuevo()
    data class MonedasRecuperadas(val cantidad: Int) : ResultadoHuevo()
    data object Vacio : ResultadoHuevo()
}

data class ShopUiState(
    val monedas: Int = 0,
    val mascotasBase: List<MascotaBaseEntity> = emptyList(),
    val mascotasUsuario: List<MascotaUsuarioEntity> = emptyList(),
    val estaCargando: Boolean = true,
    val resultadoHuevo: ResultadoHuevo? = null,
    val mostrarResultado: Boolean = false,
    val animandoHuevo: Boolean = false,
    val error: String? = null
)

class ShopViewModel(
    private val database: SpiroDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            try {
                val perfil = database.perfilUsuarioDao().obtenerPerfilDirecto()
                val mascotasBase = database.mascotaBaseDao().obtenerTodasDirecto()
                database.mascotaUsuarioDao().obtenerTodas().collect { mascotas ->
                    _uiState.update {
                        it.copy(
                            monedas = perfil?.monedas ?: 0,
                            mascotasBase = mascotasBase,
                            mascotasUsuario = mascotas,
                            estaCargando = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(estaCargando = false, error = "Error al cargar tienda") }
            }
        }
    }

    fun abrirHuevo() {
        val state = _uiState.value
        if (state.monedas < 30) {
            _uiState.update { it.copy(error = "Te faltan ${30 - state.monedas} monedas") }
            return
        }

        viewModelScope.launch {
            database.perfilUsuarioDao().actualizarMonedas(state.monedas - 30)
            _uiState.update { it.copy(monedas = state.monedas - 30, animandoHuevo = true) }

            kotlinx.coroutines.delay(1500L)

            val random = Random.nextInt(1, 101)
            val resultado = when {
                random <= 35 -> obtenerNuevaMascota()
                random <= 65 -> darExperiencia()
                random <= 85 -> recuperarMonedas()
                else -> ResultadoHuevo.Vacio
            }

            _uiState.update {
                it.copy(
                    resultadoHuevo = resultado,
                    mostrarResultado = true,
                    animandoHuevo = false
                )
            }
        }
    }

    private suspend fun obtenerNuevaMascota(): ResultadoHuevo {
        val state = _uiState.value
        val especiesPoseidas = state.mascotasUsuario.map { it.id_mascota_base }.toSet()
        val especiesNoPoseidas = state.mascotasBase.filter { it.id_mascota_base !in especiesPoseidas }

        return if (especiesNoPoseidas.isNotEmpty()) {
            val elegida = especiesNoPoseidas[Random.nextInt(especiesNoPoseidas.size)]
            val nuevaMascota = MascotaUsuarioEntity(
                id_mascota_base = elegida.id_mascota_base,
                nombre_personalizado = elegida.nombre_especie,
                nivel = 1,
                experiencia = 0,
                esta_activa = state.mascotasUsuario.isEmpty(),
                felicidad_actual = elegida.felicidad_base,
                fecha_obtencion = System.currentTimeMillis()
            )
            database.mascotaUsuarioDao().insertar(nuevaMascota)
            ResultadoHuevo.NuevaMascota(elegida)
        } else {
            darExperiencia()
        }
    }

    private suspend fun darExperiencia(): ResultadoHuevo {
        val state = _uiState.value
        if (state.mascotasUsuario.isEmpty()) {
            return recuperarMonedas()
        }

        val mascotaAleatoria = state.mascotasUsuario[Random.nextInt(state.mascotasUsuario.size)]
        val especie = state.mascotasBase.find { it.id_mascota_base == mascotaAleatoria.id_mascota_base }

        database.mascotaUsuarioDao().sumarExperiencia(mascotaAleatoria.id_mascota_usuario, 30)
        verificarSubidaNivel(mascotaAleatoria)

        return ResultadoHuevo.ExperienciaGanada(
            especie = especie ?: state.mascotasBase.first(),
            xp = 30
        )
    }

    private suspend fun recuperarMonedas(): ResultadoHuevo {
        val state = _uiState.value
        database.perfilUsuarioDao().actualizarMonedas(state.monedas + 15)
        _uiState.update { it.copy(monedas = state.monedas + 15) }
        return ResultadoHuevo.MonedasRecuperadas(15)
    }

    private suspend fun verificarSubidaNivel(mascota: MascotaUsuarioEntity) {
        val xpNecesaria = mascota.nivel * 100
        if (mascota.experiencia + 30 >= xpNecesaria) {
            database.mascotaUsuarioDao().subirNivel(mascota.id_mascota_usuario)
        }
    }

    fun comprarMascotaDirecta(mascotaBase: MascotaBaseEntity) {
        val state = _uiState.value
        if (state.monedas < mascotaBase.precio_monedas) {
            _uiState.update { it.copy(error = "Te faltan ${mascotaBase.precio_monedas - state.monedas} monedas") }
            return
        }

        viewModelScope.launch {
            val nuevoSaldo = state.monedas - mascotaBase.precio_monedas
            database.perfilUsuarioDao().actualizarMonedas(nuevoSaldo)

            val nuevaMascota = MascotaUsuarioEntity(
                id_mascota_base = mascotaBase.id_mascota_base,
                nivel = 1,
                experiencia = 0,
                esta_activa = state.mascotasUsuario.isEmpty(),
                fecha_obtencion = System.currentTimeMillis()
            )
            database.mascotaUsuarioDao().insertar(nuevaMascota)

            _uiState.update {
                it.copy(
                    monedas = nuevoSaldo,
                    mascotasUsuario = it.mascotasUsuario + nuevaMascota
                )
            }
        }
    }

    fun tieneEspecie(idMascotaBase: Int): Boolean {
        return _uiState.value.mascotasUsuario.any { it.id_mascota_base == idMascotaBase }
    }

    fun puedeComprar(precio: Int): Boolean {
        return _uiState.value.monedas >= precio
    }

    fun cerrarResultado() {
        _uiState.update { it.copy(mostrarResultado = false, resultadoHuevo = null) }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
}