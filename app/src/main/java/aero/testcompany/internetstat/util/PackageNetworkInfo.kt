package aero.testcompany.internetstat.util

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

@TargetApi(Build.VERSION_CODES.M)
class PackageNetworkInfo(
    private val context: Context,
    private val networkStatsManager: NetworkStatsManager,
    private val packageUid: Int
) {

    private val df = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z")
    fun getInfo(interval: Long, step: Long): Pair<List<Long>, List<Long>> {
        val receiverList = arrayListOf<Long>()
        val transmittedList = arrayListOf<Long>()
        val cal = Calendar.getInstance()
        var startTime = System.currentTimeMillis() - interval
        cal.timeInMillis = startTime
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        startTime = cal.timeInMillis
        var endTime = startTime + step
        for (i in 0 .. interval / step) {
            val networkStatsMobile: NetworkStats? = try {
                networkStatsManager.queryDetailsForUid(
                    ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(ConnectivityManager.TYPE_MOBILE),
                    startTime,
                    endTime,
                    packageUid
                )
            } catch (e: Exception) {
                null
            }
            val networkStatsWifi: NetworkStats? = try {
                networkStatsManager.queryDetailsForUid(
                    ConnectivityManager.TYPE_WIFI,
                    getSubscriberId(ConnectivityManager.TYPE_WIFI),
                    startTime,
                    endTime,
                    packageUid
                )
            } catch (e: Exception) {
                null
            }
            var rxBytes = 0L
            var txBytes = 0L
            val bucketMobile = NetworkStats.Bucket()
            while (networkStatsMobile!!.hasNextBucket()) {
                networkStatsMobile.getNextBucket(bucketMobile)
                rxBytes += bucketMobile.rxBytes
                txBytes += bucketMobile.txBytes
                Log.d("LogTime", "Modile start: ${df.format(bucketMobile.startTimeStamp)}, end: ${df.format(bucketMobile.endTimeStamp)}, rx: ${bucketMobile.rxBytes}, tx: ${bucketMobile.txBytes}")
            }
            val bucketWifi = NetworkStats.Bucket()
            while (networkStatsWifi!!.hasNextBucket()) {
                networkStatsWifi.getNextBucket(bucketWifi)
                rxBytes += bucketWifi.rxBytes
                txBytes += bucketWifi.txBytes
                Log.d("LogTime", "WIFI start: ${df.format(bucketWifi.startTimeStamp)}, end: ${df.format(bucketWifi.endTimeStamp)}, rx: ${bucketWifi.rxBytes}, tx: ${bucketWifi.txBytes}")
            }

            receiverList.add(rxBytes)
            transmittedList.add(txBytes)
            networkStatsMobile.close()
            networkStatsWifi.close()
            startTime += step
            endTime += step
        }

        return Pair(receiverList, transmittedList)
    }

    @SuppressLint("MissingPermission")
    private fun getSubscriberId(networkType: Int): String {
        if (ConnectivityManager.TYPE_MOBILE == networkType) {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return tm.subscriberId
        }
        return ""
    }
}