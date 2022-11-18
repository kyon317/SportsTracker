package com.example.jiaqing_hu.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/* ExerciseEntryDatabaseDao - DAO object for ExerciseEntryDatabase*/
@Dao
interface ExerciseEntryDatabaseDao {
    @Insert
    suspend fun insertEntry(ExerciseEntry: ExerciseEntry)

    //A Flow is an async sequence of values
    //Flow produces values one at a time (instead of all at once) that can generate values
    //from async operations like network requests, database calls, or other async code.
    //It supports coroutines throughout its API, so you can transform a flow using coroutines as well!
    //Code inside the flow { ... } builder block can suspend. So the function is no longer marked with suspend modifier.
    //See more details here: https://kotlinlang.org/docs/flow.html#flows
    @Query("SELECT * FROM exercise_table")
    fun getAllEntries(): Flow<List<ExerciseEntry>>

    @Query("DELETE FROM exercise_table")
    suspend fun deleteAll()

    @Query("DELETE FROM exercise_table WHERE id = :key") //":" indicates that it is a Bind variable
    suspend fun deleteComment(key: Long)

}