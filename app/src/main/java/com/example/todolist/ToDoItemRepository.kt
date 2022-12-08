package com.example.todolist

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class ToDoItemRepository(private val todoItemDao: ToDoItemDao) {

    val allToDoItems: Flow<List<ToDoItem>> = todoItemDao.getToDoItems()

    @WorkerThread
    suspend fun insert(toDoItem: ToDoItem): Long = todoItemDao.insert(toDoItem)

    @WorkerThread
    suspend fun updateItem(toDoItem: ToDoItem) = todoItemDao.updateItem(toDoItem)

    @WorkerThread
    suspend fun deleteItem(item: ToDoItem) = todoItemDao.deleteItem(item)
}