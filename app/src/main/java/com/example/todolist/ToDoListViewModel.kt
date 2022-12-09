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

    init {
        Log.d("MainActivity", "$uid")
        uid?.let { user ->
            checkFirebaseUserExists(user)
            checkLocalNotInFirebase(user)
            checkFirebaseNotInLocal(user)
        }
    }

    private fun userItems() = firebaseDb.document(uid.toString()).collection("items")

    private fun checkFirebaseUserExists(user: String) =
        // TODO: see if this is the correct way to initialize user collections
        firebaseDb.document(user).get().addOnCompleteListener { result ->
            if (result.result.data == null) {
                firebaseDb.document(user).set(hashMapOf(
                    "items" to emptyList<ToDoItem>()
                ))
            }
        }

    private fun checkLocalNotInFirebase(user: String) = userItems().get().addOnSuccessListener { query ->
        viewModelScope.launch {
            val online = query.documents.mapNotNull {
                val data = it.data
                val id = it.id.toLongOrNull()
                if (data != null && id != null)
                    ToDoItem.fromFirebaseMap(data, id, user)
                else null
            }
            val local = repository.getToDoItemsOnce(user)
            for (l in local) {
                // TODO: more robust checking for id conflicts
                if (l !in online) { userItems().document(l.id.toString()).set(l.toFirebaseMap()) }
            }
        }
    }

    private fun checkFirebaseNotInLocal(user: String) = userItems().get().addOnSuccessListener { query ->
        viewModelScope.launch {
            val online = query.documents.mapNotNull {
                val data = it.data
                val id = it.id.toLongOrNull()
                if (data != null && id != null)
                    ToDoItem.fromFirebaseMap(data, id, user)
                else null
            }
            val local = repository.getToDoItemsOnce(user)
            for (o in online) {
                if (o !in local) {
                    val newId = repository.insert(o.copy(id = null))
                    userItems().document(newId.toString()).delete()
                    userItems().document(newId.toString()).set(o.toFirebaseMap())
                }
            }
        }
    }

    // Livedata used by activities to see item status
    val allToDoItems: LiveData<List<ToDoItem>> = repository.todoItemFlow(uid.toString()).asLiveData()

    fun insert(todoItem: ToDoItem) = viewModelScope.launch {
        uid?.let {
            val newItem = todoItem.copy(uid = it)
            val id = repository.insert(newItem)
            userItems().document(id.toString()).set(newItem.toFirebaseMap())
        }
    }

    fun updateItem(todoItem: ToDoItem) = viewModelScope.launch {
        repository.updateItem(todoItem)
        todoItem.id?.let { userItems().document(it.toString()).set(todoItem.toFirebaseMap()) }
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