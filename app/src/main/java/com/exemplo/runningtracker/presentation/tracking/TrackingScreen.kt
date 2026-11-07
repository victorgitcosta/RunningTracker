package com.exemplo.runningtracker.presentation.tracking

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exemplo.runningtracker.domain.util.RunFormatters
import com.exemplo.runningtracker.ui.theme.Ink
import com.exemplo.runningtracker.ui.theme.Lime
import com.exemplo.runningtracker.ui.theme.LimeSoft
import com.exemplo.runningtracker.ui.theme.Muted
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

@Composable
fun TrackingScreen(viewModel: TrackingViewModel, onRunSaved: () -> Unit) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val error by viewModel.errorMessage.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var locationGranted by remember { mutableStateOf(hasLocationPermission(context)) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        locationGranted = hasLocationPermission(context)
        if (locationGranted) viewModel.toggleTracking()
        else scope.launch { snackbarHost.showSnackbar("Conceda acesso à localização para iniciar.") }
    }

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHost.showSnackbar(error ?: "Erro")
            viewModel.clearError()
        }
    }

    Column(Modifier.fillMaxSize().background(Color.White)) {
        RunHeader(state)
        Box(Modifier.weight(1f).fillMaxWidth()) {
            RunMap(state.pathPoints, locationGranted)
            Box(
                modifier = Modifier.align(Alignment.TopStart).padding(18.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.95f))
                    .padding(horizontal = 13.dp, vertical = 9.dp)
            ) {
                Text(
                    text = when {
                        state.isTracking -> "● Registrando percurso"
                        state.hasRun -> "◉ Corrida pausada"
                        else -> "GPS • Pronto para correr"
                    },
                    fontSize = 12.sp, color = Ink, fontWeight = FontWeight.Medium
                )
            }
            Column(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MetricsCard(
                    state = state,
                    onToggle = {
                        if (state.isTracking || hasLocationPermission(context)) {
                            viewModel.toggleTracking()
                        } else {
                            val requested = mutableListOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                requested += Manifest.permission.POST_NOTIFICATIONS
                            }
                            launcher.launch(requested.toTypedArray())
                        }
                    },
                    onSave = { viewModel.finishRun(onRunSaved) }
                )
            }
            SnackbarHost(
                hostState = snackbarHost,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp)
            )
        }
    }
}

@Composable
private fun RunHeader(state: TrackingUiState) {
    Row(
        Modifier.fillMaxWidth().background(Color.White)
            .padding(horizontal = 22.dp, vertical = 19.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(LimeSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = Ink,
                modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.size(14.dp))
        Column {
            Text("Minha rota", color = Muted, fontSize = 12.sp)
            Text("Park Running Route", fontWeight = FontWeight.Bold, fontSize = 19.sp, color = Ink)
            Text(
                if (state.hasRun) RunFormatters.date(state.startTimestamp)
                else "Aguardando início",
                color = Muted, fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun RunMap(points: List<LatLng>, locationGranted: Boolean) {
    val context = LocalContext.current
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-15.7801, -47.9292), 12f)
    }
    LaunchedEffect(locationGranted) {
        if (locationGranted && points.isEmpty() && hasLocationPermission(context)) {
            try {
                LocationServices.getFusedLocationProviderClient(context).lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null && points.isEmpty()) {
                            cameraState.move(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(location.latitude, location.longitude), 15f
                                )
                            )
                        }
                    }
            } catch (_: SecurityException) { /* Mapa permanece no ponto padrão. */ }
        }
    }
    LaunchedEffect(points.size) {
        points.lastOrNull()?.let { last ->
            cameraState.animate(CameraUpdateFactory.newLatLngZoom(last, 16f), 550)
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraState,
        properties = MapProperties(isMyLocationEnabled = locationGranted),
        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
    ) {
        if (points.size >= 2) Polyline(points = points, color = Ink, width = 13f)
        points.firstOrNull()?.let {
            Marker(state = MarkerState(it), title = "Início da corrida")
        }
        if (points.size > 1) points.lastOrNull()?.let {
            Marker(state = MarkerState(it), title = "Posição mais recente")
        }
    }
}

@Composable
private fun MetricsCard(state: TrackingUiState, onToggle: () -> Unit, onSave: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FilledIconButton(
                onClick = onToggle,
                modifier = Modifier.size(68.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Lime)
            ) {
                Icon(
                    if (state.isTracking) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (state.isTracking) "Pausar" else "Iniciar ou retomar",
                    modifier = Modifier.size(31.dp), tint = Ink
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                RunFormatters.duration(state.currentTimeInMillis),
                color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 46.sp
            )
            Text(
                when {
                    state.isTracking -> "Toque para pausar"
                    state.hasRun -> "Corrida pausada — pode retomar ou salvar"
                    else -> "Toque para começar a correr"
                },
                fontSize = 12.sp, color = Muted, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(15.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile("Distância", "${RunFormatters.distance(state.currentDistance)} km", Modifier.weight(1f))
                MetricTile("Calorias¹", "${state.calories} kcal", Modifier.weight(1f))
            }
            Spacer(Modifier.height(9.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile("Pace médio", RunFormatters.pace(state.currentPace), Modifier.weight(1f))
                MetricTile("BPM demo", if (state.hasRun) "${state.currentBpm}" else "--", Modifier.weight(1f))
            }
            if (state.hasRun && !state.isTracking) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onSave,
                    enabled = state.currentTimeInMillis >= 1_000L,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Ink)
                ) {
                    Text("Finalizar e salvar", fontWeight = FontWeight.Bold)
                }
            }
            Text("¹ Calorias estimadas para 70 kg", color = Muted, fontSize = 10.sp,
                modifier = Modifier.padding(top = 7.dp))
        }
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(15.dp)).background(LimeSoft)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(label, color = Muted, fontSize = 11.sp)
        Text(value, color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
