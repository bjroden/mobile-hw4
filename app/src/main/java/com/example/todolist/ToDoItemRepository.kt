package com.example.todolist

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class ToDoItemRepository(private val todoItemDao: ToDoItemDao) {

    fun todoItemFlow(uid: String): Flow<List<ToDoItem>> = todoItemDao.getToDoItems(uid)

    @WorkerThread
    suspend fun getToDoItemsOnce(uid: String) = todoItemDao.getToDoItemsOnce(uid)

    @WorkerThread
    suspend fun insert(toDoItem: ToDoItem): Long = todoItemDao.insert(toDoItem)

    @WorkerThread
    suspend fun updateItem(toDoItem: ToDoItem) = todoItemDao.updateItem(toDoItem)

    @WorkerThread
    suspend fun deleteItem(item: ToDoItem) = todoItemDao.deleteItem(item)
}