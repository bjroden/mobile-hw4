package com.example.todolist

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = arrayOf(ToDoItem::class), version = 1, exportSchema = false)
public abstract class ToDoItemRoomDatabase: RoomDatabase() {
    abstract fun toDoItemDao(): ToDoItemDao

    companion object {
        // Singleton prevents multiple instances of database opening at the same time
        @Volatile
        private var INSTANCE: ToDoItemRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ToDoItemRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?:  synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ToDoItemRoomDatabase::class.java,
                    "todoitem_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}