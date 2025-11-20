package com.adarsh.flag.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.adarsh.flag.receiver.StartGameReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAlarm(ms: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, StartGameReceiver::class.java).apply {
            putExtra("scheduled_epoch", ms)
        }

        val pending = PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ms, pending)
    }
}
