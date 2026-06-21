package com.krishifarms.mobile.feature.worker.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.core.common.Resource
import com.krishifarms.mobile.core.database.entity.AttendanceStatus
import com.krishifarms.mobile.feature.worker.domain.model.Attendance
import com.krishifarms.mobile.feature.worker.domain.model.DateUtils
import com.krishifarms.mobile.feature.worker.domain.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AttendanceUiState(
    val selectedDate: Long = DateUtils.startOfDay(System.currentTimeMillis()),
    val records: List<Attendance> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
) : ViewModel() {

    private val selectedDate = MutableStateFlow(DateUtils.startOfDay(System.currentTimeMillis()))
    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AttendanceUiState> = combine(
        selectedDate,
        isRefreshing,
        errorMessage,
        selectedDate.flatMapLatest { date ->
            attendanceRepository.observeAttendanceForDate(date)
        },
    ) { date, refreshing, error, records ->
        AttendanceUiState(
            selectedDate = date,
            records = records,
            isRefreshing = refreshing,
            errorMessage = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AttendanceUiState())

    fun onDateChanged(dateMillis: Long) {
        selectedDate.value = DateUtils.startOfDay(dateMillis)
    }

    fun markStatus(workerId: String, status: AttendanceStatus) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val checkIn = if (status == AttendanceStatus.PRESENT || status == AttendanceStatus.HALF_DAY) now else null
            val checkOut = if (status == AttendanceStatus.ABSENT) null else now
            when (
                val result = attendanceRepository.markAttendance(
                    workerId = workerId,
                    date = selectedDate.value,
                    status = status,
                    checkIn = checkIn,
                    checkOut = checkOut,
                )
            ) {
                is Resource.Error -> errorMessage.value = result.message
                else -> Unit
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            when (val result = attendanceRepository.syncAttendance()) {
                is Resource.Error -> errorMessage.value = result.message
                else -> Unit
            }
            isRefreshing.value = false
        }
    }
}
