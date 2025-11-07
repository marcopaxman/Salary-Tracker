package com.example.waiterwallet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [DailyEntry::class, Job::class, MonthlyGoal::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(LocalDateTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyEntryDao(): DailyEntryDao
    abstract fun jobDao(): JobDao
    abstract fun goalDao(): MonthlyGoalDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

        private fun buildDatabase(context: Context): AppDatabase = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "waiter_wallet.db"
        ).fallbackToDestructiveMigration().build()
    }
}
