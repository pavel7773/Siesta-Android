package com.example.catering.data.local.menu

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_cache_info")
data class MenuCacheInfoEntity(
    @PrimaryKey
    val id: String,

    val lastUpdatedAtMillis: Long
)
