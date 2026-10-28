package com.vcm.adoptamascota.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vcm.adoptamascota.ui.theme.NaranjaApp

/**
 * Huella dibujada con Canvas: una almohadilla central y cuatro dedos.
 * La pata de perro es más ancha y robusta; la de gato es más pequeña, con dedos más redondeados.
 */
@Composable
fun PawPrint(
    modifier: Modifier = Modifier,
    color: Color = NaranjaApp,
    cat: Boolean = false
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Almohadilla principal
        val padW = if (cat) w * 0.52f else w * 0.60f
        val padH = if (cat) h * 0.38f else h * 0.44f
        drawOval(
            color = color,
            topLeft = Offset((w - padW) / 2f, h * 0.54f),
            size = Size(padW, padH)
        )

        // Dedos (por su centro): los de los extremos más bajos, los del medio más altos
        val toeW = if (cat) w * 0.19f else w * 0.21f
        val toeH = if (cat) h * 0.23f else h * 0.27f
        val centers = listOf(
            Offset(w * 0.13f, h * 0.44f),
            Offset(w * 0.37f, h * 0.22f),
            Offset(w * 0.63f, h * 0.22f),
            Offset(w * 0.87f, h * 0.44f)
        )
        centers.forEach { c ->
            drawOval(
                color = color,
                topLeft = Offset(c.x - toeW / 2f, c.y - toeH / 2f),
                size = Size(toeW, toeH)
            )
        }
    }
}

private data class PawSpot(
    val alignment: Alignment,
    val x: Int,
    val y: Int,
    val size: Int,
    val rotation: Float,
    val cat: Boolean
)

// Posiciones fijas (en dp desde cada borde) para que el fondo se vea igual en cada apertura
private val pawSpots = listOf(
    PawSpot(Alignment.TopStart, 18, 96, 46, -22f, false),
    PawSpot(Alignment.TopEnd, -14, 190, 58, 24f, true),
    PawSpot(Alignment.CenterStart, -14, 70, 66, -34f, true),
    PawSpot(Alignment.CenterEnd, -10, -50, 42, 16f, false),
    PawSpot(Alignment.BottomStart, 26, -170, 54, 20f, false),
    PawSpot(Alignment.BottomEnd, -24, -120, 62, -26f, true)
)

/**
 * Fondo decorativo con huellas de perro y de gato repartidas por la pantalla.
 * Se coloca como primer hijo de un Box para que quede detrás del contenido.
 */
@Composable
fun PawScatter(
    modifier: Modifier = Modifier,
    color: Color = NaranjaApp.copy(alpha = 0.15f)
) {
    Box(modifier = modifier.fillMaxSize()) {
        pawSpots.forEach { spot ->
            PawPrint(
                modifier = Modifier
                    .align(spot.alignment)
                    .offset(spot.x.dp, spot.y.dp)
                    .size(spot.size.dp)
                    .rotate(spot.rotation),
                color = color,
                cat = spot.cat
            )
        }
    }
}
