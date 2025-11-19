package com.adarsh.flag.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.adarsh.flag.MainActivity
import com.adarsh.flag.R

class StartGameReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val epoch = intent?.getLongExtra("scheduled_epoch", -1L) ?: -1L

        // Build a PendingIntent that opens the GameActivity
        val openIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("started_by_alarm", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            context,
            2001,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build and show notification (create notification channel beforehand)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "flag_game_channel"
        nm.createNotificationChannel(
            NotificationChannel(channelId, "Game Start", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifies when a scheduled game is ready to start"
            }
        )

        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_name) // replace with your drawable
            .setContentTitle("Flags Challenge — Ready to start")
            .setContentText("Tap to open and start the game")
            .setContentIntent(pendingOpen)
            .setAutoCancel(true)
            .build()

        nm.notify(3001, notif)
    }
}

