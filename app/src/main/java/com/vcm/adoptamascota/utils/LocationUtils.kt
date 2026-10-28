package com.vcm.adoptamascota.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import java.util.Locale

/** Coordenada simple (latitud, longitud) para no depender de clases de Google fuera de los mapas. */
data class Coordinates(val lat: Double, val lng: Double) {
    /** Texto corto para mostrar en pantalla, ej: "-33.44890, -70.66930". */
    fun format(): String = String.format(Locale.US, "%.5f, %.5f", lat, lng)
}

/** Une latitud y longitud opcionales: devuelve null si falta alguna. */
fun coordinatesOf(lat: Double?, lng: Double?): Coordinates? =
    if (lat != null && lng != null) Coordinates(lat, lng) else null

/** Centro de Santiago, usado como posición inicial del selector si no hay otra ubicación. */
val DEFAULT_COORDINATES = Coordinates(-33.4489, -70.6693)

fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

/**
 * Obtiene la ubicación actual del dispositivo, o null si no hay permiso, el GPS está apagado
 * o no se pudo determinar. Usa el proveedor fusionado de Google Play Services.
 */
@SuppressLint("MissingPermission")
suspend fun fetchCurrentLocation(context: Context): Coordinates? {
    if (!hasLocationPermission(context)) return null
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    val priority = if (fine) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY
    val client = LocationServices.getFusedLocationProviderClient(context)
    return try {
        val location = client.getCurrentLocation(priority, CancellationTokenSource().token).await()
            ?: client.lastLocation.await()
        location?.let { Coordinates(it.latitude, it.longitude) }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
}

/** Abre una coordenada en la app de mapas del teléfono (o en el navegador si no hay ninguna). */
fun openInMaps(context: Context, coordinates: Coordinates, label: String) {
    val lat = String.format(Locale.US, "%.6f", coordinates.lat)
    val lng = String.format(Locale.US, "%.6f", coordinates.lng)
    val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})"))
    try {
        context.startActivity(geo)
    } catch (e: ActivityNotFoundException) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng"))
        )
    }
}
