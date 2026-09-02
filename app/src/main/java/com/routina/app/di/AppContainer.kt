package com.routina.app.di

import android.content.Context
import androidx.room.Room
import com.routina.app.data.RoutineRepositoryImpl
import com.routina.app.data.local.RoutinaDatabase
import com.routina.app.domain.repository.RoutineRepository

class AppContainer(context: Context) {
    val database: RoutinaDatabase = Room.databaseBuilder(
        context.applicationContext,
        RoutinaDatabase::class.java,
        DATABASE_NAME,
    ).build()

    val routineRepository: RoutineRepository = RoutineRepositoryImpl(database)

    private companion object {
        const val DATABASE_NAME = "routina.db"
    }
}
