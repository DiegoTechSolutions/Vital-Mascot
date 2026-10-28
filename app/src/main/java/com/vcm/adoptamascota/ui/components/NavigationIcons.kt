package com.vcm.adoptamascota.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Botón "Volver" de las barras superiores: una flechita junto a una garrita de perro.
 * La flecha mantiene claro que sirve para regresar; la garrita le da la identidad de la app.
 * Toma el color del contenido de la barra, así se ve bien sobre cualquier fondo.
 */
@Composable
fun PawBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String = "Volver"
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .testTag("back_button")
            .semantics { contentDescription = description }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            PawPrint(modifier = Modifier.size(24.dp), color = LocalContentColor.current)
        }
    }
}
