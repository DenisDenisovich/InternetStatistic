package aero.testcompany.internetstat.domain.network.api


import aero.testcompany.internetstat.data.api.dto.ApiNetworkPeriod
import aero.testcompany.internetstat.data.api.dto.NetworkData
import aero.testcompany.internetstat.domain.timeline.GetTimeLineMinutesUseCase
import aero.testcompany.internetstat.domain.timeline.GetTimeLineUseCase
import aero.testcompany.internetstat.models.MyPackageInfo
import aero.testcompany.internetstat.models.NetworkPeriod
import aero.testcompany.internetstat.util.getFullDate
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ApiNetworkDataCalculator(
    private val user: String,
    private val packages: ArrayList<MyPackageInfo>,
    val context: Context,
    scope: CoroutineScope,
    minutesLast: Long,
    hourLast: Long
) {

    private val dataProvider = ApiNetworkDataProvider(context, scope)
    private val hourTimeLine: ArrayList<Long>
    private val minutesTimeLine: ArrayList<Long>
    private val currentLastMinutes: Long
    private val currentLastHour: Long

    init {
        val weakAgoTime = System.currentTimeMillis() - (NetworkPeriod.DAY.getStep() / 2) + NetworkPeriod.HOUR.getStep()
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
            data = getData(period, timeLine)
        }
        return data
    }

    private suspend fun getData(
        period: NetworkPeriod,
        timeLine: ArrayList<Long>
    ): ArrayList<NetworkData> {
        val apiPeriod = if (period == NetworkPeriod.HOUR) {
            ApiNetworkPeriod.MINUTES
        } else {
            ApiNetworkPeriod.HOUR
        }
        val resultArray: ArrayList<NetworkData> = arrayListOf()
        for (timeIndex in 0 until timeLine.size - 1) {
            val networkData = StringBuilder()
            val startTime = timeLine[timeIndex]
            val endTime = timeLine[timeIndex + 1]
            Log.d("OnTikTock", "time: ${startTime.getFullDate()}")
            packages.forEach {
                val data = dataProvider.getData(it, startTime, endTime, period)
                val dataString = data.toStringShort()
                if (dataString.isNotEmpty()) {
                    networkData.append(":${it.packageName}$dataString")
                }
            }
            if (networkData.isNotEmpty()) {
                resultArray.add(
                    NetworkData(user, startTime, apiPeriod, networkData.toString())
                )
            }
            Log.d("OnTikTock", resultArray.lastOrNull().toString())
        }
        return resultArray
    }
}
