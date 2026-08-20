package com.voidecosystem.feature.theming

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.voidecosystem.core.designsystem.theme.PillarAccents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ThemingDestination {
    const val ROUTE = "theming"
}

private data class ColorRole(val name: String, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemingRoute(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pickedImage by remember { mutableStateOf<Uri?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isApplying by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri -> pickedImage = uri }

    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            kotlinx.coroutines.delay(2500)
            statusMessage = null
        }
    }

    fun applyImageWallpaper() {
        val uri = pickedImage ?: return
        isApplying = true
        scope.launch {
            statusMessage = withContext(Dispatchers.IO) { setWallpaperFromUri(context, uri) }
            isApplying = false
        }
    }

    fun applySolidWallpaper(color: Color) {
        isApplying = true
        scope.launch {
            statusMessage = withContext(Dispatchers.IO) { setSolidColorWallpaper(context, color) }
            isApplying = false
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val roles = remember(colorScheme) {
        listOf(
            ColorRole("Primary", colorScheme.primary),
            ColorRole("Secondary", colorScheme.secondary),
            ColorRole("Background", colorScheme.background),
            ColorRole("Surface", colorScheme.surfaceVariant),
            ColorRole("Error", colorScheme.error),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theming Engine") },
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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Column {
                    Text("Void palette", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "The color scheme every app in the ecosystem shares.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(roles) { role -> PaletteSwatch(role) }
                }
            }

            item {
                Column {
                    Text("Set wallpaper", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Pick a photo or a flat color and apply it to your home screen — really changes it, right from here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    val uri = pickedImage
                    if (uri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(model = uri),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(14.dp)),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { pickImageLauncher.launch("image/*") }) {
                            Icon(Icons.Filled.Wallpaper, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Choose photo", modifier = Modifier.padding(start = 8.dp))
                        }
                        Button(onClick = ::applyImageWallpaper, enabled = uri != null && !isApplying) {
                            Text("Apply")
                        }
                    }
                }
            }

            item {
                Text("Or a flat color", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(PillarAccents) { accent ->
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(accent)
                                .clickable(enabled = !isApplying) { applySolidWallpaper(accent) },
                        )
                    }
                }
            }

            item {
                val message = statusMessage
                if (message != null) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun PaletteSwatch(role: ColorRole, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(role.color),
        )
        Text(
            text = role.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

private fun setWallpaperFromUri(context: Context, uri: Uri): String = try {
    context.contentResolver.openInputStream(uri)?.use { input ->
        WallpaperManager.getInstance(context).setStream(input)
    }
    "Wallpaper set."
} catch (e: Exception) {
    "Couldn't set wallpaper: ${e.message}"
}

private fun setSolidColorWallpaper(context: Context, color: Color): String = try {
    val manager = WallpaperManager.getInstance(context)
    val width = manager.desiredMinimumWidth.coerceAtLeast(1)
    val height = manager.desiredMinimumHeight.coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.eraseColor(color.toArgb())
    manager.setBitmap(bitmap)
    bitmap.recycle()
    "Wallpaper set."
} catch (e: Exception) {
    "Couldn't set wallpaper: ${e.message}"
}
