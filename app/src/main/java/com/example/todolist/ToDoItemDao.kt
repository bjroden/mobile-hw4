package com.example.todolist

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoItemDao {
    @Query("SELECT * FROM todoitems_table order by id ASC")
    fun getToDoItems(): Flow<List<ToDoItem>>

    @Update
    suspend fun updateItem(todoItem: ToDoItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(todoItem: ToDoItem)

    @Delete
    suspend fun deleteItem(item: ToDoItem)

    @Query("DELETE FROM todoitems_table")
    suspend fun deleteAll()
}