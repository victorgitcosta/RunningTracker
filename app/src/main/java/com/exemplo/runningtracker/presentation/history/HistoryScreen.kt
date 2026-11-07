package com.exemplo.runningtracker.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exemplo.runningtracker.data.local.RunEntity
import com.exemplo.runningtracker.domain.util.RunFormatters
import com.exemplo.runningtracker.ui.theme.Ink
import com.exemplo.runningtracker.ui.theme.Lime
import com.exemplo.runningtracker.ui.theme.LimeSoft
import com.exemplo.runningtracker.ui.theme.Muted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val runs by viewModel.runs.collectAsStateWithLifecycle()
    val sortType by viewModel.sortType.collectAsStateWithLifecycle()
    val dateFilter by viewModel.dateFilter.collectAsStateWithLifecycle()
    var openPicker by remember { mutableStateOf(false) }

    if (openPicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = dateFilter)
        DatePickerDialog(
            onDismissRequest = { openPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let(viewModel::filterByDate)
                    openPicker = false
                }) { Text("Filtrar") }
            },
            dismissButton = {
                TextButton(onClick = { openPicker = false }) { Text("Cancelar") }
            }
        ) { DatePicker(state = dateState) }
    }

    Column(Modifier.fillMaxSize().background(Color.White).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.DirectionsRun, contentDescription = null,
                tint = Ink,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                    .background(LimeSoft).padding(9.dp)
            )
            Spacer(Modifier.size(12.dp))
            Column {
                Text("Oi, corredor(a)!", fontWeight = FontWeight.ExtraBold,
                    color = Ink, fontSize = 27.sp)
                Text("Vamos correr hoje?", fontSize = 14.sp, color = Muted)
            }
        }
        Spacer(Modifier.height(25.dp))
        SummaryCard(runs)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Minhas corridas", color = Ink, fontWeight = FontWeight.Bold,
                fontSize = 20.sp, modifier = Modifier.weight(1f))
            if (dateFilter != null) {
                IconButton(onClick = viewModel::clearFilters) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpar filtros")
                }
            }
            IconButton(onClick = { openPicker = true }) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Filtrar por data", tint = Ink)
            }
            IconButton(onClick = {
                viewModel.setSortType(
                    if (sortType == SortType.DATE_DESC) SortType.DATE_ASC else SortType.DATE_DESC
                )
            }) {
                Icon(Icons.Default.Sort, contentDescription = "Alternar ordem", tint = Ink)
            }
        }
        Text(
            if (dateFilter != null) "Filtrado por data • " +
                (if (sortType == SortType.DATE_DESC) "mais recentes primeiro" else "mais antigas primeiro")
            else if (sortType == SortType.DATE_DESC) "Mais recentes primeiro" else "Mais antigas primeiro",
            color = Muted, fontSize = 12.sp
        )
        Spacer(Modifier.height(12.dp))
        if (runs.isEmpty()) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.DirectionsRun, null, tint = Lime, modifier = Modifier.size(50.dp))
                Spacer(Modifier.height(12.dp))
                Text("Nenhuma corrida encontrada", fontWeight = FontWeight.Bold, color = Ink)
                Text("Salve uma corrida na tela Mapa para vê-la aqui.",
                    color = Muted, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(runs, key = { it.id }) { run -> RunItem(run) }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun SummaryCard(runs: List<RunEntity>) {
    val distance = runs.sumOf { it.distanceInMeters.toDouble() }.toFloat()
    val calories = runs.sumOf { it.caloriesBurned }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = LimeSoft)
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("RESUMO DAS CORRIDAS EXIBIDAS", color = Muted, fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryValue("Corridas", "${runs.size}")
                SummaryValue("Distância", "${RunFormatters.distance(distance)} km")
                SummaryValue("Calorias", "$calories kcal")
            }
        }
    }
}

@Composable
private fun SummaryValue(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = Muted)
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, color = Ink, fontSize = 17.sp)
    }
}

@Composable
fun RunItem(run: RunEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAF7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(17.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = Ink,
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                        .background(Lime).padding(8.dp))
                Spacer(Modifier.size(12.dp))
                Column {
                    Text("Corrida #${run.id}", fontWeight = FontWeight.Bold,
                        fontSize = 16.sp, color = Ink)
                    Text(RunFormatters.date(run.timestamp), fontSize = 12.sp, color = Muted)
                }
            }
            Spacer(Modifier.height(13.dp))
            HorizontalDivider(color = Color(0xFFE6E9E3))
            Spacer(Modifier.height(13.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                RunItemMetric("Distância", "${RunFormatters.distance(run.distanceInMeters)} km")
                RunItemMetric("Tempo", RunFormatters.duration(run.durationInMillis))
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                RunItemMetric("Calorias estim.", "${run.caloriesBurned} kcal")
                RunItemMetric("Pace médio", RunFormatters.pace(run.avgPace))
            }
        }
    }
}

@Composable
private fun RunItemMetric(label: String, value: String) {
    Column(Modifier.size(width = 138.dp, height = 40.dp)) {
        Text(label, color = Muted, fontSize = 11.sp)
        Text(value, color = Ink, fontWeight = FontWeight.Bold, fontSize = 17.sp)
    }
}
