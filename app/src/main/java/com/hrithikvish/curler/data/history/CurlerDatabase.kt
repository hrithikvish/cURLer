package com.hrithikvish.curler.data.history

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [HistoryEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CurlerDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}
