package com.schoolstats.data.sync

import com.schoolstats.domain.model.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SyncState(
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false,
    val pendingCount: Int = 0,
    val failedCount: Int = 0,
    val lastError: String? = null,
)

class SyncManager(
    private val networkMonitor: NetworkMonitor,
    private val onSyncRequested: suspend () -> Result<Unit>,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(SyncState())
    val state: StateFlow<SyncState> = _state.asStateFlow()

    fun start() {
        scope.launch {
            networkMonitor.isOnline.collect { online ->
                _state.value = _state.value.copy(isOnline = online)
                if (online) syncNow()
            }
        }
    }

    fun scheduleSync() {
        scope.launch { syncNow() }
    }

    suspend fun syncNow(): Result<Unit> {
        if (!_state.value.isOnline) return Result.failure(IllegalStateException("Hors ligne"))
        _state.value = _state.value.copy(isSyncing = true, lastError = null)
        return onSyncRequested()
            .onSuccess {
                _state.value = _state.value.copy(
                    isSyncing = false,
                    pendingCount = 0,
                    failedCount = 0,
                    lastError = null,
                )
            }
            .onFailure { error ->
                _state.value = _state.value.copy(
                    isSyncing = false,
                    failedCount = _state.value.failedCount + 1,
                    lastError = error.message,
                )
            }
    }
}

class NetworkMonitor {
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    fun setOnline(online: Boolean) {
        _isOnline.value = online
    }

    suspend fun checkConnectivity(check: suspend () -> Boolean) {
        _isOnline.value = runCatching { check() }.getOrDefault(false)
    }
}

fun SyncStatus.isEditable(): Boolean = this in setOf(
    SyncStatus.PENDING_SYNC,
    SyncStatus.SYNCED,
    SyncStatus.SYNC_FAILED,
)
