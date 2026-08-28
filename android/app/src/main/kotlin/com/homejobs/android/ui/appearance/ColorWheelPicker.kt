package com.homejobs.android.ui.appearance

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * A classic hue/saturation wheel (drag or tap to pick a hue and saturation) plus a brightness
 * slider underneath. [color] seeds the wheel's starting position; every change is reported
 * immediately via [onColorChange] so the caller can apply it live.
 */
@Composable
fun ColorWheelPicker(
    color: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    val startHsv = remember(color) {
        FloatArray(3).also { AndroidColor.colorToHSV(color.toArgb(), it) }
    }
    var hue by remember(color) { mutableStateOf(startHsv[0]) }
    var saturation by remember(color) { mutableStateOf(startHsv[1]) }
    var value by remember(color) { mutableStateOf(startHsv[2]) }

    // The wheel's drag gesture (below) runs in a coroutine that's launched once and lives for
    // as long as this Canvas does — it does NOT restart on every recomposition. Without this,
    // it would keep calling whichever onColorChange closure existed when it first launched,
    // which closed over a since-stale snapshot of the other two colors — so editing one color
    // would silently revert the others back to their value from that earlier moment.
    val currentOnColorChange = rememberUpdatedState(onColorChange)

    fun emit(newHue: Float = hue, newSaturation: Float = saturation, newValue: Float = value) {
        currentOnColorChange.value(Color(AndroidColor.HSVToColor(floatArrayOf(newHue, newSaturation, newValue))))
    }

    val hueRingColors = remember {
        (0..12).map { step -> Color(AndroidColor.HSVToColor(floatArrayOf(step * 30f, 1f, 1f))) }
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .pointerInput(Unit) {
                    fun updateFrom(position: Offset) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = minOf(size.width, size.height) / 2f
                        val delta = position - center
                        var angleDeg = Math.toDegrees(atan2(delta.y, delta.x).toDouble()).toFloat()
                        if (angleDeg < 0f) angleDeg += 360f
                        hue = angleDeg
                        saturation = (delta.getDistance() / radius).coerceIn(0f, 1f)
                        emit()
                    }
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        updateFrom(down.position)
                        var pressed = true
                        while (pressed) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.pressed }
                            if (change != null) {
                                updateFrom(change.position)
                                change.consume()
                            }
                            pressed = event.changes.any { it.pressed }
                        }
                    }
                },
        ) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(brush = Brush.sweepGradient(hueRingColors), radius = radius, center = center)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color.White.copy(alpha = 0f)),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )
            val angleRad = Math.toRadians(hue.toDouble())
            val selectorDistance = saturation * radius
            val selectorPos = Offset(
                x = center.x + (cos(angleRad) * selectorDistance).toFloat(),
                y = center.y + (sin(angleRad) * selectorDistance).toFloat(),
            )
            drawCircle(color = Color.White, radius = 9.dp.toPx(), center = selectorPos, style = Stroke(width = 3.dp.toPx()))
            drawCircle(color = Color.Black.copy(alpha = 0.45f), radius = 9.dp.toPx(), center = selectorPos, style = Stroke(width = 1.dp.toPx()))
        }
        Slider(
            value = value,
            onValueChange = { value = it; emit() },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )
    }
}
