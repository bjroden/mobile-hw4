package com.example.todolist

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), ToDoListAdapter.ToDoItemButtonListener {
    private val toDoListViewModel: ToDoListViewModel by viewModels {
        ToDoListViewModel.ToDoListViewModelFactory((application as ToDoListApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = Firebase.firestore.collection("items-test")

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = ToDoListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Actions to perform when database receives updates. Will submit the new list to the
        // recyclerview, and alarm handler to update whether notification times should be modified
        toDoListViewModel.allToDoItems.observe(this) { items ->
            adapter.submitList(items)
            for (item in items) {
                item.id?.let {
                    // Test write
                    db.document(it.toString()).set(item.toMap()).addOnSuccessListener {
                        Log.d("MainActivity", "Success item: $item")
                    }.addOnFailureListener {
                        Log.d("MainActivity", "Failure item: $item")
                    }
                }
            }
        }

        // Launch activity to create new item
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, NewToDoItemActivity::class.java)
            createItem.launch(intent)
        }
    }

    // Launch activity to create new item
    private val createItem = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val item = result.data?.getParcelableExtra<ToDoItem>(NewToDoItemActivity.EXTRA_ITEM)
            item?.let { toDoListViewModel.insert(it) }
        }
        else {
            sendCancelToast()
        }
    }

    // Launch activity to edit item
    override fun editRequested(item: ToDoItem) {
        val intent = Intent(this@MainActivity, NewToDoItemActivity::class.java)
        intent.putExtra(NewToDoItemActivity.EXTRA_ITEM, item)
        editItem.launch(intent)
    }

    // Launch edit item activity. Update or delete depending on user action.
    private val editItem = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val item = result.data?.getParcelableExtra<ToDoItem>(NewToDoItemActivity.EXTRA_ITEM)
            val shouldDelete = result?.data?.getStringExtra(NewToDoItemActivity.SHOULD_DELETE)
            item?.let {
                shouldDelete?.let {
                    toDoListViewModel.deleteItem(item)
                } ?: toDoListViewModel.updateItem(it)
            }
        }
        else {
            sendCancelToast()
        }
    }

    // Update when the completion button is pressed
    override fun completionChanged(item: ToDoItem) {
        toDoListViewModel.updateItem(item)
    }

    // Send toast if user cancels second activity
    private fun sendCancelToast() {
        Toast.makeText(
            applicationContext,
            R.string.action_cancelled,
            Toast.LENGTH_LONG
        ).show()
    }
}