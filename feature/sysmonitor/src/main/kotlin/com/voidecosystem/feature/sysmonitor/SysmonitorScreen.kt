package com.voidecosystem.feature.sysmonitor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

object SysmonitorDestination {
    const val ROUTE = "sysmonitor"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SysmonitorRoute(
    onBack: () -> Unit = {},
    viewModel: SysmonitorViewModel = viewModel(),
) {
    val state = viewModel.uiState
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Monitor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                MonitorCard(title = "Memory") {
                    StatRow("Used", "${state.usedRamPercent}%")
                    LinearProgressIndicator(
                        progress = { state.usedRamPercent / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    StatRow("Available", "${state.availRamMb} MB / ${state.totalRamMb} MB")
                    StatRow("Low-memory threshold", "${state.lowMemThresholdMb} MB")
                    StatRow("Low-memory state", if (state.lowMemory) "Yes" else "No")
                }
            }
            item {
                MonitorCard(title = "This App's Process") {
                    StatRow("CPU time (elapsed)", "${state.appCpuTimeMs} ms")
                    StatRow("Heap used", "${state.appHeapUsedMb} MB / ${state.appHeapMaxMb} MB")
                    StatRow("CPU cores available", "${state.cpuCores}")
                    StatRow("Visible processes", "${state.runningProcessCount}")
                }
            }
            item {
                MonitorCard(title = "Battery") {
                    StatRow("Level", "${state.batteryPercent}%")
                    LinearProgressIndicator(
                        progress = { state.batteryPercent / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    StatRow("Status", state.batteryStatus)
                    StatRow("Health", state.batteryHealth)
                    StatRow("Temperature", "${state.temperatureC} °C")
                    StatRow("Voltage", "${state.voltageMv} mV")
                }
            }
            item {
                Text(
                    text = "Android doesn't expose other apps' CPU usage or memory to third-party apps without root — the numbers above are everything a normal app can legitimately read.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun MonitorCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
