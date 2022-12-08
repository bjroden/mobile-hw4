package com.example.todolist

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import android.icu.text.SimpleDateFormat

class AlarmHandler  {
    // Initialize the notification channel for later notification use.
    fun initNotificationChannel(context: Context) {
        NotificationUtils().createNotificationChannel(context)
    }

    // Update alarms for only the difference between the lists.
    fun updateList(context: Context, oldList: List<ToDoItem>, newList: List<ToDoItem>) {
        val diff = DiffUtil.calculateDiff(ToDoItemDiffCallback(oldList, newList))
        diff.dispatchUpdatesTo(AlarmDiffHandler(context, oldList, newList))
    }

    // Sets an alarm for the given item.
    fun setItemAlarm(context: Context, item: ToDoItem) {
        item.id?.let {
            val manager = context.applicationContext.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
            manager.setExact(AlarmManager.RTC_WAKEUP, getTriggerTime(item), getPendingIntent(context, item, it))
        }
    }

    // Deletes the currently set alarm for the given item
    fun deleteItemAlarm(context: Context, item: ToDoItem) {
        item.id?.let {
            val manager = context.applicationContext.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
            manager.cancel(getPendingIntent(context, item, it))
        }
    }

    // Get the pending intent for use in setting the alarm with the manager. The id is used to make a different intent for each item uniquely.
    private fun getPendingIntent(context: Context, item: ToDoItem, id: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(EXTRA_ITEM, item)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    // Get the time to send a warning. Will try to send it a few hours beforehand first,
    // then at the dueDate if it is within that few-hour window, and finally trigger immediately
    // if the date has already past.
    private fun getTriggerTime(item: ToDoItem): Long {
        val currentTime = System.currentTimeMillis()
        val timeWithOffset = item.dueDate - TIME_OFFSET
        return if (currentTime < timeWithOffset) {
            timeWithOffset
        }
        else if (currentTime > timeWithOffset && currentTime < item.dueDate) {
            item.dueDate
        }
        else {
            currentTime
        }
    }

    companion object {
        const val EXTRA_ITEM = "com.example.todolist.AlarmReceiver.ITEM"
        // How long before an item's dueDate a notification should be sent.
        private const val TIME_OFFSET = 3 * 3600 * 1000
    }

    // Listener for alarms set with previous methods
    class AlarmReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val item = intent?.getParcelableExtra<ToDoItem>(EXTRA_ITEM)
            if (item != null && context != null && !item.completed) {
                NotificationUtils().createNotification(context, item)
            }
        }
    }

    // Class to calculate the difference between lists. Will perform the appropriate message set
    // and deletes if new items are added.
    inner class AlarmDiffHandler (
        private val context: Context,
        private val oldList: List<ToDoItem>,
        private val newList: List<ToDoItem>
    ) : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {
            for (i in 0 until count) {
                setItemAlarm(context, newList[i + position])
            }
        }

        override fun onRemoved(position: Int, count: Int) {
            for (i in 0 until count) {
                deleteItemAlarm(context, oldList[i + position])
            }
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {}

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            for (i in 0 until count) {
                setItemAlarm(context, newList[i + position])
            }
        }
    }

    // Helper class for sending notifications
    class NotificationUtils {

        private val CHANNEL_ID = "CHANNEL NAME"

        // Create channel
        fun createNotificationChannel(context: Context) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Send a notification using the item's title and dueDate as additional information
        fun createNotification(context: Context, item: ToDoItem){
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(
                    context.getString(
                        R.string.notification_description,
                        item.title,
                        SimpleDateFormat.getTimeInstance().format(item.dueDate)
                    )
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(context)) {
                // notificationId is a unique int for each notification that you must define
                item.id?.let { notify(it, builder.build()) }
            }
        }
    }
}