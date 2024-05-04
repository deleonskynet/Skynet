package com.deleon.skynet.card

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.concurrent.TimeUnit

@Database(entities = [CardModel::class], version = 1, exportSchema = true)
abstract class CardDatabase : RoomDatabase() {
    abstract fun cardAccess(): CardAccess
    companion object {
        private var INSTANCE: CardDatabase? = null
        fun getDatabase(context: Context): CardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    CardDatabase::class.java,
                    "cards_database.db"
                )
                    .setAutoCloseTimeout(5, TimeUnit.MINUTES)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}