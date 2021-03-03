package com.kylecorry.healthsense.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kylecorry.healthsense.medicine.infrastructure.MedicineReminderReceiver
import com.kylecorry.healthsense.steps.infrastructure.PedometerService
import com.kylecorry.trailsensecore.infrastructure.system.IntentUtils
import com.kylecorry.trailsensecore.infrastructure.system.NotificationUtils
import com.kylecorry.trailsensecore.infrastructure.system.PermissionUtils

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            MedicineReminderReceiver.start(context)
            PedometerService.start(context)
        }
    }
}