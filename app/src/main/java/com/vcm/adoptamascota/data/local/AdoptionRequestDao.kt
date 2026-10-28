package com.vcm.adoptamascota.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AdoptionRequestDao {
    @Query("SELECT * FROM adoption_requests WHERE petId = :petId ORDER BY id DESC")
    fun observeByPet(petId: Int): Flow<List<AdoptionRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: AdoptionRequestEntity)

    @Query("UPDATE adoption_requests SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Query("DELETE FROM adoption_requests WHERE petId = :petId")
    suspend fun deleteByPet(petId: Int)
}
