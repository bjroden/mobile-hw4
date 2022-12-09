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

    // Create a collection in firebase if it doesn't already exist
    private fun checkFirebaseUserExists(user: String) =
        firebaseDb.document(user).get().addOnCompleteListener { result ->
            if (result.result.data == null) {
                firebaseDb.document(user).set(hashMapOf(
                    "items" to emptyList<ToDoItem>()
                ))
            }
        }

    // Upload local items to firebase if they do not exist
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
                if (l !in online) {
                    // Reinsert into room to avoid conflicting ids between room and firebase
                    repository.deleteItem(l)
                    val newId = repository.insert(l.copy(id = null))
                    userItems().document(newId.toString()).set(l.toFirebaseMap())
                }
            }
        }
    }

    // Save firebase items to local repository if they do not exist
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
                    // Reinsert into firebase to avoid conflicting ids between room and firebase
                    val newId = repository.insert(o.copy(id = null))
                    userItems().document(o.id.toString()).delete()
                    userItems().document(newId.toString()).set(o.toFirebaseMap())
                }
            }
        }
    }

    // Livedata used by activities to see item status
    val allToDoItems: LiveData<List<ToDoItem>> = repository.todoItemFlow(uid.toString()).asLiveData()

    // Insert item into both room and firebase
    fun insert(todoItem: ToDoItem) = viewModelScope.launch {
        uid?.let {
            val newItem = todoItem.copy(uid = it)
            val id = repository.insert(newItem)
            userItems().document(id.toString()).set(newItem.toFirebaseMap())
        }
    }

    // Update item in both room and firebase
    fun updateItem(todoItem: ToDoItem) = viewModelScope.launch {
        repository.updateItem(todoItem)
        todoItem.id?.let { userItems().document(it.toString()).set(todoItem.toFirebaseMap()) }
    }

    // Delete item from both room and firebase
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