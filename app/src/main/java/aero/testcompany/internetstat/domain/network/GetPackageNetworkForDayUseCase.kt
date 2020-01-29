package aero.testcompany.internetstat.domain.network

import aero.testcompany.internetstat.domain.timeline.GetTimeLineUseCase
import aero.testcompany.internetstat.models.NetworkPeriod
import aero.testcompany.internetstat.models.bucket.BucketInfo
import android.app.usage.NetworkStatsManager
import android.content.Context
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*

class GetPackageNetworkForDayUseCase(
    packageUid: Int,
    context: Context,
    networkStatsManager: NetworkStatsManager
) : GetPackageNetworkUseCase(packageUid, context, networkStatsManager) {


    suspend fun startNow(scope: CoroutineScope): List<BucketInfo> {
        workScope?.cancel()
        workScope = scope
        bucketLiveData = MutableLiveData()
        getTimeLineUseCase = GetTimeLineUseCase(NetworkPeriod.DAY.getStep(), NetworkPeriod.HOUR)
        timeLine = ArrayList(getTimeLineUseCase.getTimeLine())
        return getData()
    }


    private suspend fun getData(): List<BucketInfo> {
        return withContext(Dispatchers.Default) {
            val buckets: ArrayList<BucketInfo> = arrayListOf()
            bucketsList.clear()
            var startTime: Long
            var endTime: Long
            for (timeIndex in timeLine.lastIndex downTo 1) {
                startTime = timeLine[timeIndex - 1]
                endTime = timeLine[timeIndex]
                calculateBytes(startTime, endTime)?.let {
                    buckets.add(it)
                }
            }
            bucketsList.addAll(buckets)
            bucketsList
        }
    }
}