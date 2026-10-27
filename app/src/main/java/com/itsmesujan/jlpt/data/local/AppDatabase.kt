package com.itsmesujan.jlpt.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PlayerProfileEntity::class, NodeProgressEntity::class, ReviewCardEntity::class, MistakeEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun nodeProgressDao(): NodeProgressDao
    abstract fun reviewCardDao(): ReviewCardDao
    abstract fun mistakeDao(): MistakeDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "jlptquest.db")
                .fallbackToDestructiveMigration(dropAllTables = true) // v0.x only; ship real migrations before production
                .build().also { instance = it }
        }
    }
}

/** Whole days since the Unix epoch (UTC) — the app's scheduling clock for streaks and FSRS due dates. */
fun todayEpochDay(): Long = System.currentTimeMillis() / 86_400_000L
