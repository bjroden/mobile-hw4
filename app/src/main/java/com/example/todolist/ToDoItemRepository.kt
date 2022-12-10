package com.example.todolist

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class ToDoItemRepository(private val todoItemDao: ToDoItemDao) {

    fun todoItemFlow(uid: String): Flow<List<ToDoItem>> = todoItemDao.getToDoItems(uid)

    @WorkerThread
    suspend fun getItemById(id: Long) = todoItemDao.getItemById(id)

    @WorkerThread
    suspend fun getToDoItemsOnce(uid: String) = todoItemDao.getToDoItemsOnce(uid)

    @WorkerThread
    suspend fun insert(toDoItem: ToDoItem): Long = todoItemDao.insert(toDoItem)

    @WorkerThread
    suspend fun insertNewTimestamp(toDoItem: ToDoItem): Long = todoItemDao.insert(toDoItem.copy(lastModified = System.currentTimeMillis()))

    @WorkerThread
    suspend fun update(toDoItem: ToDoItem) = todoItemDao.updateItem(toDoItem)

    @WorkerThread
    suspend fun updateNewTimestamp(toDoItem: ToDoItem) = todoItemDao.updateItem(toDoItem.copy(lastModified = System.currentTimeMillis()))

    @WorkerThread
    suspend fun deleteItem(item: ToDoItem) = todoItemDao.deleteItem(item)
}