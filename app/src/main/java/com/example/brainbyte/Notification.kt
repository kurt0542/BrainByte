package com.example.brainbyte

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

const val notificationID = 1
const val channelID = "channel1"
const val titleExtra = "titleExtra"
const val messageExtra = "messageExtra"


class Notification : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(titleExtra) ?: "Reminder"
        val message = intent.getStringExtra(messageExtra) ?: "Time to study!"

        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_home)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(notificationID, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}