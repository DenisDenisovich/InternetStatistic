package aero.testcompany.internetstat.domain.network.minutes

import aero.testcompany.internetstat.domain.MyFileWriter
import aero.testcompany.internetstat.domain.packageinfo.GetPackageUidUseCase
import aero.testcompany.internetstat.domain.packageinfo.GetPackagesUseCase
import aero.testcompany.internetstat.models.bucket.BucketInfo
import aero.testcompany.internetstat.util.minus
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ScannerNetworkMinutes(private val context: Context) {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private val networkStartManager =
        context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
    private val calculators: HashMap<String, GetPackageNetworkMinutesUseCase> = HashMap()
    private val packagesList = GetPackagesUseCase(context.packageManager)
    private val packageUid = GetPackageUidUseCase(context)

    private val previewBytes: HashMap<String, BucketInfo> = HashMap()
    private val nextBytes: HashMap<String, BucketInfo> = HashMap()
    private val minuteBytes: HashMap<String, BucketInfo> = HashMap()

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
                withContext(Dispatchers.Default) {
                    updateCalculatorsList()
                    calculateMinuteNetwork()
                }
                log()
                delay(1000 * 60)
            }
        }
    }

    private fun updateCalculatorsList() {
        val packages = packagesList.getPackages()
        packages.forEach {
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
    }

    private suspend fun fillBytes(hashBytes: HashMap<String, BucketInfo>) {
        hashBytes.clear()
        calculators.forEach {
            with(it.value) {
                getLastMinutesInfo()?.let { bytes ->
                    hashBytes[packageName] = bytes
                }
            }
        }
    }

    private fun log() {
        val sdf = SimpleDateFormat("MM_dd_yyyy_HH_mm_ss", Locale.US)
        val time = sdf.format(Date())
        val fileBody = StringBuilder()
        minuteBytes.forEach { (packageName, stat) ->
            val line = stat.toString()
            if (line.isNotEmpty()) {
                Log.d(
                    "LogStatMinutes",
                    "$packageName - $line"
                )
            }
            val lineShort = stat.toStringShort()
            if (lineShort.isNotEmpty()) {
                fileBody.append("${packageName.split(".").lastOrNull()}-${lineShort}")
            }
        }
        if (fileBody.isNotEmpty()) {
            MyFileWriter(context, time).apply {
                add(fileBody.toString())
                close()
            }
        }
        Log.d("LogStatMinutes", "/////////////////////////////////////////////////////////////")
    }
}