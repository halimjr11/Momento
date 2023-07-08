package com.nurhaqhalim.momento.core.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nurhaqhalim.momento.core.local.dao.MoDao
import com.nurhaqhalim.momento.core.local.model.RemoteKeys
import com.nurhaqhalim.momento.core.local.model.StoryEntity
import com.nurhaqhalim.momento.utils.GlobalConstants

@Database(entities = [StoryEntity::class, RemoteKeys::class], version = 1)
abstract class MoDatabase : RoomDatabase() {
    abstract fun moDao() : MoDao
    companion object {
        @Volatile
        private var INSTANCE : MoDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context) : MoDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MoDatabase::class.java, GlobalConstants.dbName
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}