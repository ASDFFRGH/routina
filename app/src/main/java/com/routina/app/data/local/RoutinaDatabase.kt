package com.routina.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [RoutineEntity::class, RoutineCompletionEntity::class, ProfileEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class RoutinaDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    abstract fun routineCompletionDao(): RoutineCompletionDao
    abstract fun profileDao(): ProfileDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE routines ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
        // Existing lists used creation time, so retain that order after the migration.
        database.execSQL("UPDATE routines SET sortOrder = createdAtEpochMillis")
    }
}
