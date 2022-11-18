package com.example.jiaqing_hu.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/* ExerciseEntryDatabase - An abstract database for entries */
@Database(entities = [ExerciseEntry::class], version = 1)
@TypeConverters(Converters::class)
abstract class ExerciseEntryDatabase: RoomDatabase() {
    abstract val exerciseEntryDatabaseDao:ExerciseEntryDatabaseDao

    companion object{
        @Volatile
        private var INSTANCE:ExerciseEntryDatabase?=null

        fun getInstance(context: Context):ExerciseEntryDatabase{
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(context.applicationContext,
                        ExerciseEntryDatabase::class.java, "exercise_table").build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}