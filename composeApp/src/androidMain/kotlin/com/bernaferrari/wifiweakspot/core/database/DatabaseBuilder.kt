package com.bernaferrari.wifiweakspot.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<WifiWeakspotDatabase> {
    val dbFile = context.getDatabasePath("wifiweakspot.db")
    return Room.databaseBuilder<WifiWeakspotDatabase>(
        context = context,
        name = dbFile.absolutePath
    )
}
