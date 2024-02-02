package com.muham.petv01

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

// Broadcast Receiver sınıfı
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Bildirim oluşturma
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        // Bildirim kanalı oluşturma (API seviyesi 26 ve üstü için gerekli)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default_channel_id",
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Bildirim oluşturma
        val notification = NotificationCompat.Builder(context, "default_channel_id")
            .setContentTitle("Geri Sayım Tamamlandı!")
            .setContentText("Geri sayım süresi doldu.")
            .setSmallIcon(R.drawable.logo)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Bildirimi gösterme
        notificationManager.notify(1, notification)
    }
}

// Geri sayım süresini ayarlamak için kullanılacak fonksiyon
fun setAlarm(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val alarmIntent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    // Geri sayım süresi (örneğin 24 saat)
    val timeInMillis = System.currentTimeMillis() + 24 * 60 * 60 * 1000

    // AlarmManager'ı ayarla
    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
}
