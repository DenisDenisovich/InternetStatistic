package aero.testcompany.internetstat.domain.network

import aero.testcompany.internetstat.domain.GetTimeLineUseCase
import aero.testcompany.internetstat.models.ApplicationState
import aero.testcompany.internetstat.models.bucket.BucketBytes
import aero.testcompany.internetstat.models.NetworkPeriod
import aero.testcompany.internetstat.models.NetworkSource
import aero.testcompany.internetstat.models.bucket.BucketInfo
import aero.testcompany.internetstat.models.bucket.BucketSource
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
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*

@TargetApi(Build.VERSION_CODES.M)
open class GetPackageNetworkUseCase(
    val packageUid: Int,
    protected val context: Context,
    protected val networkStatsManager: NetworkStatsManager
) {
    var timeLine: List<Long> = listOf()
    private set
    protected var workScope: CoroutineScope? = null
    protected lateinit var getTimeLineUseCase: GetTimeLineUseCase
    protected var bucketsList: ArrayList<BucketInfo> = arrayListOf()
    private lateinit var bucketLiveData: MutableLiveData<List<BucketInfo>>

    open fun setup(
        interval: Long,
        period: NetworkPeriod,
        scope: CoroutineScope
    ): MutableLiveData<List<BucketInfo>> {
        if (period == NetworkPeriod.MINUTES) {
            throw Exception("For MINUTES period use GetPackageNetworkMinutesUseCase")
        }
        workScope?.cancel()
        workScope = scope
        bucketLiveData = MutableLiveData()
        getTimeLineUseCase = GetTimeLineUseCase(interval, period)
        timeLine = getTimeLineUseCase.getTimeLine()
        return bucketLiveData
    }

    fun start() {
        workScope?.launch {
            val buckets: ArrayList<BucketInfo> = arrayListOf()
            bucketsList.clear()

            var startTime: Long
            var endTime: Long
            for (timeIndex in 0 until timeLine.lastIndex) {
                startTime = timeLine[timeIndex]
                endTime = timeLine[timeIndex + 1]
                calculateBytes(startTime, endTime)?.let {
                    buckets.add(it)
                }
                if (buckets.size == 50) {
                    val newDataPart = ArrayList(buckets)
                    bucketLiveData.postValue(newDataPart)
                    bucketsList.addAll(newDataPart)
                    buckets.clear()
                }
            }
            val newDataPart = ArrayList(buckets)
            bucketLiveData.postValue(newDataPart)
            bucketsList.addAll(newDataPart)
            buckets.clear()
        }
    }

    protected suspend fun calculateBytes(startTime: Long, endTime: Long): BucketInfo? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            workScope?.run {
                val mF = async {
                    getNetworkBytes(
                        startTime,
                        endTime,
                        NetworkSource.MOBILE,
                        ApplicationState.FOREGROUND
                    )
                }
                val mB = async {
                    getNetworkBytes(
                        startTime,
                        endTime,
                        NetworkSource.MOBILE,
                        ApplicationState.BACKGROUND
                    )
                }
                val wF = async {
                    getNetworkBytes(
                        startTime,
                        endTime,
                        NetworkSource.WIFI,
                        ApplicationState.FOREGROUND
                    )
                }
                val wB = async {
                    getNetworkBytes(
                        startTime,
                        endTime,
                        NetworkSource.WIFI,
                        ApplicationState.BACKGROUND
                    )
                }
                val mobileForeground = mF.await()
                val mobileBackground = mB.await()
                val wifiForeground = wF.await()
                val wifiBackground = wB.await()
                val allType = BucketSource(
                    mobile = BucketBytes(
                        mobileBackground.received + mobileForeground.received,
                        mobileBackground.transmitted + mobileForeground.transmitted
                    ),
                    wifi = BucketBytes(
                        wifiBackground.received + wifiForeground.received,
                        wifiBackground.transmitted + wifiForeground.transmitted
                    )
                )
                BucketInfo(
                    allType,
                    BucketSource(mobileForeground, wifiForeground),
                    BucketSource(mobileBackground, wifiBackground)
                )
            }
        } else {
            workScope?.run {
                val mobileAll = async {
                    getNetworkBytes(
                        startTime,
                        endTime,
                        NetworkSource.MOBILE
                    )
                }
                val wifiAll = async {
                    getNetworkBytes(
                        startTime,
                        endTime,
                        NetworkSource.WIFI
                    )
                }
                BucketInfo(BucketSource(mobileAll.await(), wifiAll.await()), null, null)
            }
        }

    private fun getNetworkBytes(
        startTime: Long,
        endTime: Long,
        source: NetworkSource
    ): BucketBytes = try {
        val networkStats = networkStatsManager.queryDetailsForUid(
            source.value,
            getSubscriberId(source.value),
            startTime,
            endTime,
            packageUid
        )
        getNetworkBytes(source, networkStats)
    } catch (e: Exception) {
        e.printStackTrace()
        BucketBytes(0, 0)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun getNetworkBytes(
        startTime: Long,
        endTime: Long,
        source: NetworkSource,
        state: ApplicationState
    ): BucketBytes = try {
        val networkStats = networkStatsManager.queryDetailsForUidTagState(
            source.value,
            getSubscriberId(source.value),
            startTime,
            endTime,
            packageUid,
            NetworkStats.Bucket.TAG_NONE,
            state.value
        )
        getNetworkBytes(source, networkStats)
    } catch (e: Exception) {
        e.printStackTrace()
        BucketBytes(0, 0)
    }

    private fun getNetworkBytes(source: NetworkSource, networkStats: NetworkStats?): BucketBytes {
        var rxBytes = 0L
        var txBytes = 0L
        val bucket = NetworkStats.Bucket()
        networkStats?.let {
            while (networkStats.hasNextBucket()) {
                networkStats.getNextBucket(bucket)
                rxBytes += bucket.rxBytes
                txBytes += bucket.txBytes
                log(source, bucket)
            }
        }
        return BucketBytes(rxBytes, txBytes)
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    private fun getSubscriberId(networkType: Int): String {
        if (ConnectivityManager.TYPE_MOBILE == networkType) {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return tm.subscriberId
        }
        return ""
    }

    private fun log(sourceType: NetworkSource, bucket: NetworkStats.Bucket) {
        val state = when (bucket.state) {
            NetworkStats.Bucket.STATE_FOREGROUND -> "Foreground"
            NetworkStats.Bucket.STATE_DEFAULT -> "Default"
            NetworkStats.Bucket.STATE_ALL -> "All"
            else -> "unknown"
        }
        val source = when (sourceType) {
            NetworkSource.MOBILE -> "Mobile"
            NetworkSource.WIFI -> "Wifi"
            NetworkSource.ALL -> "all"
        }
        Log.d(
            "LogTime",
            "$state, $source, " +
                "start: " + "${bucket.startTimeStamp.getFullDate()}, " +
                "end: ${bucket.endTimeStamp.getFullDate()}, " +
                "rx: ${bucket.rxBytes}, " +
                "tx: ${bucket.txBytes}"
        )
    }
}