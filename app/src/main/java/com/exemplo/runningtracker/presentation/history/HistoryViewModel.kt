package com.exemplo.runningtracker.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.exemplo.runningtracker.data.local.RunEntity
import com.exemplo.runningtracker.data.repository.RunRepository
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** O filtro do DatePicker chega como meia-noite UTC e é convertido ao dia local. */
enum class SortType { DATE_ASC, DATE_DESC }

class HistoryViewModel(private val repository: RunRepository) : ViewModel() {
    private val _sortType = MutableStateFlow(SortType.DATE_DESC)
    val sortType = _sortType.asStateFlow()

    private val _dateFilter = MutableStateFlow<Long?>(null)
    val dateFilter = _dateFilter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val runs: StateFlow<List<RunEntity>> = combine(sortType, dateFilter) { sort, date ->
        sort to date
    }.flatMapLatest { (sort, selectedUtcMillis) ->
        if (selectedUtcMillis == null) {
            if (sort == SortType.DATE_DESC) repository.runsDescending()
            else repository.runsAscending()
        } else {
            val date = Instant.ofEpochMilli(selectedUtcMillis)
                .atZone(ZoneOffset.UTC).toLocalDate()
            val zone = ZoneId.systemDefault()
            val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
            val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            repository.runsOnDate(start, end).map { list ->
                if (sort == SortType.DATE_DESC) list else list.reversed()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun loadRuns() { /* Flow reativo: executa automaticamente enquanto a tela observa. */ }
    fun setSortType(type: SortType) { _sortType.value = type }
    fun filterByDate(dateInMillis: Long) { _dateFilter.value = dateInMillis }
    fun clearFilters() {
        _dateFilter.value = null
        _sortType.value = SortType.DATE_DESC
    }

    companion object {
        fun factory(repository: RunRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    HistoryViewModel(repository) as T
            }
    }
}
