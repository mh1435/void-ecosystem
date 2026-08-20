package com.voidecosystem.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voidecosystem.core.designsystem.component.PillarCard
import com.voidecosystem.core.designsystem.theme.PillarAccents
import com.voidecosystem.core.model.Pillar

object DashboardDestination {
    const val ROUTE = "dashboard"
}

/**
 * The central gateway UI: the "home screen" of the whole ecosystem.
 * Every pillar's modules are grouped under a section header and rendered
 * as a grid of [PillarCard]s. Tapping a tile hands the route string back
 * to the caller (:app's NavHost) — this screen never navigates itself,
 * so it stays trivially previewable and independent of the 19 feature
 * modules it links to.
 */
@Composable
fun DashboardRoute(onModuleClick: (route: String) -> Unit) {
    DashboardScreen(tiles = DashboardRegistry, onModuleClick = onModuleClick)
}

@Composable
fun DashboardScreen(
    tiles: List<DashboardTile>,
    onModuleClick: (route: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val grouped = tiles.groupBy { it.module.pillar }

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
                PillarCard(
                    title = tile.module.title,
                    subtitle = tile.module.subtitle,
                    icon = tile.icon,
                    accent = accent,
                    onClick = { onModuleClick(tile.module.route) },
                )
            }
        }
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
