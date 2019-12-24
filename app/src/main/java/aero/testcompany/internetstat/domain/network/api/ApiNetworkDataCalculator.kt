package aero.testcompany.internetstat.domain.network.api


import aero.testcompany.internetstat.data.api.dto.ApiNetworkPeriod
import aero.testcompany.internetstat.data.api.dto.NetworkData
import aero.testcompany.internetstat.domain.network.GetApiNetworkHourUseCase
import aero.testcompany.internetstat.domain.network.minutes.GetPackageNetworkMinutesUseCase
import aero.testcompany.internetstat.domain.packageinfo.GetPackageUidUseCase
import aero.testcompany.internetstat.domain.timeline.GetTimeLineMinutesUseCase
import aero.testcompany.internetstat.domain.timeline.GetTimeLineUseCase
import aero.testcompany.internetstat.models.MyPackageInfo
import aero.testcompany.internetstat.models.NetworkPeriod
import aero.testcompany.internetstat.models.bucket.BucketInfo
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

class ApiNetworkDataCalculator(
    private val user: String,
    private val packages: ArrayList<MyPackageInfo>,
    val context: Context,
    val scope: CoroutineScope,
    minutesLast: Long,
    hourLast: Long
) {
    private lateinit var hourUseCase: GetApiNetworkHourUseCase
    private lateinit var minutesUseCase: GetPackageNetworkMinutesUseCase
    private val hourTimeLine: ArrayList<Long>
    private val minutesTimeLine: ArrayList<Long>
    private val currentLastMinutes: Long
    private val currentLastHour: Long

    init {
        val weakAgoTime = System.currentTimeMillis() - NetworkPeriod.DAY.getStep()
        currentLastMinutes = if (minutesLast < weakAgoTime) {
            weakAgoTime
        } else {
            minutesLast
        }
        minutesTimeLine =
            ArrayList(GetTimeLineMinutesUseCase(System.currentTimeMillis() - currentLastMinutes).getTimeLine())
        currentLastHour = if (hourLast < weakAgoTime) {
            weakAgoTime
        } else {
            hourLast
        }
        hourTimeLine = ArrayList(
            GetTimeLineUseCase(
                System.currentTimeMillis() - currentLastHour,
                NetworkPeriod.HOUR
            ).getTimeLine()
        )
    }

    suspend fun getData(period: NetworkPeriod): ArrayList<NetworkData> {
        val timeLine = if (period == NetworkPeriod.HOUR) {
            hourTimeLine
        } else {
            minutesTimeLine
        }
        var data: ArrayList<NetworkData> = arrayListOf()
        runBlocking {
            data = if(period == NetworkPeriod.HOUR){
                getDataHour(timeLine)
            } else {
                getDataMinutes(timeLine)
            }
        }
        return data
    }

    private suspend fun getDataHour(timeLine: ArrayList<Long>): ArrayList<NetworkData> {
        val resultArray: ArrayList<NetworkData> = arrayListOf()
        for (timeIndex in 0 until timeLine.size - 1) {
            val networkData = StringBuilder()
            val startTime = timeLine[timeIndex]
            val endTime = timeLine[timeIndex + 1]
            packages.forEach {
                initUseCases(it, NetworkPeriod.HOUR)
                val data = hourUseCase.getNetworkInfo(startTime, endTime) ?: BucketInfo()
                val dataString = data.toStringShort()
                if (dataString.isNotEmpty()) {
                    networkData.append(":${it.packageName}$dataString")
                }
            }
            if (networkData.isNotEmpty()) {
                resultArray.add(
                    NetworkData(user, startTime, ApiNetworkPeriod.HOUR, networkData.toString())
                )
            }
        }
        return resultArray
    }

    private fun getDataMinutes(
        timeLine: ArrayList<Long>
    ): ArrayList<NetworkData> {
        val packData: HashMap<String, ArrayList<String>> = hashMapOf()
        val resultArray: ArrayList<NetworkData> = arrayListOf()
        Log.d("OnTikTock", "one")
        packages.forEach {
            initUseCases(it, NetworkPeriod.MINUTES)
            val data = minutesUseCase.startNow(minutesTimeLine)
                .map { it.toStringShort() }.reversed()
            Log.d("OnTikTock", "data: $data")
            packData[it.packageName] = ArrayList(data)
        }
        for(timeIndex in timeLine.indices) {
            val networkData = StringBuilder()
            packData.forEach { (packName, array) ->
                val element = array.getOrNull(timeIndex)
                if (!element.isNullOrEmpty()) {
                    networkData.append(":$packName$element")
                }
            }
            if (networkData.isNotEmpty()) {
                resultArray.add(
                    NetworkData(user, timeLine[timeIndex], ApiNetworkPeriod.MINUTES, networkData.toString())
                )
            }
        }
        return resultArray
    }


    private fun initUseCases(myPackageInfo: MyPackageInfo, period: NetworkPeriod) {
        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val getPackageUidUseCase = GetPackageUidUseCase(context)
        if (period == NetworkPeriod.HOUR) {
            hourUseCase = GetApiNetworkHourUseCase(
                getPackageUidUseCase.getUid(myPackageInfo.packageName),
                context,
                networkStatsManager,
                scope
            )
        } else {
            minutesUseCase = GetPackageNetworkMinutesUseCase(
                myPackageInfo.packageName,
                getPackageUidUseCase.getUid(myPackageInfo.packageName),
                context,
                networkStatsManager
            )
        }
    }
}
