package de.kyrohpaneup.abirechner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.kyrohpaneup.abirechner.data.grades.Grade
import de.kyrohpaneup.abirechner.data.grades.GradeDao
import de.kyrohpaneup.abirechner.data.grades.HeadGrade

@Database(entities = [Grade::class, HeadGrade::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gradeDao(): GradeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "abirechner_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}