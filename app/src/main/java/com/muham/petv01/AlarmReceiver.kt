package com.muham.petv01

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onReceive(context: Context, intent: Intent) {
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        val userId = firebaseAuth.currentUser?.uid

        userId?.let {
            databaseReference.child("reminders").child(it).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val reminderName = data.child("reminderName").getValue(String::class.java)
                        reminderName?.let {
                            showNotification(context, it)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Hata durumunda işlemler
                }
            })
        }
    }

    private fun showNotification(context: Context, reminderName: String) {
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "default_channel_id"
            val channelName = "Default Channel"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "default_channel_id")
            .setContentTitle("CountDown Completed!")
            .setContentText("Reminder: $reminderName")
            .setSmallIcon(R.drawable.logo2) // logo drawable'ınızı buraya ekleyin
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1, notification)
    }
}
