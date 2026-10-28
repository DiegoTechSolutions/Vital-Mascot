package com.vcm.adoptamascota.data.repository

import com.vcm.adoptamascota.data.local.AdoptionRequestDao
import com.vcm.adoptamascota.data.local.PetDao
import com.vcm.adoptamascota.data.local.PetEntity
import kotlinx.coroutines.flow.Flow

class PetRepository(
    private val petDao: PetDao,
    private val requestDao: AdoptionRequestDao
) {
    fun observePets(): Flow<List<PetEntity>> = petDao.observeAll()

    fun observePet(id: Int): Flow<PetEntity?> = petDao.observeById(id)

    suspend fun savePet(pet: PetEntity) = petDao.insert(pet)

    /** Al eliminar una mascota también se eliminan sus solicitudes de adopción. */
    suspend fun deletePet(pet: PetEntity) {
        requestDao.deleteByPet(pet.id)
        petDao.delete(pet)
    }
}
