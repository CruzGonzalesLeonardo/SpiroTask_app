package com.example.spire_task.feature.shop

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spire_task.SpiroTaskApplication
import com.example.spire_task.data.local.database.SpiroDatabase
import com.example.spire_task.data.local.entidades.MascotaBaseEntity
import com.example.spire_task.data.local.entidades.MascotaUsuarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

sealed class ResultadoHuevo {
    data class NuevaPieza(val especie: MascotaBaseEntity) : ResultadoHuevo()
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

    private val context = SpiroTaskApplication.instance
    private val PREFS_NAME = "spiro_shop_limit_prefs"

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            try {
                val perfil = withContext(Dispatchers.IO) {
                    database.perfilUsuarioDao().obtenerPerfilDirecto()
                }

                database.mascotaBaseDao().obtenerTodas().collect { listaBase ->
                    database.mascotaUsuarioDao().obtenerTodas().collect { mascotas ->
                        _uiState.update {
                            it.copy(
                                monedas = perfil?.monedas ?: 0,
                                mascotasBase = listaBase,
                                mascotasUsuario = mascotas,
                                estaCargando = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(estaCargando = false, error = "Error al conectar con el Mercado Místico") }
            }
        }
    }

    fun obtenerPiezasCompradasHoy(idMascotaBase: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hoyStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val ultimaFecha = prefs.getString("fecha_compra", "")

        if (ultimaFecha != hoyStr) {
            prefs.edit().putString("fecha_compra", hoyStr).apply()
            prefs.all.keys.filter { it.startsWith("pet_") }.forEach { key ->
                prefs.edit().remove(key).apply()
            }
            return 0
        }
        return prefs.getInt("pet_$idMascotaBase", 0)
    }

    private fun registrarCompraPieza(idMascotaBase: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val actuales = obtenerPiezasCompradasHoy(idMascotaBase)
        prefs.edit().putInt("pet_$idMascotaBase", actuales + 1).apply()
    }

    fun abrirHuevo() {
        val state = _uiState.value
        if (state.monedas < 30) {
            _uiState.update { it.copy(error = "Fondos insuficientes. Te faltan ${30 - state.monedas} monedas 🪙") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(animandoHuevo = true) }

            withContext(Dispatchers.IO) {
                database.perfilUsuarioDao().actualizarMonedas(state.monedas - 30)
            }
            _uiState.update { it.copy(monedas = state.monedas - 30) }

            kotlinx.coroutines.delay(1500L)

            val random = Random.nextInt(1, 101)
            val resultado = when {
                // 35% de salir una pieza mística/común que NO esté completada NI reclamada
                random <= 35 -> obtenerPiezaAleatoriaRuleta()
                random <= 65 -> darExperienciaCompensatoria()
                random <= 85 -> recuperarMonedasParciales()
                else -> ResultadoHuevo.Vacio
            }

            _uiState.update {
                it.copy(resultadoHuevo = resultado, mostrarResultado = true, animandoHuevo = false)
            }
            cargarDatos()
        }
    }

    /**
     * 🔥 CORRECCIÓN DE RULETA: Filtra y remueve las mascotas que ya pertenecen al inventario del usuario.
     */
    private suspend fun obtenerPiezaAleatoriaRuleta(): ResultadoHuevo {
        val state = _uiState.value

        // Filtrar: 1. El rompecabezas no debe estar lleno. 2. El usuario NO debe tener ya esa mascota reclamada.
        val especiesDisponibles = state.mascotasBase.filter { base ->
            val rompecabezasIncompleto = base.rompecabezas_actuales < base.rompecabezas_totales
            val noReclamada = state.mascotasUsuario.none { usuario -> usuario.id_mascota_base == base.id_mascota_base }
            rompecabezasIncompleto && noReclamada
        }

        // Si ya completó o reclamó todas, la ruleta por defecto le otorga directamente experiencia a sus mascotas activas
        if (especiesDisponibles.isEmpty()) return darExperienciaCompensatoria()

        val poolMascotas = mutableListOf<MascotaBaseEntity>()
        especiesDisponibles.forEach { m ->
            val pesoEspecie = if (m.nombre_especie.contains("dragon", ignoreCase = true) || m.id_mascota_base == 4) 1 else 4
            repeat(pesoEspecie) { poolMascotas.add(m) }
        }

        val elegida = poolMascotas[Random.nextInt(poolMascotas.size)]
        val nuevosFragmentos = elegida.rompecabezas_actuales + 1

        withContext(Dispatchers.IO) {
            database.mascotaBaseDao().actualProgresoRompecabezas(elegida.id_mascota_base, nuevosFragmentos)
        }

        return ResultadoHuevo.NuevaPieza(elegida.copy(rompecabezas_actuales = nuevosFragmentos))
    }

    fun comprarPiezaDirecta(mascotaBase: MascotaBaseEntity) {
        val state = _uiState.value
        val compradasHoy = obtenerPiezasCompradasHoy(mascotaBase.id_mascota_base)

        if (compradasHoy >= 2) {
            _uiState.update { it.copy(error = "🚫 Límite alcanzado: Máximo 2 piezas directas al día.") }
            return
        }
        if (state.monedas < mascotaBase.precio_monedas) {
            _uiState.update { it.copy(error = "Te faltan ${mascotaBase.precio_monedas - state.monedas} monedas.") }
            return
        }
        if (tieneEspecie(mascotaBase.id_mascota_base)) {
            _uiState.update { it.copy(error = "Ya has invocado esta criatura. ¡Disfruta de su compañía!") }
            return
        }

        viewModelScope.launch {
            val nuevoSaldo = state.monedas - mascotaBase.precio_monedas

            withContext(Dispatchers.IO) {
                database.perfilUsuarioDao().actualizarMonedas(nuevoSaldo)
                database.mascotaBaseDao().actualProgresoRompecabezas(
                    mascotaBase.id_mascota_base,
                    mascotaBase.rompecabezas_actuales + 1
                )
            }

            registrarCompraPieza(mascotaBase.id_mascota_base)
            _uiState.update { it.copy(monedas = nuevoSaldo) }
            cargarDatos()
        }
    }

    fun ensamblarMascota(mascotaBase: MascotaBaseEntity) {
        val state = _uiState.value
        if (mascotaBase.rompecabezas_actuales < mascotaBase.rompecabezas_totales) return

        viewModelScope.launch {
            val nuevaMascota = MascotaUsuarioEntity(
                id_mascota_base = mascotaBase.id_mascota_base,
                nombre_personalizado = mascotaBase.nombre_especie,
                nivel = 1,
                experiencia = 0,
                esta_activa = state.mascotasUsuario.isEmpty(),
                felicidad_actual = mascotaBase.felicidad_base,
                fecha_obtencion = System.currentTimeMillis()
            )

            withContext(Dispatchers.IO) {
                database.mascotaUsuarioDao().insertar(nuevaMascota)
            }
            cargarDatos()
        }
    }

    private suspend fun darExperienciaCompensatoria(): ResultadoHuevo {
        val state = _uiState.value
        if (state.mascotasUsuario.isEmpty()) return recuperarMonedasParciales()

        val mascotaAleatoria = state.mascotasUsuario[Random.nextInt(state.mascotasUsuario.size)]
        val especie = state.mascotasBase.find { it.id_mascota_base == mascotaAleatoria.id_mascota_base }

        withContext(Dispatchers.IO) {
            database.mascotaUsuarioDao().sumarExperiencia(mascotaAleatoria.id_mascota_usuario, 30)
        }
        return ResultadoHuevo.ExperienciaGanada(especie ?: state.mascotasBase.first(), 30)
    }

    private suspend fun recuperarMonedasParciales(): ResultadoHuevo {
        val state = _uiState.value
        val retorno = 15
        withContext(Dispatchers.IO) {
            database.perfilUsuarioDao().actualizarMonedas(state.monedas + retorno)
        }
        return ResultadoHuevo.MonedasRecuperadas(retorno)
    }

    fun tieneEspecie(idMascotaBase: Int): Boolean {
        return _uiState.value.mascotasUsuario.any { it.id_mascota_base == idMascotaBase }
    }

    fun cerrarResultado() {
        _uiState.update { it.copy(mostrarResultado = false, resultadoHuevo = null) }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
}