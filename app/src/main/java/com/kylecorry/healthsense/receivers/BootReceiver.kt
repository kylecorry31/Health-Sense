package com.kylecorry.healthsense.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kylecorry.healthsense.medicine.infrastructure.MedicineReminderReceiver

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED && context != null) {
            MedicineReminderReceiver.start(context)
        }
    }
}