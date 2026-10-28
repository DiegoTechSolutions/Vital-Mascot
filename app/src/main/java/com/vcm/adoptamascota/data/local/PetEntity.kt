package com.vcm.adoptamascota.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Categorías de mascota: de compañía (perros, gatos...) o exótica (reptiles, aves...). */
object PetCategory {
    const val COMPANION = "COMPANION"
    const val EXOTIC = "EXOTIC"
}

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val species: String,         // Perro/Gato/Otro; en exóticas, el tipo (Reptil, Ave...)
    val age: String,
    val description: String,
    val contactPhone: String,
    val imageUri: String?,
    val dateRegistered: String,
    val category: String = PetCategory.COMPANION,
    val exoticSpecies: String = "",   // nombre de la especie exótica (ej: Iguana verde)
    val permitNumber: String = "",    // N° de resolución SAG / permiso CITES / documento de origen legal
    val foundPlace: String = "",      // descripción del lugar donde fue encontrada
    val foundLat: Double? = null,
    val foundLng: Double? = null,
    val lostPlace: String = "",       // lugar donde se extravió (si se sabe)
    val lostLat: Double? = null,
    val lostLng: Double? = null
) {
    val isExotic: Boolean get() = category == PetCategory.EXOTIC
    val hasFoundLocation: Boolean get() = foundLat != null && foundLng != null
    val hasLostLocation: Boolean get() = lostLat != null && lostLng != null
}
