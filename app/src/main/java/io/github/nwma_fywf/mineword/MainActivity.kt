package io.github.nwma_fywf.mineword

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.github.nwma_fywf.mineword.data.local.FontStyle
import io.github.nwma_fywf.mineword.data.local.ThemeMode
import io.github.nwma_fywf.mineword.data.worker.ReviewScheduler
import io.github.nwma_fywf.mineword.ui.screen.MineWordApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        requestNotificationPermission()
        requestExactAlarmPermission()
        
        val application = applicationContext as MineWordApplication
        
        setContent {
            val themeMode by application.themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val useDynamicColor by application.themePreferences.useDynamicColor.collectAsState(initial = true)
            val fontStyle by application.themePreferences.fontStyle.collectAsState(initial = FontStyle.DEFAULT)
            val customFontPath by application.themePreferences.customFontPath.collectAsState(initial = null)
            val customPrimaryColor by application.themePreferences.customPrimaryColor.collectAsState(initial = null)
            val customSecondaryColor by application.themePreferences.customSecondaryColor.collectAsState(initial = null)
            val customTertiaryColor by application.themePreferences.customTertiaryColor.collectAsState(initial = null)
            val fontScale by application.themePreferences.fontScale.collectAsState(initial = 1.0f)
            
            MineWordApp(
                application = application,
                repository = application.repository,
                themeMode = themeMode,
                useDynamicColor = useDynamicColor,
                fontStyle = fontStyle,
                customFontPath = customFontPath,
                customPrimaryColor = customPrimaryColor,
                customSecondaryColor = customSecondaryColor,
                customTertiaryColor = customTertiaryColor,
                fontScale = fontScale
            )
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    checkBatteryOptimization()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_CODE
                    )
                }
                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_CODE
                    )
                }
            }
        } else {
            checkBatteryOptimization()
        }
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
                return
            }
        }
        checkBatteryOptimization()
    }

    private fun checkBatteryOptimization() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }
        scheduleReminderIfEnabled()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkBatteryOptimization()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.notification_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
                checkBatteryOptimization()
            }
        }
    }

    private fun scheduleReminderIfEnabled() {
        val prefs = getSharedPreferences("mineword_prefs", MODE_PRIVATE)
        val enabled = prefs.getBoolean("review_reminder_enabled", false)
        if (enabled) {
            val hour = prefs.getInt("review_reminder_hour", 20)
            val minute = prefs.getInt("review_reminder_minute", 0)
            ReviewScheduler.scheduleReviewReminder(this, hour, minute)
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 1001
    }
}
