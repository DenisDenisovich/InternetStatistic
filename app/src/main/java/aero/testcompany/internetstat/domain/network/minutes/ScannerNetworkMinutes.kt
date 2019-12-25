package aero.testcompany.internetstat.domain.network.minutes

import aero.testcompany.internetstat.data.db.ApplicationEntity
import aero.testcompany.internetstat.data.db.NetworkEntity
import aero.testcompany.internetstat.domain.packageinfo.GetPackageUidUseCase
import aero.testcompany.internetstat.domain.packageinfo.GetPackagesUseCase
import aero.testcompany.internetstat.models.MyPackageInfo
import aero.testcompany.internetstat.models.bucket.BucketInfo
import aero.testcompany.internetstat.util.isSameHour
import aero.testcompany.internetstat.util.minus
import aero.testcompany.internetstat.view.App
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ScannerNetworkMinutes(private val context: Context) {

    var mostActiveApplicationCallback: ((packageInfo: MyPackageInfo, BucketInfo: BucketInfo) -> Unit)? =
        null

    private val db = App.db

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private val networkStartManager =
        context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
    private val calculators: HashMap<String, GetPackageNetworkMinutesUseCase> = HashMap()
    private val packagesListUseCase = GetPackagesUseCase(context.packageManager)
    private var packagesList = ArrayList<MyPackageInfo>()

    private val packageUid = GetPackageUidUseCase(context)
    private val previewBytes: HashMap<String, BucketInfo> = HashMap()
    private val nextBytes: HashMap<String, BucketInfo> = HashMap()

    private val minuteBytes: HashMap<String, BucketInfo> = HashMap()

    private val previewScanTimes: HashMap<String, Long> = HashMap()

    fun start() {
        previewBytes.clear()
        nextBytes.clear()
        minuteBytes.clear()
        startScanning()
    }

    fun stop() {
        scope.cancel()
    }

    private fun startScanning() {
        scope.launch {
            val calcWorks = ArrayList<Deferred<Pair<Long, Long>>>()
            while (isActive) {
                calcWorks.clear()
                updateCalculatorsList()
                calculateMinuteNetwork()
                writeAppsToDb()
                writeNetworkToDb()
                updateMostActiveApplication()
                log()
                logFromDB()
                delay(1000 * 60)
            }
        }
    }

    private fun updateCalculatorsList() {
        packagesList = ArrayList(packagesListUseCase.getPackages())
        packagesList.forEach {
            calculators[it.packageName] = GetPackageNetworkMinutesUseCase(
                it.packageName,
                packageUid.getUid(it.packageName),
                context,
                networkStartManager
            )
        }
    }

    private suspend fun calculateMinuteNetwork() {
        // fill preview bytes if empty
        if (previewBytes.isEmpty()) {
            fillBytes(previewBytes)
            return
        }
        // fill next bytes
        fillBytes(nextBytes)
        // check previewTime
        previewScanTimes.forEach { (key, previewTimestamp) ->
            val currentTimestamp =
                calculators[key]?.timeLine?.getOrNull(0) ?: System.currentTimeMillis()
            if (!currentTimestamp.isSameHour(previewTimestamp)) {
                previewBytes[key] = BucketInfo()
            }
        }
        // calculate minutes network
        minuteBytes.clear()
        nextBytes.forEach { (key, nextBytes) ->
            previewBytes[key]?.let { previewByte ->
                minuteBytes[key] = nextBytes - previewByte
            }
        }
        // replace previewBytes with nextBytes
        previewBytes.clear()
        nextBytes.forEach { (key, nextBytes) ->
            previewBytes[key] = nextBytes
        }
        // save previewTime
        previewScanTimes.clear()
        calculators.forEach { (key, useCase) ->
            previewScanTimes[key] = useCase.timeLine.getOrNull(0) ?: System.currentTimeMillis()
        }
    }

    private suspend fun fillBytes(hashBytes: HashMap<String, BucketInfo>) {
        hashBytes.clear()
        calculators.forEach {
            with(it.value) {
                getLastMinutesInfo(scope)?.let { bytes ->
                    hashBytes[packageName] = bytes
                }
            }
        }
    }

    private fun writeAppsToDb() {
        val appsMap: HashMap<String, Int> = hashMapOf()
        db.applicationDao().getAll().forEach {
            appsMap[it.name] = it.uid
        }
        calculators.forEach { (key, _) ->
            if (!appsMap.containsKey(key)) {
                db.applicationDao().addApplication(ApplicationEntity(0, key))
            }
        }
    }

    private fun writeNetworkToDb() {
        val fileBody = StringBuilder()
        val appIdsMap: HashMap<String, Int> = hashMapOf()
        db.applicationDao().getAll().forEach {
            appIdsMap[it.name] = it.uid
        }
        minuteBytes.forEach { (packageName, stat) ->
            val lineShort = stat.toStringShort()
            if (lineShort.isNotEmpty()) {
                fileBody.append(":${appIdsMap[packageName]}${lineShort}")
            }
        }
        if (fileBody.isNotEmpty()) {
            val calendar = GregorianCalendar().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val time = calendar.timeInMillis
            db.networkDao().addNetworkEntity(NetworkEntity(0, time, fileBody.toString()))
        }
    }

    private fun updateMostActiveApplication() {
        var mostKey = ""
        var previewTotal = 0L
        previewBytes.forEach { (key, bucketInfo) ->
            val all = bucketInfo.all.mobile.received + bucketInfo.all.mobile.transmitted +
                    bucketInfo.all.wifi.received + bucketInfo.all.wifi.transmitted
            if (previewTotal < all) {
                previewTotal = all
                mostKey = key
            }
        }
        previewBytes[mostKey]?.let { bucketInfo ->
            packagesList.find { it.packageName == mostKey }?.let { packageInfo ->
                mostActiveApplicationCallback?.invoke(packageInfo, bucketInfo)
            }
        }
    }

    private fun log() {
        minuteBytes.forEach { (packageName, stat) ->
            val line = stat.toString()
            if (line.isNotEmpty()) {
                Log.d(
                    "LogStatMinutes",
                    "$packageName - $line"
                )
            }
        }
        Log.d("LogStatMinutes", "/////////////////////////////////////////////////////////////")
    }

    private fun logFromDB() {
        val appIdsMap: HashMap<Int, String> = hashMapOf()
        db.applicationDao().getAll().forEach {
            appIdsMap[it.uid] = it.name
        }
        db.networkDao().getAll().forEach {
            Log.d("LogStatMinutesDB", "${it.time} - ${it.data}")
        }
        Log.d("LogStatMinutesDB", "/////////////////////////////////////////")
/*        val aps = db.applicationDao().getAll()
        Log.d("LogStatMinutesDBApps", "size - ${aps.size}")
        aps.forEach {
            Log.d("LogStatMinutesDBApps", "${it.uid} - ${it.name}|")
        }*/
    }
}