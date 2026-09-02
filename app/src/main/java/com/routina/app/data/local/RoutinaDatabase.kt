package com.routina.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RoutineEntity::class, RoutineCompletionEntity::class, ProfileEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class RoutinaDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    abstract fun routineCompletionDao(): RoutineCompletionDao
    abstract fun profileDao(): ProfileDao
}
