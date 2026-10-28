package com.vcm.adoptamascota.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Mapeo de la paleta a los roles de Material 3. Los componentes (Button, FAB, TopAppBar,
 * Card, FilterChip...) leen estos roles automáticamente, por eso al cambiarlos aquí
 * cambia toda la app sin tocar cada pantalla.
 *
 *  primary   (naranja)  -> Button, FloatingActionButton, indicadores
 *  secondary (celeste)  -> TopAppBar (la usamos en cada pantalla), botones secundarios
 *  tertiary  (rosado)   -> detalles y categorías
 *  background/surface   -> blanco
 */
private val LightColorScheme = lightColorScheme(
    primary = NaranjaApp,
    onPrimary = BlancoApp,
    primaryContainer = AmarilloSuave,          // tarjetas destacadas
    onPrimaryContainer = TextoOscuro,

    secondary = CelesteApp,
    onSecondary = TextoOscuro,
    secondaryContainer = CelesteSuave,         // chips seleccionados
    onSecondaryContainer = TextoOscuro,

    tertiary = RosadoSuave,
    onTertiary = TextoOscuro,
    tertiaryContainer = RosadoSuave,
    onTertiaryContainer = TextoOscuro,

    background = BlancoApp,
    onBackground = TextoOscuro,
    surface = BlancoApp,
    onSurface = TextoOscuro,
    surfaceVariant = RosadoClaro,
    onSurfaceVariant = TextoSecundario,
    outline = RosadoSuave,
    outlineVariant = RosadoSuave,

    // Las Card de Material 3 usan surfaceContainer*: las dejamos blancas
    surfaceContainerLowest = BlancoApp,
    surfaceContainerLow = BlancoApp,
    surfaceContainer = BlancoApp,
    surfaceContainerHigh = BlancoApp,
    surfaceContainerHighest = BlancoApp,
    surfaceTint = NaranjaApp
)

@Composable
fun VitalMascotTheme(content: @Composable () -> Unit) {
    // Un solo esquema claro y sin color dinámico: la paleta se ve igual en todos los equipos
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
