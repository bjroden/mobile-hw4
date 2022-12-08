package com.example.todolist

import androidx.recyclerview.widget.DiffUtil

// Class for comparing two lits of ToDoItems. Used by AlarmHandler so that it only updates the
// alarms when items are changed
class ToDoItemDiffCallback (
    private val old: List<ToDoItem>,
    private val new: List<ToDoItem>
    ) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition].id == new[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] == new[newItemPosition]
    }

}