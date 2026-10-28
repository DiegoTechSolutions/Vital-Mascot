package com.vcm.adoptamascota.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import com.vcm.adoptamascota.ui.components.PawBackButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.vcm.adoptamascota.ui.components.mapsAvailable
import com.vcm.adoptamascota.utils.Coordinates
import com.vcm.adoptamascota.utils.DEFAULT_COORDINATES
import com.vcm.adoptamascota.utils.fetchCurrentLocation
import kotlinx.coroutines.launch

/** Qué ubicación se está eligiendo en el selector. */
enum class LocationTarget { FOUND, LOST }

/**
 * Selector de ubicación: el usuario toca el mapa para colocar el marcador y confirma.
 * Sin clave de Google Maps muestra un aviso (la ubicación actual se puede usar desde el formulario).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
    target: LocationTarget,
    initial: Coordinates?,
    onConfirm: (Coordinates) -> Unit,
    onBack: () -> Unit
) {
    val title = if (target == LocationTarget.FOUND) "¿Dónde fue encontrada?" else "¿Dónde se extravió?"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    PawBackButton(onClick = onBack)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.onSecondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        }
    ) { padding ->
        if (!mapsAvailable) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Mapa no disponible", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Esta versión de la app no tiene configurada la clave de Google Maps. " +
                        "Puedes usar el botón \"Usar mi ubicación actual\" del formulario.",
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onBack) { Text("Volver") }
            }
            return@Scaffold
        }

        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val start = initial ?: DEFAULT_COORDINATES
        var selected by remember { mutableStateOf(initial) }
        val cameraState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(start.lat, start.lng), if (initial != null) 16f else 12f)
        }

        // Mueve el mapa y el marcador a la ubicación actual del dispositivo
        fun goToMyLocation() {
            scope.launch {
                fetchCurrentLocation(context)?.let { here ->
                    selected = here
                    cameraState.animate(
                        com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(LatLng(here.lat, here.lng), 17f)
                    )
                }
            }
        }
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result -> if (result.values.any { it }) goToMyLocation() }

        Box(Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraState,
                onMapClick = { selected = Coordinates(it.latitude, it.longitude) }
            ) {
                selected?.let { Marker(state = MarkerState(LatLng(it.lat, it.lng)), title = title) }
            }

            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                tonalElevation = 6.dp,
                shape = MaterialTheme.shapes.large
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        selected?.let { "Ubicación elegida: ${it.format()}" } ?: "Toca el mapa para colocar el marcador",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.MyLocation, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Usar mi ubicación actual")
                    }
                    Button(
                        onClick = { selected?.let(onConfirm) },
                        enabled = selected != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Confirmar ubicación")
                    }
                }
            }
        }
    }
}
