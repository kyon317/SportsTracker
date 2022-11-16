package com.example.jiaqing_hu.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
/* ExerciseEntryRepository - Repository for view model to access and store database*/
class ExerciseEntryRepository(private val exerciseEntryDatabaseDao : ExerciseEntryDatabaseDao) {
    val allComments: Flow<List<ExerciseEntry>> = exerciseEntryDatabaseDao.getAllEntries()

    fun insert(exerciseEntry: ExerciseEntry){
        CoroutineScope(Dispatchers.IO).launch{
            exerciseEntryDatabaseDao.insertEntry(exerciseEntry)
        }
    }

    fun delete(id: Long){
        CoroutineScope(Dispatchers.IO).launch {
            exerciseEntryDatabaseDao.deleteComment(id)
        }
    }

    fun deleteAll(){
        CoroutineScope(Dispatchers.IO).launch {
            exerciseEntryDatabaseDao.deleteAll()
        }
    }
}