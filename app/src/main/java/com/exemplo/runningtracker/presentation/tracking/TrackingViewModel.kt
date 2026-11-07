package com.exemplo.runningtracker.presentation.tracking

import android.app.Application
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.exemplo.runningtracker.data.local.RouteJsonConverter
import com.exemplo.runningtracker.data.local.RunEntity
import com.exemplo.runningtracker.data.repository.RunRepository
import com.exemplo.runningtracker.domain.model.RoutePoint
import com.exemplo.runningtracker.domain.service.TrackingService
import com.exemplo.runningtracker.domain.service.TrackingSession
import com.exemplo.runningtracker.domain.util.MetricsCalculator
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Todos os dados da tela são derivados dos StateFlows publicados pelo serviço. */
data class TrackingUiState(
    val isTracking: Boolean = false,
    val pathPoints: List<LatLng> = emptyList(),
    val currentTimeInMillis: Long = 0L,
    val currentDistance: Float = 0f,
    val calories: Int = 0,
    val currentPace: Float = 0f,
    val currentBpm: Int = 0,
    val startTimestamp: Long = 0L
) {
    val hasRun: Boolean get() = startTimestamp != 0L
}

class TrackingViewModel(
    private val application: Application,
    private val repository: RunRepository
) : ViewModel() {

    val isTracking = TrackingSession.isTracking
    val pathPoints = TrackingSession.pathPoints
    val currentTimeInMillis = TrackingSession.timeRunInMillis
    val currentDistance = TrackingSession.distanceInMeters

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    val uiState: StateFlow<TrackingUiState> = combine(
        isTracking, pathPoints, currentTimeInMillis, currentDistance, TrackingSession.startedAt
    ) { tracking, path, duration, distance, startedAt ->
        TrackingUiState(
            isTracking = tracking,
            pathPoints = path,
            currentTimeInMillis = duration,
            currentDistance = distance,
            calories = MetricsCalculator.calories(distance),
            currentPace = MetricsCalculator.paceMinutesPerKm(duration, distance),
            currentBpm = if (startedAt > 0) MetricsCalculator.DEMO_BPM else 0,
            startTimestamp = startedAt
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TrackingUiState())

    fun toggleTracking() {
        if (TrackingSession.isTracking.value) {
            application.startService(TrackingService.intent(application, TrackingService.ACTION_PAUSE))
        } else {
            // A Activity deve pedir a permissão antes de chamar esse método.
            ContextCompat.startForegroundService(
                application,
                TrackingService.intent(application, TrackingService.ACTION_START)
            )
        }
    }

    fun calculateMetrics(): TrackingUiState = uiState.value

    fun finishRun(onSaved: () -> Unit) {
        val state = uiState.value
        if (state.isTracking) {
            _errorMessage.value = "Pause a corrida antes de salvar."
            return
        }
        if (!state.hasRun || state.currentTimeInMillis < 1_000L) {
            _errorMessage.value = "Registre pelo menos um segundo antes de salvar."
            return
        }
        viewModelScope.launch {
            try {
                val routeJson = RouteJsonConverter().fromPoints(
                    state.pathPoints.map { RoutePoint(it.latitude, it.longitude) }
                )
                repository.insertRun(
                    RunEntity(
                        timestamp = state.startTimestamp,
                        durationInMillis = state.currentTimeInMillis,
                        distanceInMeters = state.currentDistance,
                        caloriesBurned = state.calories,
                        avgPace = state.currentPace,
                        avgBpm = state.currentBpm,
                        routeCoordinates = routeJson
                    )
                )
                application.startService(
                    TrackingService.intent(application, TrackingService.ACTION_STOP)
                )
                onSaved()
            } catch (error: Exception) {
                _errorMessage.value = "Erro ao salvar: ${error.localizedMessage ?: "desconhecido"}"
            }
        }
    }

    fun clearError() { _errorMessage.value = null }

    companion object {
        fun factory(app: Application, repository: RunRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    TrackingViewModel(app, repository) as T
            }
    }
}
