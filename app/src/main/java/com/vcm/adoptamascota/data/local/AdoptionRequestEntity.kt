package com.vcm.adoptamascota.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Estados de una solicitud de adopción. */
object RequestStatus {
    const val PENDING = "PENDIENTE"
    const val APPROVED = "APROBADA"
    const val REJECTED = "RECHAZADA"
}

/** Solicitud de adopción de una mascota (tercera tabla de Room). */
@Entity(tableName = "adoption_requests")
data class AdoptionRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val petId: Int,
    val applicantName: String,
    val applicantPhone: String,
    val applicantEmail: String,
    val message: String,
    val permitNumber: String,   // permiso/documento del adoptante; vacío si la mascota no es exótica
    val status: String = RequestStatus.PENDING,
    val dateRequested: String
)
