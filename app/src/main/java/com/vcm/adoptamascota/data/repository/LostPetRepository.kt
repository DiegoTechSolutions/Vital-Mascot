package com.vcm.adoptamascota.data.repository

import com.vcm.adoptamascota.data.local.LostPetDao
import com.vcm.adoptamascota.data.local.LostPetEntity
import kotlinx.coroutines.flow.Flow

class LostPetRepository(private val dao: LostPetDao) {
    fun observeLostPets(): Flow<List<LostPetEntity>> = dao.observeAll()

    suspend fun report(pet: LostPetEntity) = dao.insert(pet)

    suspend fun markFound(pet: LostPetEntity) = dao.update(pet.copy(isFound = true))

    suspend fun delete(pet: LostPetEntity) = dao.delete(pet)
}
