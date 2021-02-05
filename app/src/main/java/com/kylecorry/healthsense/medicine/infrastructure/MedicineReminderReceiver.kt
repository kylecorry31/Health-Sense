package com.kylecorry.healthsense.medicine.infrastructure

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kylecorry.healthsense.R
import com.kylecorry.healthsense.medicine.domain.Frequency
import com.kylecorry.healthsense.medicine.domain.Medicine
import com.kylecorry.healthsense.medicine.domain.MedicineFoodRequirement
import com.kylecorry.healthsense.medicine.domain.TimeOfDay
import com.kylecorry.trailsensecore.infrastructure.system.AlarmUtils
import com.kylecorry.trailsensecore.infrastructure.system.NotificationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class MedicineReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return

        Log.d("MedicineReminder", "Medicine reminder called at  ${LocalDateTime.now()}")

        val time = getCurrentTime()
        val day = LocalDate.now().dayOfWeek

        val medicineRepo = MedicineRepo.getInstance(context.applicationContext)

        val medicines = runBlocking {
            withContext(Dispatchers.IO) {
                medicineRepo.get().filter {
                    it.isReminding && it.frequency.days.contains(day) && it.frequency.times.contains(
                        time
                    )
                }
            }
        }

        if (medicines.isNotEmpty()) {
            val medicinesToTake = medicines.joinToString("\n\n") {
                "${it.name} (${it.dosage})"
            }

            val notification = NotificationUtils.builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.medicine_reminder))
                .setContentText(medicinesToTake)
                .setSmallIcon(R.drawable.pill)
                .setStyle(
                    Notification.BigTextStyle()
                    .bigText(medicinesToTake))
                .setAutoCancel(false)
                .build()
            NotificationUtils.send(context, NOTIFICATION_ID, notification)
        }

        scheduleNextAlarm(context)
    }

    companion object {
        const val NOTIFICATION_ID = 7239423
        const val PENDING_INTENT_ID = 472098304
        const val NOTIFICATION_CHANNEL_ID = "medicine_reminder"

        fun intent(context: Context): Intent {
            return Intent(context, MedicineReminderReceiver::class.java)
        }

        fun pendingIntent(context: Context): PendingIntent {
            return PendingIntent.getBroadcast(
                context,
                PENDING_INTENT_ID,
                intent(context),
                PendingIntent.FLAG_CANCEL_CURRENT
            )
        }

        fun start(context: Context) {
            if (!AlarmUtils.isAlarmRunning(context, PENDING_INTENT_ID, intent(context))) {
                scheduleNextAlarm(context)
            }
        }

        private fun scheduleNextAlarm(context: Context) {
            AlarmUtils.cancel(context, pendingIntent(context))
            val nextTime = getNextOccurrence()
            Log.d("MedicineReminder", "Next alarm set for $nextTime")
            AlarmUtils.set(
                context,
                nextTime,
                pendingIntent(context),
                exact = true,
                allowWhileIdle = true
            )
        }

        private fun getCurrentTime(): TimeOfDay {
            val time = LocalTime.now()

            // TODO: Do better than this
            return when {
                time.hour < 10 -> TimeOfDay.Morning
                time.hour < 14 -> TimeOfDay.Midday
                time.hour < 18 -> TimeOfDay.Evening
                else -> TimeOfDay.Bedtime
            }
        }

        private fun getNextOccurrence(): LocalDateTime {
            val time = when (getCurrentTime()) {
                TimeOfDay.Morning -> LocalTime.of(12, 0)
                TimeOfDay.Midday -> LocalTime.of(16, 0)
                TimeOfDay.Evening -> LocalTime.of(20, 0)
                TimeOfDay.Bedtime -> LocalTime.of(6, 30)
            }

            val currentTime = LocalTime.now()

            return if (currentTime > time) {
                LocalDate.now().plusDays(1).atTime(time)
            } else {
                LocalDate.now().atTime(time)
            }
        }
    }

}