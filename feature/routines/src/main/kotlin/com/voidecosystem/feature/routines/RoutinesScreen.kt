package com.voidecosystem.feature.routines

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voidecosystem.core.ui.EmptyState

object RoutinesDestination {
    const val ROUTE = "routines"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesRoute(
    onBack: () -> Unit = {},
    viewModel: RoutinesViewModel = viewModel(),
) {
    val context = LocalContext.current
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    // Re-check DND access whenever this screen resumes (the grant happens
    // in a Settings screen the user navigates back from).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Routine Scheduler") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("New schedule", modifier = Modifier.padding(start = 8.dp))
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (!viewModel.hasDndAccess) {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Do Not Disturb access needed", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "To actually turn Focus mode on and off, Void needs permission to control Do Not Disturb.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Button(
                            onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)) },
                            modifier = Modifier.padding(top = 12.dp),
                        ) { Text("Grant access") }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        viewModel.lastAppliedMessage ?: "Tap Apply to sync focus mode with your schedules now.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(onClick = viewModel::applyNow) { Text("Apply now") }
                }
            }

            if (schedules.isEmpty()) {
                EmptyState(title = "No schedules yet", message = "Add a time block to get started.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(schedules, key = { it.id }) { schedule ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(schedule.label, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${"%02d:%02d".format(schedule.startHour, schedule.startMinute)} – ${"%02d:%02d".format(schedule.endHour, schedule.endMinute)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(checked = schedule.isEnabled, onCheckedChange = { viewModel.toggleEnabled(schedule) })
                            IconButton(onClick = { viewModel.delete(schedule) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete schedule")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddScheduleDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { label, sh, sm, eh, em ->
                viewModel.addSchedule(label, sh, sm, eh, em)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun AddScheduleDialog(
    onDismiss: () -> Unit,
    onConfirm: (label: String, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) -> Unit,
) {
    var label by remember { mutableStateOf("") }
    var startHour by remember { mutableStateOf("8") }
    var startMinute by remember { mutableStateOf("0") }
    var endHour by remember { mutableStateOf("11") }
    var endMinute by remember { mutableStateOf("30") }

    fun Int?.inRange(max: Int) = this != null && this in 0..max

    val sh = startHour.toIntOrNull()
    val sm = startMinute.toIntOrNull()
    val eh = endHour.toIntOrNull()
    val em = endMinute.toIntOrNull()
    val isValid = label.isNotBlank() && sh.inRange(23) && sm.inRange(59) && eh.inRange(23) && em.inRange(59)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New schedule") },
        text = {
            Column {
                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label") }, singleLine = true)
                Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = startHour, onValueChange = { startHour = it }, label = { Text("Start h") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = startMinute, onValueChange = { startMinute = it }, label = { Text("Start m") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = endHour, onValueChange = { endHour = it }, label = { Text("End h") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = endMinute, onValueChange = { endMinute = it }, label = { Text("End m") }, modifier = Modifier.weight(1f), singleLine = true)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(label, sh ?: 0, sm ?: 0, eh ?: 0, em ?: 0) },
                enabled = isValid,
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
