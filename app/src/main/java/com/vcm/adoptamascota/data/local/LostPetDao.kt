package com.vcm.adoptamascota.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LostPetDao {
    // Primero los que siguen perdidos (isFound = 0), luego los encontrados; más recientes arriba
    @Query("SELECT * FROM lost_pets ORDER BY isFound ASC, id DESC")
    fun observeAll(): Flow<List<LostPetEntity>>

    @Insert
    suspend fun insert(pet: LostPetEntity)

    @Update
    suspend fun update(pet: LostPetEntity)

    @Delete
    suspend fun delete(pet: LostPetEntity)
}
