package com.ghostid.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [AliasEntity::class, AccountEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class GhostIDDatabase : RoomDatabase() {
    abstract fun aliasDao(): AliasDao
}
