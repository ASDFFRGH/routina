package com.routina.app.data.local

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class RoutinaDatabaseMigrationTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "migration-test.db"

    @After fun tearDown() {
        context.deleteDatabase(name)
    }

    @Test fun migrationFromVersionOnePreservesRoutinesAndInitializesCreationOrder() {
        val file = File(context.getDatabasePath(name).path)
        file.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            db.execSQL("CREATE TABLE routines (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, startEpochDay INTEGER NOT NULL, intervalDays INTEGER NOT NULL, rewardXp INTEGER NOT NULL, rewardPoints INTEGER NOT NULL, createdAtEpochMillis INTEGER NOT NULL, archivedEpochDay INTEGER)")
            db.execSQL("CREATE TABLE routine_completions (routineId TEXT NOT NULL, scheduledEpochDay INTEGER NOT NULL, completedAtEpochMillis INTEGER NOT NULL, awardedXp INTEGER NOT NULL, awardedPoints INTEGER NOT NULL, PRIMARY KEY(routineId, scheduledEpochDay))")
            db.execSQL("CREATE TABLE profile (id INTEGER NOT NULL PRIMARY KEY, totalXp INTEGER NOT NULL, totalPoints INTEGER NOT NULL)")
            db.execSQL("INSERT INTO routines VALUES ('late', '後', 1, 1, 20, 10, 20, NULL)")
            db.execSQL("INSERT INTO routines VALUES ('early', '先', 1, 1, 20, 10, 10, NULL)")
            db.execSQL("INSERT INTO routine_completions VALUES ('early', 1, 100, 20, 10)")
            db.execSQL("INSERT INTO profile VALUES (1, 40, 20)")
            db.version = 1
        }

        val database = Room.databaseBuilder(context, RoutinaDatabase::class.java, name)
            .addMigrations(MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()
        try {
            val ordered = runBlocking { database.routineDao().observeAll().first() }
            assertEquals(listOf("early", "late"), ordered.map { it.id })
            assertEquals(listOf(10L, 20L), ordered.map { it.sortOrder })
            val completions = runBlocking { database.routineCompletionDao().observeBetween(1, 1).first() }
            assertEquals("early", completions.single().routineId)
            assertEquals(40, runBlocking { database.profileDao().observe().first() }?.totalXp)
        } finally {
            database.close()
        }
    }
}
