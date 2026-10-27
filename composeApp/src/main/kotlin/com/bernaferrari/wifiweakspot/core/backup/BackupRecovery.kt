package com.bernaferrari.wifiweakspot.core.backup

import com.bernaferrari.wifiweakspot.core.database.dao.WifiWeakspotDao
import com.bernaferrari.wifiweakspot.core.rooms.RoomCheckRepository
import com.bernaferrari.wifiweakspot.core.rooms.RoomReading

/** Retry-safe handoff: a crash between these writes neither loses nor duplicates room readings. */
internal suspend fun replayPendingBackupImports(dao: WifiWeakspotDao, rooms: RoomCheckRepository) {
    dao.getPendingBackupImports().forEach { pending ->
        rooms.importMissing(backupJson.decodeFromString<List<RoomReading>>(pending.roomsJson))
        dao.completeBackupImport(pending.archiveId)
    }
}
