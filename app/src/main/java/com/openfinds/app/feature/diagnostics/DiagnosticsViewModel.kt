package com.openfinds.app.feature.diagnostics

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openfinds.app.core.diagnostics.LogBuffer
import com.openfinds.app.core.diagnostics.LogEntry
import com.openfinds.app.core.diagnostics.LogExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiagnosticsViewModel
    @Inject
    constructor(
        private val logBuffer: LogBuffer,
        private val logExporter: LogExporter,
    ) : ViewModel() {
        val entries: StateFlow<List<LogEntry>> =
            logBuffer.entries
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        fun clearLogs() {
            logBuffer.clear()
        }

        fun exportLogs(onReady: (Uri) -> Unit) {
            viewModelScope.launch { onReady(logExporter.export()) }
        }
    }
