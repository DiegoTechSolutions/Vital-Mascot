package com.vcm.adoptamascota.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RECURSO NATIVO #2 (Fecha/Hora del sistema).
 * Usa java.util.Date para leer el reloj del dispositivo y SimpleDateFormat
 * para darle formato legible. Se invoca al guardar una mascota.
 */
object DateUtils {
    private val locale = Locale("es", "CL")

    /** Fecha y hora actual del sistema, ej: "27/10/2026 14:35". */
    fun currentDateTime(): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", locale).format(Date())

    /** Solo la fecha actual, ej: "27/10/2026" (para mostrar como vista previa en el formulario). */
    fun currentDate(): String =
        SimpleDateFormat("dd/MM/yyyy", locale).format(Date())
}
