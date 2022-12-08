package com.example.todolist

import androidx.lifecycle.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class ToDoListViewModel(private val repository: ToDoItemRepository): ViewModel() {

    private val firebaseDb = Firebase.firestore.collection("items-test")

    // Livedata used by activities to see item status
    val allToDoItems: LiveData<List<ToDoItem>> = repository.allToDoItems.asLiveData()

    fun insert(todoItem: ToDoItem) = viewModelScope.launch {
        val id = repository.insert(todoItem)
        firebaseDb.document(id.toString()).set(todoItem.toMap())
    }

    fun updateItem(todoItem: ToDoItem) = viewModelScope.launch {
        repository.updateItem(todoItem)
        todoItem.id?.let { firebaseDb.document(it.toString()).set(todoItem.toMap()) }
    }

    fun deleteItem(todoItem: ToDoItem) = viewModelScope.launch {
        repository.deleteItem(todoItem)
        todoItem.id?.let { firebaseDb.document(it.toString()).delete() }
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