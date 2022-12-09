package com.example.todolist

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class ToDoListViewModel(private val repository: ToDoItemRepository): ViewModel() {

    val auth = Firebase.auth
    var uid = auth.currentUser?.uid

    private val firebaseDb = Firebase.firestore.collection("users")

    private fun userItems() = firebaseDb.document(uid.toString()).collection("items")

    init {
        Log.d("MainActivity", "$uid")
        uid?.let { user ->
            // TODO: see if this is the correct way to initialize user collections
            firebaseDb.document(user).get().addOnCompleteListener { result ->
                if (result.result.data == null) {
                    firebaseDb.document(user).set(hashMapOf(
                        "items" to emptyList<ToDoItem>()
                    ))
                }
            }
        }
    }

    // Livedata used by activities to see item status
    val allToDoItems: LiveData<List<ToDoItem>> = repository.allToDoItems.asLiveData()

    fun insert(todoItem: ToDoItem) = viewModelScope.launch {
        val id = repository.insert(todoItem)
        userItems().document(id.toString()).set(todoItem.toMap())
    }

    fun updateItem(todoItem: ToDoItem) = viewModelScope.launch {
        repository.updateItem(todoItem)
        todoItem.id?.let { userItems().document(it.toString()).set(todoItem.toMap()) }
    }

    fun deleteItem(todoItem: ToDoItem) = viewModelScope.launch {
        repository.deleteItem(todoItem)
        todoItem.id?.let { userItems().document(it.toString()).delete() }
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