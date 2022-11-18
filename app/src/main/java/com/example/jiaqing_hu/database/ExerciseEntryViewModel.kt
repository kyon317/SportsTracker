package com.example.jiaqing_hu.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData


/* ExerciseEntryViewModel - A view model that operates on ExerciseEntryRepository*/
class ExerciseEntryViewModel(private val repository: ExerciseEntryRepository) : ViewModel() {

    class CommentViewModel(private val repository: ExerciseEntryRepository) : ViewModel() {
        val allCommentsLiveData: LiveData<List<ExerciseEntry>> = repository.allComments.asLiveData()

        fun insert(comment: ExerciseEntry) {
            repository.insert(comment)
        }

        fun deleteFirst(){
            val entryList = allCommentsLiveData.value
            if (entryList != null && entryList.size > 0){
                val id = entryList[0].id
                repository.delete(id)
            }
        }

        fun deleteAll(){
            val commentList = allCommentsLiveData.value
            if (commentList != null && commentList.size > 0)
                repository.deleteAll()
        }

        fun getById(index:Int):ExerciseEntry? {
            val commentList = allCommentsLiveData.value

            if (commentList != null && commentList.isNotEmpty())
                return allCommentsLiveData.value?.get(index)
            return null
        }

    }

}

class ExerciseViewModelFactory (private val repository: ExerciseEntryRepository) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{ //create() creates a new instance of the modelClass, which is CommentViewModel in this case.
        if(modelClass.isAssignableFrom(ExerciseEntryViewModel.CommentViewModel::class.java))
            return ExerciseEntryViewModel.CommentViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}