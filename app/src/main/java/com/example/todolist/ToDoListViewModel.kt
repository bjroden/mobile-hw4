package com.example.todolist

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class ToDoListViewModel(private val repository: ToDoItemRepository): ViewModel() {

    // Livedata used by activities to see item status
    val allToDoItems: LiveData<List<ToDoItem>> = repository.allToDoItems.asLiveData()

    fun insert(todoItem: ToDoItem) = viewModelScope.launch {
        repository.insert(todoItem)
    }

    fun updateItem(todoItem: ToDoItem) = viewModelScope.launch {
        repository.updateItem(todoItem)
    }

    fun deleteItem(item: ToDoItem) = viewModelScope.launch {
        repository.deleteItem(item)
    }

    class ToDoListViewModelFactory(private val repository: ToDoItemRepository) : ViewModelProvider.Factory{
        override fun <T: ViewModel> create(modelClass: Class<T>): T{
            if(modelClass.isAssignableFrom(ToDoListViewModel::class.java)){
                @Suppress("UNCHECKED_CAST")
                return ToDoListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel Class")
        }
    }
}