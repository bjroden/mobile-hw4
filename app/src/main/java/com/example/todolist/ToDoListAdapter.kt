package com.example.todolist

import android.icu.text.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.truncate

class ToDoListAdapter(
    private val handler: ToDoItemButtonListener
) : ListAdapter<ToDoItem, ToDoListAdapter.ToDoItemViewHolder>(ToDoItemComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoItemViewHolder {
        return ToDoItemViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ToDoItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, handler)
        // Clicking the holder will launch the edit activity
        holder.itemView.setOnClickListener { handler.editRequested(item) }
    }

    class ToDoItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val toDoItemTitleView: TextView = itemView.findViewById(R.id.tvTitle)
        private val toDoItemContentView: TextView = itemView.findViewById(R.id.tvContent)
        private val toDoItemDate: TextView = itemView.findViewById(R.id.tvDate)
        private val toDoItemCheckBox: CheckBox = itemView.findViewById(R.id.isCompleted)

        // Set all relevant values for the item view
        fun bind(item: ToDoItem?, handler: ToDoItemButtonListener) {
            if (item != null) {
                toDoItemTitleView.text = truncateStringView(item.title, TITLE_LENGTH)
                toDoItemContentView.text = truncateStringView(item.content, CONTENT_LENGTH)
                toDoItemDate.text = convertDate(item.dueDate)
                toDoItemCheckBox.isChecked = item.completed
                toDoItemCheckBox.setOnClickListener {
                    handler.completionChanged(item.copy(completed = toDoItemCheckBox.isChecked))
                }
            }
        }

        // Get the date to use for the due date
        private fun convertDate(time: Long) = DateFormat.getDateTimeInstance().format(time)

        companion object {
            fun create(parent: ViewGroup): ToDoItemViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_item, parent, false)
                return ToDoItemViewHolder(view)
            }

            // Max lengths for title and content
            private const val TITLE_LENGTH = 25
            private const val CONTENT_LENGTH = 25
        }

        // Truncate String to given int length and add ellipses if it is too long
        private fun truncateStringView(string: String, maxChars: Int): String {
            val newString = string.take(maxChars)
            return if (newString.length == maxChars) {
                "$newString…"
            }
            else {
                newString
            }
        }
    }

    // Comparator for initialization
    class ToDoItemComparator : DiffUtil.ItemCallback<ToDoItem>() {
        override fun areItemsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean {
            return oldItem == newItem
        }
    }

    // Used for callbacks to the mainactivity
    interface ToDoItemButtonListener {
        fun editRequested(item: ToDoItem)
        fun completionChanged(item: ToDoItem)
    }
}
