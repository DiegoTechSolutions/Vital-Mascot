package com.vcm.adoptamascota.utils

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * El Uri que entrega la galería (GetContent) es temporal: puede perder el permiso
 * al reiniciar la app. Por eso copiamos la foto al almacenamiento interno de la app
 * y guardamos en Room la ruta de ESA copia, que siempre será accesible.
 */
object ImageStorage {

    /** Copia la imagen elegida a filesDir/pet_images y devuelve el Uri (String) de la copia, o null si falla. */
    fun copyToInternal(context: Context, source: Uri): String? = try {
        val dir = File(context.filesDir, "pet_images").apply { mkdirs() }
        val target = File(dir, "pet_${System.currentTimeMillis()}.jpg")
        val input = context.contentResolver.openInputStream(source)
        if (input == null) {
            null
        } else {
            input.use { src -> target.outputStream().use { dst -> src.copyTo(dst) } }
            Uri.fromFile(target).toString()
        }
    } catch (e: Exception) {
        null
    }

    /** Borra la copia local cuando se elimina la mascota. */
    fun delete(imageUri: String?) {
        if (imageUri == null) return
        runCatching { Uri.parse(imageUri).path?.let { File(it).delete() } }
    }
}
