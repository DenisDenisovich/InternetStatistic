package aero.testcompany.internetstat.domain

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
import java.util.*

@TargetApi(Build.VERSION_CODES.M)
class GetPackageNetworkUseCase(
    private val context: Context,
    private val networkStatsManager: NetworkStatsManager,
    private val packageUid: Int
) {
    private lateinit var getTimeLineUseCase: GetTimeLineUseCase

    fun getInfo(interval: Long, period: NetworkPeriod): Pair<List<Long>, List<Long>> {
        val receiverList = arrayListOf<Long>()
        val transmittedList = arrayListOf<Long>()
        getTimeLineUseCase = GetTimeLineUseCase(interval, period)
        val timeLine = getTimeLineUseCase.getTimeLine()
        var startTime: Long
        var endTime: Long
        for (timeIndex in 0 until timeLine.lastIndex) {
            startTime = timeLine[timeIndex]
            endTime = timeLine[timeIndex + 1]
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
                log("Mobile", bucketMobile)
            }
            val bucketWifi = NetworkStats.Bucket()
            while (networkStatsWifi!!.hasNextBucket()) {
                networkStatsWifi.getNextBucket(bucketWifi)
                rxBytes += bucketWifi.rxBytes
                txBytes += bucketWifi.txBytes
                log("WIFI", bucketWifi)
            }
            receiverList.add(rxBytes)
            transmittedList.add(txBytes)
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