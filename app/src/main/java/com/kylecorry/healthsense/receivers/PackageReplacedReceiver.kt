package com.kylecorry.healthsense.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kylecorry.healthsense.medicine.infrastructure.MedicineReminderReceiver

class PackageReplacedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_PACKAGE_REPLACED && context != null) {
            MedicineReminderReceiver.start(context)
        }
    }
}