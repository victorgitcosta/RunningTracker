package com.exemplo.runningtracker.domain.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.exemplo.runningtracker.MainActivity
import com.exemplo.runningtracker.R
import com.exemplo.runningtracker.domain.util.RunFormatters
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Mantém captura de GPS com a tela apagada enquanto a corrida está ativa. */
class TrackingService : Service() {
    companion object {
        const val ACTION_START = "com.exemplo.runningtracker.START"
        const val ACTION_PAUSE = "com.exemplo.runningtracker.PAUSE"
        const val ACTION_STOP = "com.exemplo.runningtracker.STOP"
        private const val CHANNEL_ID = "running_tracking_channel"
        private const val NOTIFICATION_ID = 4401

        fun intent(context: Context, action: String): Intent =
            Intent(context, TrackingService::class.java).setAction(action)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val fusedClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private var timerJob: Job? = null
    private var updatesRequested = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.forEach(::addPathPoint)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTrackingService()
            ACTION_PAUSE -> pauseTrackingService()
            ACTION_STOP -> {
                pauseTrackingService()
                TrackingSession.reset()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }
        // Não reiniciar sozinho após morte do processo: exige nova ação explícita.
        return START_NOT_STICKY
    }

    private fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun startTrackingService() {
        if (!hasPermission()) {
            stopSelf()
            return
        }
        try {
            ServiceCompat.startForeground(
                this, NOTIFICATION_ID, buildNotification(),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
            )
            TrackingSession.begin()
            startLocationUpdates()
            timerJob?.cancel()
            timerJob = scope.launch {
                while (isActive) {
                    TrackingSession.tick()
                    delay(250L)
                }
            }
            refreshNotification()
        } catch (_: SecurityException) {
            pauseTrackingService()
            stopSelf()
        } catch (_: IllegalStateException) {
            pauseTrackingService()
            stopSelf()
        }
    }

    private fun pauseTrackingService() {
        timerJob?.cancel()
        timerJob = null
        TrackingSession.pause()
        stopLocationUpdates()
        refreshNotification()
    }

    private fun startLocationUpdates() {
        if (updatesRequested || !hasPermission()) return
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2_000L)
            .setMinUpdateIntervalMillis(1_000L)
            .setMinUpdateDistanceMeters(2f)
            .build()
        try {
            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
                .addOnFailureListener { updatesRequested = false }
            updatesRequested = true
        } catch (_: SecurityException) {
            updatesRequested = false
        }
    }

    private fun stopLocationUpdates() {
        if (updatesRequested) {
            fusedClient.removeLocationUpdates(locationCallback)
            updatesRequested = false
        }
    }

    private fun addPathPoint(location: Location) = TrackingSession.addPathPoint(location)

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Corrida em andamento",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Monitora GPS e cronômetro durante a corrida" }
        )
    }

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(if (TrackingSession.isTracking.value) "Corrida em andamento" else "Corrida pausada")
            .setContentText("Tempo: ${RunFormatters.duration(TrackingSession.timeRunInMillis.value)}")
            .setContentIntent(openApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun refreshNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    override fun onDestroy() {
        timerJob?.cancel()
        stopLocationUpdates()
        TrackingSession.pause()
        scope.cancel()
        super.onDestroy()
    }
}
