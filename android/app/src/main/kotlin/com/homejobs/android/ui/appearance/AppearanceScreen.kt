package com.homejobs.android.ui.appearance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.homejobs.android.ui.theme.CustomColors
import com.homejobs.android.ui.theme.ThemeMode

private enum class ColorRole(val label: String) {
    PRIMARY("Primary"),
    SECONDARY("Secondary"),
    ACCENT("Accent"),
}

/**
 * A relaxed, browsable settings screen: a Light/Dark/Custom toggle, and — only when Custom is
 * selected — three color swatches you pick from a color wheel. Every change applies immediately
 * everywhere else in the app, so there's nothing to "save".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    onBack: () -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    customColors: CustomColors,
    onCustomColorsChange: (CustomColors) -> Unit,
) {
    var editingRole by remember { mutableStateOf(ColorRole.PRIMARY) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Theme", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = themeMode == mode,
                            onClick = { onThemeModeChange(mode) },
                            label = { Text(mode.label) },
                        )
                    }
                }
            }

            if (themeMode == ThemeMode.CUSTOM) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Custom colors", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Pick a swatch, then drag on the wheel to explore — it applies live.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        ColorRole.entries.forEach { role ->
                            RoleSwatch(
                                role = role,
                                color = customColors.colorFor(role),
                                selected = editingRole == role,
                                onClick = { editingRole = role },
                            )
                        }
                    }
                    ColorWheelPicker(
                        color = customColors.colorFor(editingRole),
                        onColorChange = { newColor -> onCustomColorsChange(customColors.with(editingRole, newColor)) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

private fun CustomColors.colorFor(role: ColorRole): Color = when (role) {
    ColorRole.PRIMARY -> primary
    ColorRole.SECONDARY -> secondary
    ColorRole.ACCENT -> tertiary
}

private fun CustomColors.with(role: ColorRole, color: Color): CustomColors = when (role) {
    ColorRole.PRIMARY -> copy(primary = color)
    ColorRole.SECONDARY -> copy(secondary = color)
    ColorRole.ACCENT -> copy(tertiary = color)
}

@Composable
private fun RoleSwatch(role: ColorRole, color: Color, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = CircleShape,
                )
                .clickable(onClick = onClick),
        ) {}
        Text(role.label, style = MaterialTheme.typography.labelSmall)
    }
}
