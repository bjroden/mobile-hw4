package com.example.todolist

import android.app.*
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NewToDoItemActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    private lateinit var editTitleView: EditText
    private lateinit var editContentView: EditText
    private lateinit var editDateView: TextView
    private lateinit var editTimeView: TextView
    private lateinit var isCompletedView: CheckBox
    private var date = initCalendar()
    private var existingItem: ToDoItem? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_item)
        editTitleView = findViewById(R.id.edit_title)
        editContentView = findViewById(R.id.edit_content)
        editDateView = findViewById(R.id.dueDate)
        editDateView.text = SimpleDateFormat.getDateInstance().format(date)
        // Launch datepicker if clicked
        editDateView.setOnClickListener { DatePickerFragment(this).show(supportFragmentManager, "datePicker") }
        editTimeView = findViewById(R.id.dueTime)
        editTimeView.text = SimpleDateFormat.getTimeInstance().format(date)
        // Launch timepicker if clicked
        editTimeView.setOnClickListener { TimePickerFragment(this).show(supportFragmentManager, "timePicker") }
        isCompletedView = findViewById(R.id.completed)
        // Launch relevant action to end activity
        val saveButton = findViewById<FloatingActionButton>(R.id.save_button)
        saveButton.setOnClickListener { saveClicked() }
        val deleteButton = findViewById<FloatingActionButton>(R.id.delete_button)
        deleteButton.setOnClickListener { deleteClicked() }

        // Check for existing data, since this activity is reused for creation and editing
        val item = intent.getParcelableExtra<ToDoItem>(EXTRA_ITEM)
        item?.let {
            existingItem = it
            editTitleView.setText(it.title)
            editContentView.setText(it.content)
            editDateView.text = SimpleDateFormat.getDateInstance().format(it.dueDate)
            editTimeView.text = SimpleDateFormat.getTimeInstance().format(it.dueDate)
            date.timeInMillis = it.dueDate
            isCompletedView.isChecked = it.completed
            deleteButton.show()
        } ?: deleteButton.hide()
    }

    // Send a confirmation dialog, then end activity
    private fun deleteClicked() {
        AlertDialog.Builder(this).apply {
            setMessage(R.string.confirm_delete)
            setPositiveButton(R.string.dialog_yes) { _, _ ->
                val replyIntent = Intent()
                existingItem?.let { replyIntent.putExtra(EXTRA_ITEM, it) }
                replyIntent.putExtra(SHOULD_DELETE, SHOULD_DELETE)
                setResult(Activity.RESULT_OK, replyIntent)
                finish()
            }
            setNegativeButton(R.string.dialog_no) { _, _ -> }
        }.create().show()
    }

    // Warn user if text is empty, then send item back to MainActivity
    private fun saveClicked() {
        val replyIntent = Intent()
        if (TextUtils.isEmpty(editTitleView.text) || TextUtils.isEmpty(editContentView.text)) {
            Toast.makeText(
                applicationContext,
                R.string.fields_empty,
                Toast.LENGTH_LONG
            ).show()
        }
        else {
            val title = editTitleView.text.toString()
            val content = editContentView.text.toString()
            val epochTime: Long = date.timeInMillis
            val completed = isCompletedView.isChecked
            val item = ToDoItem(existingItem?.id, "", title, content, epochTime, completed)
            replyIntent.putExtra(EXTRA_ITEM, item)
            setResult(Activity.RESULT_OK, replyIntent)
            finish()
        }
    }

    // Callback for time set
    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        date.set(Calendar.HOUR_OF_DAY, hourOfDay)
        date.set(Calendar.MINUTE, minute)
        editTimeView.text = SimpleDateFormat.getTimeInstance().format(date)
    }

    // Callback for date set
    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        date.set(Calendar.YEAR, year)
        date.set(Calendar.MONTH, month)
        date.set(Calendar.DAY_OF_MONTH, day)
        editDateView.text = SimpleDateFormat.getDateInstance().format(date)
    }

    // Class for picking time
    class TimePickerFragment(private val listener: NewToDoItemActivity) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            return TimePickerDialog(activity, listener, hour, minute, DateFormat.is24HourFormat(activity))
        }
    }

    // Class for picking date
    class DatePickerFragment(private val listener: NewToDoItemActivity) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            return DatePickerDialog(requireContext(), listener, year, month, day)
        }
    }

    // Set initial time to be 12pm tomorrow
    private fun initCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        return calendar
    }

    companion object {
        const val EXTRA_ITEM = "com.example.android.wordlistsql.ITEM"
        const val SHOULD_DELETE = "DELETE"
    }
}