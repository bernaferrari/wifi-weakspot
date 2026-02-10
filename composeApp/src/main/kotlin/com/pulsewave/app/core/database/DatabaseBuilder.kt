package com.pulsewave.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<PulseWaveDatabase> {
    val dbFile = context.getDatabasePath("pulsewave.db")
    return Room.databaseBuilder<PulseWaveDatabase>(
        context = context,
        name = dbFile.absolutePath
    ).fallbackToDestructiveMigration()
}
