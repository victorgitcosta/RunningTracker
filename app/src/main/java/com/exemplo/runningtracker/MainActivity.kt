package com.exemplo.runningtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exemplo.runningtracker.presentation.history.HistoryViewModel
import com.exemplo.runningtracker.presentation.navigation.AppNavigation
import com.exemplo.runningtracker.presentation.tracking.TrackingViewModel
import com.exemplo.runningtracker.ui.theme.RunningTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as RunningTrackerApplication
        setContent {
            val tracking: TrackingViewModel = viewModel(
                factory = TrackingViewModel.factory(application, app.repository)
            )
            val history: HistoryViewModel = viewModel(
                factory = HistoryViewModel.factory(app.repository)
            )
            RunningTheme {
                Surface(Modifier.fillMaxSize()) {
                    AppNavigation(tracking, history)
                }
            }
        }
    }
}
