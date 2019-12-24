package aero.testcompany.internetstat.view

import aero.testcompany.internetstat.R
import aero.testcompany.internetstat.domain.network.api.SyncNetworkDataWorker
import aero.testcompany.internetstat.domain.network.minutes.ScannerNetworkMinutes
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import androidx.core.app.NotificationCompat
import android.app.PendingIntent
import kotlinx.coroutines.*


class StatisticService: Service() {

    val myBinder = StatisticBinder(this)
    private var minutesScanner: ScannerNetworkMinutes? = null
    @SuppressLint("HardwareIds")
    private var syncNetworkDataWorker: SyncNetworkDataWorker? = null
    private val job = Job()

    private val scope = CoroutineScope(Dispatchers.Default + job)

    override fun onCreate() {
        super.onCreate()
        minutesScanner = ScannerNetworkMinutes(applicationContext)
        minutesScanner?.start()
        scope.launch {
            while (true) {
                syncNetworkDataWorker = SyncNetworkDataWorker(applicationContext)
                syncNetworkDataWorker?.start()
                delay(1000 * 60 * 60 * 24)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText("Network scanning in progress")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return myBinder
    }

    class StatisticBinder(val service: StatisticService): Binder()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "statisticChannel"
    }
}