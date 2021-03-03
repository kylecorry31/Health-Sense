package com.kylecorry.healthsense.steps.infrastructure

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.kylecorry.healthsense.R
import com.kylecorry.healthsense.steps.infrastructure.Pedometer
import com.kylecorry.trailsensecore.domain.time.toZonedDateTime
import com.kylecorry.trailsensecore.infrastructure.persistence.Cache
import com.kylecorry.trailsensecore.infrastructure.system.IntentUtils
import com.kylecorry.trailsensecore.infrastructure.system.NotificationUtils
import com.kylecorry.trailsensecore.infrastructure.system.PermissionUtils
import java.time.Instant
import java.time.LocalDate

class PedometerService: Service() {

    private val pedometer by lazy { Pedometer(this) }
    private val cache by lazy { Cache(this) }

    private var lastSteps = -1
    private var today = LocalDate.now()
    private var totalSteps = 0

    private val stepGoal = 5000

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val lastStep = cache.getLong(CACHE_LAST_TIME)
        today = if (lastStep == null){
            LocalDate.now()
        } else {
            Instant.ofEpochMilli(lastStep).toZonedDateTime().toLocalDate()
        }
        totalSteps = cache.getInt(CACHE_STEPS) ?: 0
        startForeground(NOTIFICATION_ID, getNotification())
        pedometer.start(this::onPedometer)
        return START_STICKY_COMPATIBILITY
    }

    override fun onDestroy() {
        pedometer.stop(this::onPedometer)
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

    private fun onPedometer(): Boolean {
        val now = Instant.now()
        val date = now.toZonedDateTime().toLocalDate()
        if (lastSteps == -1){
            lastSteps = pedometer.steps
        }

        if (date != today){
            today = date
            totalSteps = 0
        }

        totalSteps += pedometer.steps - lastSteps
        lastSteps = pedometer.steps
        cache.putLong(CACHE_LAST_TIME, now.toEpochMilli())
        cache.putInt(CACHE_STEPS, totalSteps)
        NotificationUtils.send(this, NOTIFICATION_ID, getNotification())
        return true
    }

    private fun getNotification(): Notification {
        return NotificationUtils.builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.steps)
            .setContentTitle(getString(R.string.steps))
            .setProgress(stepGoal, totalSteps, false)
            .setContentText("$totalSteps / $stepGoal")
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "pedometer"
        const val CACHE_STEPS = "steps"
        const val CACHE_LAST_TIME = "last_step"
        const val NOTIFICATION_ID = 1279812

        fun intent(context: Context): Intent {
            return Intent(context, PedometerService::class.java)
        }

        fun start(context: Context){
            if (!PermissionUtils.hasPermission(context, Manifest.permission.ACTIVITY_RECOGNITION)){
                return
            }

            if (NotificationUtils.isNotificationActive(context, NOTIFICATION_ID)){
                return
            }

            IntentUtils.startService(context, intent(context), true)
        }

    }

}