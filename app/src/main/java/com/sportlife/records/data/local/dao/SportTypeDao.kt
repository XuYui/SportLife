package com.sportlife.records.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sportlife.records.data.local.entity.SportTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SportTypeDao {
    @Query("SELECT * FROM sport_types ORDER BY displayName")
    fun observeSportTypes(): Flow<List<SportTypeEntity>>

    @Query("SELECT COUNT(*) FROM sport_types")
    suspend fun count(): Int

    @Upsert
    suspend fun upsertAll(types: List<SportTypeEntity>)
}
