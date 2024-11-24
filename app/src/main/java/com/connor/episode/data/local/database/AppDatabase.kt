package com.connor.episode.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.connor.episode.data.local.database.dao.MessageDao
import com.connor.episode.data.local.database.entity.MessageEntity

@Database(entities = [MessageEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}