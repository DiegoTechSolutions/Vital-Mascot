package com.vcm.adoptamascota.data.repository

import com.vcm.adoptamascota.data.local.AdoptionRequestDao
import com.vcm.adoptamascota.data.local.AdoptionRequestEntity
import kotlinx.coroutines.flow.Flow

class AdoptionRepository(private val dao: AdoptionRequestDao) {
    fun observeRequests(petId: Int): Flow<List<AdoptionRequestEntity>> = dao.observeByPet(petId)

    suspend fun saveRequest(request: AdoptionRequestEntity) = dao.insert(request)

    suspend fun updateStatus(id: Int, status: String) = dao.updateStatus(id, status)
}
