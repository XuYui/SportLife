package com.sportlife.records.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sportlife.records.domain.model.SportType

@Entity(tableName = "sport_types")
data class SportTypeEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val description: String,
    val capabilitiesCsv: String,
    val isBuiltIn: Boolean,
    val createdAtMillis: Long = System.currentTimeMillis(),
)

fun SportType.toEntity(): SportTypeEntity =
    SportTypeEntity(
        id = id,
        displayName = displayName,
        description = description,
        capabilitiesCsv = capabilities.joinToString(",") { it.name },
        isBuiltIn = true,
    )
