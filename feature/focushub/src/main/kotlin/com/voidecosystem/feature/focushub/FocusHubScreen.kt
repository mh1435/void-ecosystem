package com.voidecosystem.feature.focushub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voidecosystem.core.ui.EmptyState
import java.text.DateFormat
import java.util.Date

object FocushubDestination {
    const val ROUTE = "focushub"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocushubRoute(
    onBack: () -> Unit = {},
    viewModel: FocusHubViewModel = viewModel(),
) {
    val timerState = viewModel.timerState
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Focus Hub") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = if (timerState.mode == TimerMode.FOCUS) "Focus" else "Break",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Box(
                        modifier = Modifier.padding(vertical = 24.dp).size(220.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            progress = {
                                if (timerState.totalSecondsForMode == 0) {
                                    0f
                                } else {
                                    timerState.secondsRemaining.toFloat() / timerState.totalSecondsForMode.toFloat()
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 8.dp,
                        )
                        Text(
                            text = formatSeconds(timerState.secondsRemaining),
                            style = MaterialTheme.typography.headlineLarge,
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedIconButton(onClick = viewModel::reset) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Reset")
                        }
                        FilledIconButton(onClick = viewModel::toggleRunning, modifier = Modifier.size(64.dp)) {
                            Icon(
                                if (timerState.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (timerState.isRunning) "Pause" else "Start",
                            )
                        }
                    }

                    Text(
                        text = "${viewModel.todaysSessionCount()} focus sessions today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }

            if (sessions.isEmpty()) {
                item {
                    EmptyState(
                        title = "No sessions yet",
                        message = "Complete a focus session to start your history.",
                    )
                }
            } else {
                item {
                    Text(
                        "History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                    )
                }
                items(sessions, key = { it.id }) { session ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp),
                    ) {
                        Text("${session.durationMinutes} min", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(session.completedAt)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
