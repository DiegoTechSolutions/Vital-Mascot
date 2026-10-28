package com.vcm.adoptamascota.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Reporte de un animal perdido (segunda tabla de Room). */
@Entity(tableName = "lost_pets")
data class LostPetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val species: String,
    val lastSeen: String,        // lugar donde fue visto por última vez
    val description: String,
    val contactPhone: String,
    val imageUri: String?,
    val dateReported: String,    // fecha/hora del sistema al reportar
    val isFound: Boolean = false // se marca true cuando el animal aparece
)
