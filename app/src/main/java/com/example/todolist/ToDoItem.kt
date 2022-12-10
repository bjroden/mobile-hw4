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
    @ColumnInfo(name = "uid") val uid: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content" ) val content: String,
    @ColumnInfo(name = "due_date") val dueDate: Long,
    @ColumnInfo(name = "completed") val completed: Boolean,
    @ColumnInfo(name = "last_modified") val lastModified: Long
) : Parcelable
{
    // Convert into format used to store inside firebase
    fun toFirebaseMap(): Map<String, Any> {
        val map = HashMap<String, Any>()
        map["title"] = this.title
        map["content"] = this.content
        map["dueDate"] = this.dueDate
        map["completed"] = this.completed
        map["lastModified"] = this.lastModified
        return map
    }

    companion object {
        // Convert firebase fields to a new item
        fun fromFirebaseMap(map: Map<String, Any>, id: Long, uid: String): ToDoItem? {
            val mTitle = map["title"] as String? ?: return null
            val mContent = map["content"] as String? ?: return null
            val mDueDate = map["dueDate"] as Long? ?: return null
            val mCompleted = map["completed"] as Boolean? ?: return null
            val mLastModified = map["lastModified"] as Long? ?: return null
            return ToDoItem(id, uid, mTitle, mContent, mDueDate, mCompleted, mLastModified)
        }
    }
}
