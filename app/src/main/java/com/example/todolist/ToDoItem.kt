package com.example.todolist

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "todoitems_table")
@Parcelize
data class ToDoItem (
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content" ) val content: String,
    @ColumnInfo(name = "due_date") val dueDate: Long,
    @ColumnInfo(name = "completed") val completed: Boolean
) : Parcelable
