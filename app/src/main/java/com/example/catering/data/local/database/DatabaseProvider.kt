package com.example.catering.data.local.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var database: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        if (database != null) {
            return database!!
        }
        database = Room.databaseBuilder( // если базы нет, то создать ее
            context.applicationContext, AppDatabase::class.java,
            "app_database"
        )
            .addMigrations(MIGRATION_5_6, MIGRATION_6_7,MIGRATION_7_8,MIGRATION_8_9)
            .build()
        return database!!
    }
}