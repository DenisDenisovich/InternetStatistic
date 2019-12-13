package aero.testcompany.internetstat.domain.network.minutes

import aero.testcompany.internetstat.data.db.NetworkEntity
import aero.testcompany.internetstat.domain.network.GetPackageNetworkUseCase
import aero.testcompany.internetstat.domain.timeline.GetTimeLineMinutesUseCase
import aero.testcompany.internetstat.models.NetworkInterval
import aero.testcompany.internetstat.models.NetworkPeriod
import aero.testcompany.internetstat.models.bucket.BucketBytes
import aero.testcompany.internetstat.models.bucket.BucketInfo
import aero.testcompany.internetstat.models.bucket.BucketSource
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
        getTimeLineUseCase = GetTimeLineMinutesUseCase(NetworkPeriod.DAY.getStep())
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
            var startIndex = timeLine.lastIndex
            var endIndex = timeLine.lastIndex
            while (startIndex > 0) {
                endTime = timeLine[endIndex]
                startIndex = endIndex - 50
                if (startIndex < 0) {
                    startIndex = 0
                }
                startTime = timeLine[startIndex]
                calculateBytesMinutes(
                    startTime,
                    endTime,
                    ArrayList(timeLine.subList(startIndex, endIndex))
                ).let {
                    buckets.addAll(it)
                }
                val newDataPart = ArrayList(buckets)
                bucketLiveData.postValue(newDataPart)
                bucketsList.addAll(newDataPart)
                buckets.clear()
                endIndex = startIndex - 1
            }
        }
    }

    private fun calculateBytesMinutes(
        startTime: Long,
        endTime: Long,
        subTimeLine: ArrayList<Long>
    ): ArrayList<BucketInfo> {
        val networkEntries =
            db.networkDao().getByInterval(startTime, endTime).mapNotNull { it.toBucketInfo() }
        val buckets = ArrayList(subTimeLine.map { BucketInfo() })
        if (networkEntries.isEmpty()) {
            return buckets
        }
        var currentIndex = 0
        for (timeIndex in subTimeLine.indices.reversed()) {
            if (currentIndex < networkEntries.size &&
                subTimeLine[timeIndex] == networkEntries[currentIndex].first
            ) {
                buckets[timeIndex] = networkEntries[currentIndex].second
                currentIndex++
            } else {
                buckets[timeIndex] = BucketInfo()
            }
        }
        return buckets
    }

    private fun NetworkEntity.toBucketInfo(): Pair<Long, BucketInfo>? {
        return applicationMap[packageName]?.let { packageId ->
            // find data for packageName
            val packageData = data
                .split(":")
                .filter { it.startsWith(packageId.toString()) }
                .getOrNull(0)
            packageData?.let { it ->
                val sources = getSources(it)
                val (allMob, allWifi) = getMobileWifi(sources[0])
                val allMobBytes = getBytes(allMob)
                val allWifiBytes = getBytes(allWifi)
                val (forMob, forWifi) = getMobileWifi(sources[1])
                val forMobBytes = getBytes(forMob)
                val forWifiBytes = getBytes(forWifi)
                val (backMob, backWifi) = getMobileWifi(sources[2])
                val backMobBytes = getBytes(backMob)
                val backWifiBytes = getBytes(backWifi)
                val bucketInfo = BucketInfo(
                    BucketSource(
                        BucketBytes(allMobBytes.first, allMobBytes.second),
                        BucketBytes(allWifiBytes.first, allWifiBytes.second)
                    ),
                    BucketSource(
                        BucketBytes(forMobBytes.first, forMobBytes.second),
                        BucketBytes(forWifiBytes.first, forWifiBytes.second)
                    ),
                    BucketSource(
                        BucketBytes(backMobBytes.first, backMobBytes.second),
                        BucketBytes(backWifiBytes.first, backWifiBytes.second)
                    )
                )
                Pair(time, bucketInfo)
            }
        }
    }

    /**
     * Return array of sources.
     * Map string {{...}{...}},{{...}{...}},{{},{}}
     * to array:
     * {{...}{...}},
     * {{...}{...}},
     * {{},{}}
     * */
    private fun getSources(packageData: String): ArrayList<String> {
        val networkData =
            packageData.substring(packageData.indexOfFirst { it == '{' }, packageData.length)
        val sources = arrayListOf<String>()
        var previewSourceIndex = 0
        for (index in networkData.indices) {
            if (networkData[index] == ',' &&
                networkData[index - 1] == '}' &&
                networkData[index + 1] == '{'
            ) {
                sources.add(networkData.substring(previewSourceIndex, index))
                previewSourceIndex = index + 1
            }
        }
        sources.add(networkData.substring(previewSourceIndex, networkData.length))
        return sources
    }

    /**
     * Map string {{...}{...}} to Pair(...,...)
     * */
    private fun getMobileWifi(source: String): Pair<String, String> =
        if (source == "{}") {
            Pair("", "")
        } else {
            val data = source.split("}{")
            val mob = data[0].substring(2, data[0].length)
            val wifi = data[1].substring(0, data[1].lastIndex - 1)
            Pair(mob, wifi)
        }

    private fun getBytes(bytesString: String): Pair<Long, Long> =
        if (bytesString.isEmpty()) {
            Pair(0L, 0L)
        } else {
            val data = bytesString.split(",")
            val mob = data[0].toLong()
            val wifi = data[1].toLong()
            Pair(mob, wifi)
        }
}