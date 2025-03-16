package com.connor.episode.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.connor.episode.data.local.database.dao.MessageDao
import com.connor.episode.domain.model.entity.MessageEntity


@Database(
    entities = [MessageEntity::class],
    version = 2,
    autoMigrations = [],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}

