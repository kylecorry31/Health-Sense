package com.kylecorry.healthsense.ui

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kylecorry.healthsense.R
import com.kylecorry.healthsense.medicine.infrastructure.MedicineReminderReceiver
import com.kylecorry.healthsense.steps.infrastructure.PedometerService
import com.kylecorry.trailsensecore.infrastructure.system.NotificationUtils
import com.kylecorry.trailsensecore.infrastructure.system.PermissionUtils

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navController =
            (supportFragmentManager.findFragmentById(R.id.fragment_holder) as NavHostFragment).navController
        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation.setupWithNavController(navController)

        NotificationUtils.createChannel(
            this,
            MedicineReminderReceiver.NOTIFICATION_CHANNEL_ID,
            getString(R.string.medicine_reminder),
            getString(R.string.medicine_reminder_description),
            NotificationUtils.CHANNEL_IMPORTANCE_HIGH,
            false
        )

        NotificationUtils.createChannel(
            this,
            PedometerService.CHANNEL_ID,
            getString(R.string.steps),
            getString(R.string.steps),
            NotificationUtils.CHANNEL_IMPORTANCE_LOW,
            true
        )

        PedometerService.start(this)
        MedicineReminderReceiver.start(this)
    }
}