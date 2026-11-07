package com.exemplo.runningtracker.domain.service

import android.location.Location
import android.os.SystemClock
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Estado vivo de uma única corrida. Atualizado pelo serviço na main thread. */
object TrackingSession {
    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()

    private val _pathPoints = MutableStateFlow<List<LatLng>>(emptyList())
    val pathPoints = _pathPoints.asStateFlow()

    private val _timeRunInMillis = MutableStateFlow(0L)
    val timeRunInMillis = _timeRunInMillis.asStateFlow()

    private val _distanceInMeters = MutableStateFlow(0f)
    val distanceInMeters = _distanceInMeters.asStateFlow()

    private val _startedAt = MutableStateFlow(0L)
    val startedAt = _startedAt.asStateFlow()

    private var lastAcceptedLocation: Location? = null
    private var accumulatedTimeMs = 0L
    private var lastResumeRealtimeMs = 0L

    fun begin() {
        if (_isTracking.value) return
        if (_startedAt.value == 0L) _startedAt.value = System.currentTimeMillis()
        lastResumeRealtimeMs = SystemClock.elapsedRealtime()
        _isTracking.value = true
    }

    fun pause() {
        if (!_isTracking.value) return
        tick()
        accumulatedTimeMs = _timeRunInMillis.value
        _isTracking.value = false
        // Não conectar o ponto anterior à pausa com o ponto posterior: filtro simples
        // de distância do addPathPoint descarta saltos grandes.
        lastAcceptedLocation = null
    }

    fun tick() {
        if (_isTracking.value) {
            _timeRunInMillis.value = accumulatedTimeMs +
                (SystemClock.elapsedRealtime() - lastResumeRealtimeMs)
        }
    }

    fun addPathPoint(location: Location) {
        if (!_isTracking.value) return
        // Filtragem mínima para descartar deriva do GPS e saltos.
        if (location.hasAccuracy() && location.accuracy > 45f) return
        val previous = lastAcceptedLocation
        if (previous != null) {
            val stepMeters = previous.distanceTo(location)
            if (stepMeters < 2.5f || stepMeters > 100f) return
            _distanceInMeters.value += stepMeters
        }
        lastAcceptedLocation = Location(location)
        _pathPoints.value = _pathPoints.value + LatLng(location.latitude, location.longitude)
    }

    fun reset() {
        _isTracking.value = false
        _pathPoints.value = emptyList()
        _timeRunInMillis.value = 0L
        _distanceInMeters.value = 0f
        _startedAt.value = 0L
        lastAcceptedLocation = null
        accumulatedTimeMs = 0L
        lastResumeRealtimeMs = 0L
    }
}
