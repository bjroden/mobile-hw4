package com.example.todolist

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoItemDao {
    @Query("SELECT * FROM todoitems_table where uid = :uid order by id ASC")
    fun getToDoItems(uid: String): Flow<List<ToDoItem>>

    @Query("SELECT * FROM todoitems_table where uid = :uid order by id ASC")
    suspend fun getToDoItemsOnce(uid: String): List<ToDoItem>

    @Update
    suspend fun updateItem(todoItem: ToDoItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(todoItem: ToDoItem): Long

    @Delete
    suspend fun deleteItem(item: ToDoItem)

    @Query("DELETE FROM todoitems_table")
    suspend fun deleteAll()
}