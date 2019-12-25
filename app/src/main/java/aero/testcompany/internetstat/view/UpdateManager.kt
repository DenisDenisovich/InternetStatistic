package aero.testcompany.internetstat.view

import aero.testcompany.internetstat.domain.network.api.SyncNetworkDataWorker
import aero.testcompany.internetstat.util.isNetworkConnected
import android.app.Activity
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class UpdateManager(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        if (isNetworkConnected(applicationContext)) {
            applicationContext.getSharedPreferences("kek", Activity.MODE_PRIVATE).edit().apply {
                putLong("lastUpdateTime", System.currentTimeMillis())
                apply()
            }
            val syncNetworkDataWorker = SyncNetworkDataWorker(applicationContext)
            syncNetworkDataWorker.startBlocking()
        }
        return Result.success()
    }
}