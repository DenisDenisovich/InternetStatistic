package aero.testcompany.internetstat.domain.network

import aero.testcompany.internetstat.domain.GetTimeLineUseCase
import aero.testcompany.internetstat.models.NetworkPeriod
import aero.testcompany.internetstat.util.getFullDate
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log

@TargetApi(Build.VERSION_CODES.M)
open class GetPackageNetworkUseCase(
    protected val context: Context,
    protected val networkStatsManager: NetworkStatsManager,
    var packageUid: Int
) {
   protected lateinit var getTimeLineUseCase: GetTimeLineUseCase
   protected val receiverList = arrayListOf<Long>()
   protected val transmittedList = arrayListOf<Long>()
    open fun getInfo(interval: Long, period: NetworkPeriod): Pair<List<Long>, List<Long>> {
        if (period == NetworkPeriod.MINUTES) {
            throw Exception("For MINUTES period use GetPackageNetworkMinutesUseCase")
        }
        receiverList.clear()
        transmittedList.clear()
        getTimeLineUseCase = GetTimeLineUseCase(interval, period)
        val timeLine = getTimeLineUseCase.getTimeLine()
        var startTime: Long
        var endTime: Long
        for (timeIndex in 0 until timeLine.lastIndex) {
            startTime = timeLine[timeIndex]
            endTime = timeLine[timeIndex + 1]
            val (received, transmitted) = calculateBytes(startTime, endTime)
            receiverList.add(received)
            transmittedList.add(transmitted)
        }
        return Pair(receiverList, transmittedList)
    }

    protected fun calculateBytes(startTime: Long, endTime: Long): Pair<Long, Long> {
        val networkStatsMobile: NetworkStats? = try {
            networkStatsManager.queryDetailsForUid(
                ConnectivityManager.TYPE_MOBILE,
                getSubscriberId(ConnectivityManager.TYPE_MOBILE),
                startTime,
                endTime,
                packageUid
            )
        } catch (e: Exception) {
            e.printStackTrace()
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
            e.printStackTrace()
            null
        }
        var rxBytes = 0L
        var txBytes = 0L
        val bucketMobile = NetworkStats.Bucket()
        networkStatsMobile?.let {
            while (networkStatsMobile.hasNextBucket()) {
                networkStatsMobile.getNextBucket(bucketMobile)
                rxBytes += bucketMobile.rxBytes
                txBytes += bucketMobile.txBytes
                log("Mobile", bucketMobile)
            }
        }
        val bucketWifi = NetworkStats.Bucket()
        networkStatsWifi?.let {
            while (networkStatsWifi.hasNextBucket()) {
                networkStatsWifi.getNextBucket(bucketWifi)
                rxBytes += bucketWifi.rxBytes
                txBytes += bucketWifi.txBytes
                log("WIFI", bucketWifi)
            }
        }
        return Pair(rxBytes, txBytes)
    }

    @SuppressLint("MissingPermission")
    private fun getSubscriberId(networkType: Int): String {
        if (ConnectivityManager.TYPE_MOBILE == networkType) {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return tm.subscriberId
        }
        return ""
    }

    private fun log(description: String, bucket: NetworkStats.Bucket) {
        Log.d(
            "LogTime",
            "$description start: " + "${bucket.startTimeStamp.getFullDate()}, " +
                "end: ${bucket.endTimeStamp.getFullDate()}, " +
                "rx: ${bucket.rxBytes}, " +
                "tx: ${bucket.txBytes}"
        )
    }
}