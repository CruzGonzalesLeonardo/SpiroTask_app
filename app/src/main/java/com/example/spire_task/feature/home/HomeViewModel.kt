package com.example.spire_task.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spire_task.data.local.database.SpiroDatabase
import com.example.spire_task.data.local.entidades.MascotaUsuarioEntity
import com.example.spire_task.data.local.entidades.PerfilUsuarioEntity
import com.example.spire_task.data.local.entidades.TareaEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

data class HomeUiState(
    val perfil: PerfilUsuarioEntity? = null,
    val mascotaActiva: MascotaUsuarioEntity? = null,
    val nombreMascota: String = "",
    val emojiMascota: String = "🦉",
    val rutaAssetMascota: String? = null,
    val rutaHabitadMascota: String? = null,  // ✅ NUEVO CAMPO
    val rutaAssetTristeMascota: String? = null,
    val felicidadMascota: Int = 100,
    val estaCargando: Boolean = true,
    val tareasCompletadasHoy: Int = 0,
    val tareasTotalesHoy: Int = 0,
    val minutosEnfocadoHoy: Int = 0,
    val rachaDias: Int = 0,
    val tareasPendientes: List<TareaEntity> = emptyList()
)

class HomeViewModel(
    private val database: SpiroDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observarPerfil()
        observarMascotaActiva()
        observarCambiosYActualizarRacha()
        cargarEstadisticas()
        cargarTareasPendientes()
        verificarDescuentoDiarioMascotas()
        observarTareasVencidas()
    }

    private fun observarPerfil() {
        viewModelScope.launch {
            try {
                database.perfilUsuarioDao().obtenerPerfil().collect { perfil ->
                    _uiState.update { it.copy(perfil = perfil, estaCargando = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(estaCargando = false) }
            }
        }
    }

    private fun observarMascotaActiva() {
        viewModelScope.launch {
            try {
                database.mascotaUsuarioDao().obtenerMascotaActiva().collect { mascota ->
                    if (mascota != null) {
                        val especie = database.mascotaBaseDao().obtenerPorId(mascota.id_mascota_base)
                        _uiState.update {
                            it.copy(
                                mascotaActiva = mascota,
                                nombreMascota = especie?.nombre_especie ?: "",
                                emojiMascota = especie?.emoji ?: "🦉",
                                rutaAssetMascota = especie?.ruta_asset_base,
                                rutaAssetTristeMascota = especie?.ruta_asset_triste,
                                rutaHabitadMascota = especie?.ruta_asset_habitad ,
                                felicidadMascota = mascota.felicidad_actual// ✅ AGREGADO
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                mascotaActiva = null,
                                nombreMascota = "",
                                emojiMascota = "🦉",
                                rutaAssetMascota = null,
                                rutaAssetTristeMascota = null,
                                rutaHabitadMascota = null,
                                felicidadMascota = 100// ✅ AGREGADO
                            )
                        }
                    }
                }
            } catch (e: Exception) { /* ignorar */ }
        }
    }

    private fun cargarEstadisticas() {
        viewModelScope.launch {
            try {
                val todasTareas = database.tareaDao().obtenerTodasActivas()
                val hoy = System.currentTimeMillis()
                val inicioDia = hoy - (hoy % 86400000)

                val tareasHoy = todasTareas.filter { it.fecha_modificacion >= inicioDia }
                val completadas = tareasHoy.count { it.estado == "FINALIZADO" }
                val total = tareasHoy.size

                _uiState.update {
                    it.copy(
                        tareasCompletadasHoy = completadas,
                        tareasTotalesHoy = total,
                        rachaDias = 0
                    )
                }
            } catch (e: Exception) { /* ignorar */ }
        }
    }

    private fun cargarTareasPendientes() {
        viewModelScope.launch {
            try {
                database.tareaDao().observarTodasLasPendientes().collect { tareas ->
                    // Ordenamos: las que vencen antes primero. Las que no tienen fecha van al final.
                    val pendientesOrdenadas = tareas.sortedWith(
                        compareBy<TareaEntity> { it.fecha_limite == null }
                            .thenBy { it.fecha_limite }
                    )

                    _uiState.update { it.copy(tareasPendientes = pendientesOrdenadas) }
                }
            } catch (e: Exception) { /* ignorar */ }
        }
    }
    fun interactuarConMascota() {
        viewModelScope.launch {
            val mascotaActual = _uiState.value.mascotaActiva ?: return@launch
            val nuevaFelicidad = (mascotaActual.felicidad_actual + 10).coerceIn(0, 100)

            database.mascotaUsuarioDao().actualizarFelicidad(
                id = mascotaActual.id_mascota_usuario,
                felicidad = nuevaFelicidad
            )

            _uiState.update {
                it.copy(felicidadMascota = nuevaFelicidad)
            }
        }
    }

    fun calcularRachaActual(tareasCompletadas: List<TareaEntity>): Int {
        if (tareasCompletadas.isEmpty()) return 0

        // 1. Convertir los timestamps a días únicos (eliminando horas, minutos, segundos)
        val diasConTareas = tareasCompletadas.map { tarea ->
            val cal = Calendar.getInstance().apply { timeInMillis = tarea.fecha_completado!! }
            // Forzamos a que apunte a las 00:00:00 del día para comparar limpiamente
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.toSet().sortedDescending() // De hoy hacia atrás

        if (diasConTareas.isEmpty()) return 0

        // 2. Obtener el inicio del día de hoy y del día de ayer
        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val unDiaEnMillis = TimeUnit.DAYS.toMillis(1)
        val ayer = hoy - unDiaEnMillis

        // Si no hay tareas hoy ni ayer, la racha se rompió y vuelve a 0
        val ultimoDiaCompletado = diasConTareas.first()
        if (ultimoDiaCompletado != hoy && ultimoDiaCompletado != ayer) {
            return 0
        }

        // 3. Contar cuántos días consecutivos hay hacia atrás
        var racha = 0
        var diaEsperado = ultimoDiaCompletado

        for (dia in diasConTareas) {
            if (dia == diaEsperado) {
                racha++
                diaEsperado -= unDiaEnMillis // Restamos un día para buscar el anterior consecutivo
            } else {
                break // Hubo un salto de un día completo, se detiene el conteo
            }
        }

        return racha
    }

    private fun observarCambiosYActualizarRacha() {
        viewModelScope.launch {
            database.tareaDao().observarTodasLasCompletadas().collect { tareasCompletadas ->
                // Ejecuta el cálculo algorítmico basado en el historial de tareas finalizadas
                val nuevaRacha = calcularRachaActual(tareasCompletadas)

                // Actualiza de forma persistente en la tabla PerfilUsuario
                database.perfilUsuarioDao().actualizarRacha(nuevaRacha)

                // Refleja instantáneamente la racha calculada en el UI state de la pantalla
                _uiState.update { estadoActual ->
                    estadoActual.copy(rachaDias = nuevaRacha)
                }
            }
        }
    }

    private fun verificarDescuentoDiarioMascotas() {
        viewModelScope.launch {
            try {
                val ahora = System.currentTimeMillis()
                val unDiaEnMillis = 24 * 60 * 60 * 1000 // 86,400,000 ms

                // 1. Traer solo las mascotas asignadas a tableros no eliminados
                val mascotasMentoras = database.mascotaUsuarioDao().obtenerMascotasMentorasActivas()
                val mascotasAActualizar = mutableListOf<MascotaUsuarioEntity>()

                for (mascota in mascotasMentoras) {
                    val tiempoTranscurrido = ahora - mascota.ultima_interaccion
                    val diasPasados = (tiempoTranscurrido / unDiaEnMillis).toInt()

                    // Si ha pasado al menos 1 día completo (24 horas) de inactividad
                    if (diasPasados >= 1) {
                        val puntosDescuento = diasPasados * 10 // 10 puntos por día pasado
                        val nuevaFelicidad = (mascota.felicidad_actual - puntosDescuento).coerceIn(0, 100)

                        // Clonamos la entidad con los nuevos valores de felicidad y actualizamos su control de tiempo
                        mascotasAActualizar.add(
                            mascota.copy(
                                felicidad_actual = nuevaFelicidad,
                                ultima_interaccion = ahora // Actualizamos para que cuente desde hoy el próximo descuento
                            )
                        )
                    }
                }

                // 2. Guardar en bloque todos los cambios en la base de datos si existen registros afectados
                if (mascotasAActualizar.isNotEmpty()) {
                    database.mascotaUsuarioDao().actualizarMascotas(mascotasAActualizar)
                }
            } catch (e: Exception) { /* ignorar */ }
        }
    }

    private fun observarTareasVencidas() {
        viewModelScope.launch {
            // Obtenemos el tiempo actual en milisegundos
            val ahora = System.currentTimeMillis()

            // Escuchamos el Flow de Room de manera reactiva
            database.tareaDao().observarTareasVencidasSinPenalizar(ahora).collect { tareasVencidas ->
                for (tarea in tareasVencidas) {
                    // Determinar el descuento según la prioridad de la tarea (Nivel)
                    // Ajusta los números (1, 2, 3) según cómo manejes tus prioridades
                    val puntosDescuento = when (tarea.prioridad) {
                        3 -> 30    // Prioridad Alta / Nivel 3 -> -30%
                        2 -> 20    // Prioridad Media / Nivel 2 -> -20%
                        else -> 10 // Prioridad Baja / Nivel 1 -> -10%
                    }

                    // 1. Reducimos la felicidad de la mascota mentora asociada al tablero de esta tarea
                    database.mascotaUsuarioDao().reducirFelicidadPorTablero(
                        idTablero = tarea.id_tablero,
                        puntos = puntosDescuento
                    )

                    // 2. Marcamos la tarea como penalizada para que el Flow no la vuelva a traer
                    database.tareaDao().marcarPenalizacionAplicada(tarea.id_tarea)
                }
            }
        }
    }
}