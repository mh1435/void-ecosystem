package com.voidecosystem.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.voidecosystem.core.designsystem.component.PillarCard
import com.voidecosystem.core.designsystem.component.TileAction
import com.voidecosystem.core.designsystem.theme.PillarAccents
import com.voidecosystem.core.model.AppInstallState
import com.voidecosystem.core.model.Pillar
import com.voidecosystem.core.model.SilentInstallStatus

/**
 * The central gateway UI: the "home screen" of the whole ecosystem, styled
 * like a personal app store. Every pillar's modules are grouped under a
 * section header and rendered as a grid of [PillarCard]s. Each tile is a
 * *separately installed app* — tapping one hands its route back to the
 * caller (:app's MainActivity), which launches it if installed or
 * downloads+installs it in-app (silently via Shizuku when available) if
 * not. [installStates] is optional and Android-free (a plain map keyed by
 * route), so this screen stays trivially previewable without :app.
 */
@Composable
fun DashboardRoute(
    installStates: Map<String, AppInstallState> = emptyMap(),
    onModuleClick: (route: String, packageName: String) -> Unit,
    onUpdateAll: () -> Unit = {},
    silentInstallStatus: SilentInstallStatus = SilentInstallStatus.UNAVAILABLE,
    onEnableSilentInstalls: () -> Unit = {},
) {
    DashboardScreen(
        tiles = DashboardRegistry,
        installStates = installStates,
        onModuleClick = onModuleClick,
        onUpdateAll = onUpdateAll,
        silentInstallStatus = silentInstallStatus,
        onEnableSilentInstalls = onEnableSilentInstalls,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    tiles: List<DashboardTile>,
    installStates: Map<String, AppInstallState>,
    onModuleClick: (route: String, packageName: String) -> Unit,
    modifier: Modifier = Modifier,
    onUpdateAll: () -> Unit = {},
    silentInstallStatus: SilentInstallStatus = SilentInstallStatus.UNAVAILABLE,
    onEnableSilentInstalls: () -> Unit = {},
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(tiles, query) {
        if (query.isBlank()) {
            tiles
        } else {
            tiles.filter { it.module.title.contains(query, ignoreCase = true) }
        }
    }
    val grouped = filtered.groupBy { it.module.pillar }
    val updatable = tiles.count { installStates[it.module.route] is AppInstallState.UpdateAvailable }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(2) }) {
            DashboardHeader()
        }

        item(span = { GridItemSpan(2) }) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search the ecosystem") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                ),
            )
        }

        item(span = { GridItemSpan(2) }) {
            AnimatedVisibility(visible = updatable > 0) {
                UpdateAllBanner(count = updatable, onUpdateAll = onUpdateAll)
            }
        }

        item(span = { GridItemSpan(2) }) {
            AnimatedVisibility(visible = silentInstallStatus == SilentInstallStatus.NEEDS_PERMISSION) {
                ShizukuBanner(onEnable = onEnableSilentInstalls)
            }
        }

        Pillar.entries.forEach { pillar ->
            val pillarTiles = grouped[pillar] ?: return@forEach

            item(span = { GridItemSpan(2) }) {
                Text(
                    text = pillar.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                )
            }

            items(pillarTiles, key = { it.module.route }) { tile ->
                val accent = PillarAccents[tiles.indexOf(tile) % PillarAccents.size]
                val state = installStates[tile.module.route]
                val (subtitle, progress, action) = when (state) {
                    is AppInstallState.Downloading ->
                        Triple("Downloading… ${(state.progress * 100).toInt()}%", state.progress, TileAction.WORKING)
                    AppInstallState.Installing ->
                        Triple("Installing…", null, TileAction.WORKING)
                    AppInstallState.NotInstalled ->
                        Triple(tile.module.subtitle, null, TileAction.GET)
                    is AppInstallState.UpdateAvailable ->
                        Triple("Update available", null, TileAction.UPDATE)
                    AppInstallState.Installed, null ->
                        Triple(tile.module.subtitle, null, TileAction.OPEN)
                }
                PillarCard(
                    title = tile.module.title,
                    subtitle = subtitle,
                    icon = tile.icon,
                    accent = accent,
                    action = action,
                    downloadProgress = progress,
                    onClick = { onModuleClick(tile.module.route, tile.module.packageName) },
                )
            }
        }
    }
}

@Composable
private fun UpdateAllBanner(count: Int, onUpdateAll: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.SystemUpdateAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = if (count == 1) "1 update available" else "$count updates available",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
        Button(onClick = onUpdateAll) { Text("Update all") }
    }
}

@Composable
private fun ShizukuBanner(onEnable: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text("Silent installs available", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Shizuku is paired — grant permission to skip install prompts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        TextButton(onClick = onEnable) { Text("Enable") }
    }
}

@Composable
private fun DashboardHeader(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(
            text = "Void Ecosystem",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Your entire OS, one dashboard.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
