package aero.testcompany.internetstat.view

import aero.testcompany.internetstat.R
import aero.testcompany.internetstat.domain.network.api.SyncNetworkDataWorker
import aero.testcompany.internetstat.domain.network.minutes.ScannerNetworkMinutes
import aero.testcompany.internetstat.util.isNetworkConnected
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.os.Build
import androidx.core.app.NotificationCompat
import android.util.Log
import kotlinx.coroutines.*


class StatisticService : Service() {

    val myBinder = StatisticBinder(this)
    var notificationBroadcast = NotificationBroadcast()

    private var minutesScanner: ScannerNetworkMinutes? = null
    @SuppressLint("HardwareIds")
    private var syncNetworkDataWorker: SyncNetworkDataWorker? = null
    private val job = Job()

    private val scope = CoroutineScope(Dispatchers.Default + job)

    override fun onCreate() {
        super.onCreate()
        val intentFilter = IntentFilter(NOTIFICATION_BROADCAST_ACTION)
        registerReceiver(notificationBroadcast, intentFilter)


        minutesScanner = ScannerNetworkMinutes(applicationContext)
        minutesScanner?.start()
/*
        scope.launch {
            while (true) {
                sendNetworkStats()
                delay(1000 * 60 * 60 * 24)
            }
        }
*/
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("LogSend", "onBind")
        startForeground(1, getNotification())
        return myBinder
    }

    override fun onRebind(intent: Intent?) {
        Log.d("LogSend", "onRebind")
        startForeground(1, getNotification())
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("LogSend", "onUnbind")
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationBroadcast)
    }

    fun sendNetworkStats() {
        if (isNetworkConnected(applicationContext)) {
            Log.d("LogSend", "sendNetworkStats")
            syncNetworkDataWorker = SyncNetworkDataWorker(applicationContext)
            syncNetworkDataWorker?.start()
        }
    }

    class StatisticBinder(val service: StatisticService) : Binder()

    private fun getNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText("Network scanning in progress")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(createActivityNotificationPending(0))
            .addAction(
                R.drawable.ic_settings_black_24dp, "Stop",
                createNotificationPendind(1, ON_STOP)
            )
            .addAction(
                R.drawable.ic_settings_black_24dp, "Info",
                createActivityNotificationPending(2, ON_INFO)
            )
            .build()
    }

    private fun createNotificationPendind(requestCode: Int, extra: String) =
        PendingIntent.getBroadcast(
            this,
            requestCode,
            Intent(NOTIFICATION_BROADCAST_ACTION).apply {
                putExtra(extra, true)
            },
            0
        )

    private fun createActivityNotificationPending(requestCode: Int, extra: String? = null) =
        PendingIntent.getActivity(
            this,
            requestCode,
            Intent(this, MainActivity::class.java).apply {
                extra?.let {
                    putExtra(it, true)
                }
            }
            ,
            0
        )

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

    inner class NotificationBroadcast : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val onStop = intent?.getBooleanExtra(ON_STOP, false) ?: false
            if (onStop) {
                stopForeground(true)
                stopSelf()
            }
        }
    }

    companion object {
        const val ON_INFO = "aero.testcompany.internetstat.view.onInfo"
        private const val NOTIFICATION_BROADCAST_ACTION =
            "aero.testcompany.internetstat.view.notificationBroadcastAction"
        private const val ON_STOP = "aero.testcompany.internetstat.view.onStop"
        private const val CHANNEL_ID = "statisticChannel"
    }
}