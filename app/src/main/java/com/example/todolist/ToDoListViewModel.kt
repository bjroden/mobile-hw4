package com.example.todolist

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
        uid?.let { user ->
            // Setup firebase listeners
            checkFirebaseUserExists(user)
            userItems().addSnapshotListener { snapshot, _ ->
                viewModelScope.launch {
                    val online = snapshot?.documents?.mapNotNull {
                        val data = it.data
                        val id = it.id.toLongOrNull()
                        if (data != null && id != null)
                            ToDoItem.fromFirebaseMap(data, id, user)
                        else null
                    } ?: return@launch
                    val local = repository.getToDoItemsOnce(user)
                    checkLocalNotInFirebase(user, online, local)
                    checkFirebaseNotInLocal(user, online, local)
                }
            }
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
    private fun checkLocalNotInFirebase(user: String, onlineItems: List<ToDoItem>, localItems: List<ToDoItem>)  {
        for (l in localItems) {
            onlineItems.find { it.id == l.id && it.uid == user }?.let { o ->
                // If item exists in firebase but is different, update
                if (o != l) { writeNewerTimestamp(o, l) }
                // If item does not exist in firebase, write it by reinserting room
            } ?: run { reinsertRoom(l) }
        }
    }

    // Save firebase items to local repository if they do not exist
    private fun checkFirebaseNotInLocal(user: String, onlineItems: List<ToDoItem>, localItems: List<ToDoItem>) {
        for (o in onlineItems) {
            localItems.find { it.id == o.id && it.uid == user }?.let { l ->
                // If item exists in room but is different, update
                if (o != l) { writeNewerTimestamp(o, l) }
                // If item does not exist in room, write it by reinserting firebase
            } ?: run { reinsertFirebase(o) }
        }
    }

    // Between online item (o) and local item (l), take the newer one and modify the corresponding db
    private fun writeNewerTimestamp(o: ToDoItem, l: ToDoItem) {
        if (l.lastModified > o.lastModified) { userItems().document(l.id.toString()).set(l.toFirebaseMap()) }
        else { viewModelScope.launch { repository.update(o) } }
    }

    // Reinsert into room to avoid conflicting ids between room and firebase
    private fun reinsertRoom(localItem: ToDoItem) = viewModelScope.launch {
        repository.deleteItem(localItem)
        val newId = repository.insertNewTimestamp(localItem.copy(id = null))
        val newItem = repository.getItemById(newId)
        userItems().document(newId.toString()).set(newItem.toFirebaseMap())
    }

    // Reinsert into firebase to avoid conflicting ids between room and firebase
    private fun reinsertFirebase(onlineItem: ToDoItem) = viewModelScope.launch {
        val newId = repository.insertNewTimestamp(onlineItem.copy(id = null))
        val newItem = repository.getItemById(newId)
        Firebase.firestore.runTransaction { transaction ->
            transaction.delete(userItems().document(onlineItem.id.toString()))
            transaction.set(userItems().document(newId.toString()), newItem.toFirebaseMap())
        }
    }

    // Livedata used by activities to see item status
    val allToDoItems: LiveData<List<ToDoItem>> = repository.todoItemFlow(uid.toString()).asLiveData()

    // Insert item into both room and firebase
    fun insert(todoItem: ToDoItem) = viewModelScope.launch {
        uid?.let {
            val newItem = todoItem.copy(uid = it)
            val newId = repository.insertNewTimestamp(newItem)
            val updatedTimestamp = repository.getItemById(newId)
            userItems().document(newId.toString()).set(updatedTimestamp.toFirebaseMap())
        }
    }

    // Update item in both room and firebase
    fun updateItem(todoItem: ToDoItem) = viewModelScope.launch {
        todoItem.id?.let {
            repository.updateNewTimestamp(todoItem)
            val newItem = repository.getItemById(it)
            userItems().document(it.toString()).set(newItem.toFirebaseMap())
        }
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