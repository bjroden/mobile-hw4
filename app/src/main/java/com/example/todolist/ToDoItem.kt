package com.example.todolist

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "todoitems_table")
@Parcelize
data class ToDoItem (
    @PrimaryKey(autoGenerate = true) val id: Long?,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content" ) val content: String,
    @ColumnInfo(name = "due_date") val dueDate: Long,
    @ColumnInfo(name = "completed") val completed: Boolean
) : Parcelable
{
    fun fromMap(map: Map<String, Any>): ToDoItem? {
        val mTitle = map["title"] as String?
        val mContent = map["content"] as String?
        val mDueDate = map["dueDate"] as Long?
        val mCompleted = map["completed"] as Boolean?
        return if (mTitle != null && mContent != null && mDueDate != null && mCompleted != null) {
            ToDoItem(null, mTitle, mContent, mDueDate, mCompleted)
        } else null

    }

    fun toMap(): Map<String, Any> {
        val map = HashMap<String, Any>()
        map["title"] = this.title
        map["content"] = this.content
        map["dueDate"] = this.dueDate
        map["completed"] = this.completed
        return map
    }
}
