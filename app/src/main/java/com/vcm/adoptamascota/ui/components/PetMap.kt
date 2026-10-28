package com.vcm.adoptamascota.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.vcm.adoptamascota.BuildConfig
import com.vcm.adoptamascota.utils.Coordinates
import com.vcm.adoptamascota.utils.openInMaps

/** El mapa dentro de la app solo está disponible si se configuró MAPS_API_KEY en local.properties. */
val mapsAvailable: Boolean get() = BuildConfig.MAPS_API_KEY.isNotBlank()

private fun Coordinates.toLatLng() = LatLng(lat, lng)

/**
 * Vista previa de las ubicaciones de una mascota: un mapa con un marcador donde fue encontrada
 * y otro donde se extravió. Sin clave de Google Maps muestra las coordenadas y abre la app de mapas.
 */
@Composable
fun PetLocationMap(
    found: Coordinates?,
    lost: Coordinates?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val first = found ?: lost
        if (mapsAvailable && first != null) {
            val cameraState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(first.toLatLng(), 15f)
            }
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp)),
                cameraPositionState = cameraState,
                googleMapOptionsFactory = { GoogleMapOptions().liteMode(true) },
                onMapLoaded = {
                    // Con dos puntos, el encuadre muestra ambos
                    if (found != null && lost != null) {
                        val bounds = LatLngBounds.builder()
                            .include(found.toLatLng()).include(lost.toLatLng()).build()
                        cameraState.move(CameraUpdateFactory.newLatLngBounds(bounds, 96))
                    }
                }
            ) {
                found?.let {
                    Marker(state = MarkerState(it.toLatLng()), title = "Encontrada aquí")
                }
                lost?.let {
                    Marker(
                        state = MarkerState(it.toLatLng()),
                        title = "Se extravió aquí",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }
            }
        } else if (first != null) {
            // Alternativa sin clave de Google Maps: coordenadas + botón para abrir la app de mapas
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Map, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Mapa no disponible dentro de la app. Abre la ubicación en tu app de mapas.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Accesos a la app de mapas externa, siempre disponibles
        found?.let {
            LocationLine("Encontrada", it) { openInMaps(context, it, "Encontrada") }
        }
        lost?.let {
            LocationLine("Se extravió", it) { openInMaps(context, it, "Se extravió") }
        }
    }
}

@Composable
private fun LocationLine(label: String, coordinates: Coordinates, onOpen: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Place, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(6.dp))
        Text(
            "$label: ${coordinates.format()}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onOpen) { Text("Abrir en Maps") }
    }
}
