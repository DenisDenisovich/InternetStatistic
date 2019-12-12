package aero.testcompany.internetstat.domain.network.minutes

import aero.testcompany.internetstat.data.db.NetworkEntity
import aero.testcompany.internetstat.domain.network.GetPackageNetworkUseCase
import aero.testcompany.internetstat.domain.timeline.GetTimeLineMinutesUseCase
import aero.testcompany.internetstat.models.NetworkPeriod
import aero.testcompany.internetstat.models.bucket.BucketInfo
import aero.testcompany.internetstat.view.App
import android.app.usage.NetworkStatsManager
import android.content.Context
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class GetPackageNetworkMinutesUseCase(
    val packageName: String,
    packageUid: Int,
    context: Context,
    networkStatsManager: NetworkStatsManager
) : GetPackageNetworkUseCase(packageUid, context, networkStatsManager) {

    private var db = App.db
    private var applicationMap: HashMap<String, Int> = hashMapOf()

    override fun setup(
        interval: Long,
        period: NetworkPeriod,
        scope: CoroutineScope
    ): MutableLiveData<List<BucketInfo>> {
        workScope?.cancel()
        workScope = scope
        bucketLiveData = MutableLiveData()
        getTimeLineUseCase = GetTimeLineMinutesUseCase(interval)
        timeLine = ArrayList(getTimeLineUseCase.getTimeLine())
        return bucketLiveData
    }

    suspend fun getLastMinutesInfo(scope: CoroutineScope): BucketInfo? {
        workScope = scope
        bucketsList.clear()
        val calendar = GregorianCalendar().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        calendar.add(Calendar.HOUR_OF_DAY, 3)
        val endTime = calendar.timeInMillis
        timeLine.clear()
        timeLine.add(startTime)
        val minutesBytes = calculateBytes(startTime, endTime)
        minutesBytes?.let {
            bucketsList.add(minutesBytes)
        }
        return minutesBytes
    }

    override fun start() {
        workScope?.launch {
            applicationMap.clear()
            db.applicationDao().getAll().forEach {
                applicationMap[it.name] = it.uid
            }
            val buckets: ArrayList<BucketInfo> = arrayListOf()
            bucketsList.clear()
            var startTime: Long
            var endTime: Long
            var currentIndex = timeLine.lastIndex
            while (currentIndex > 0) {
                endTime = timeLine[currentIndex]
                currentIndex -= 50
                if (currentIndex < timeLine.size) {
                    currentIndex = 0
                }
                startTime = timeLine[currentIndex]
                calculateBytesMinutes(startTime, endTime)?.let {
                    buckets.addAll(it)
                }
                val newDataPart = ArrayList(buckets)
                bucketLiveData.postValue(newDataPart)
                bucketsList.addAll(newDataPart)
                buckets.clear()
                currentIndex--
            }
        }
    }

    fun calculateBytesMinutes(startTime: Long, endTime: Long): ArrayList<BucketInfo> {
        val networkEntries = db.networkDao().getByInterval(startTime, endTime)

    }

    override suspend fun calculateBytes(startTime: Long, endTime: Long): BucketInfo? = null

    private fun NetworkEntity.toBucketInfo(): BucketInfo {
        applicationMap[packageName]?.let { packageId ->
            val packageData = data
                .split(":")
                .filter { it.startsWith(packageId.toString()) }
                .getOrNull(0)
        }
    }
}