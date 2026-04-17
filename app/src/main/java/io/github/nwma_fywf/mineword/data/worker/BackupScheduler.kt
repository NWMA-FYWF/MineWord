package io.github.nwma_fywf.mineword.data.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object BackupScheduler {
    private const val TAG = "BackupScheduler"

    fun scheduleAutoBackup(context: Context, intervalDays: Int) {
        Log.d(TAG, "Scheduling auto backup every $intervalDays days")

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<BackupWorker>(
            intervalDays.toLong(), TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .addTag("auto_backup")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            BackupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelAutoBackup(context: Context) {
        Log.d(TAG, "Cancelling auto backup")
        WorkManager.getInstance(context).cancelUniqueWork(BackupWorker.WORK_NAME)
    }
}
