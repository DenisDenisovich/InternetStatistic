package aero.testcompany.internetstat.domain.network.minutes

import aero.testcompany.internetstat.domain.packageinfo.GetPackageUidUseCase
import aero.testcompany.internetstat.domain.packageinfo.GetPackagesUseCase
import aero.testcompany.internetstat.util.toMb
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.*

class ScannerNetworkMinutes(private val context: Context) {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private val networkStartManager =
        context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
    private val calculators: HashMap<String, GetPackageNetworkMinutesUseCase> = HashMap()
    private val packagesList = GetPackagesUseCase(context.packageManager)
    private val packageUid = GetPackageUidUseCase(context)

    private val previewBytes: HashMap<String, Pair<Long, Long>> = HashMap()
    private val nextBytes: HashMap<String, Pair<Long, Long>> = HashMap()
    private val minuteBytes: HashMap<String, Pair<Long, Long>> = HashMap()

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

    private fun calculateMinuteNetwork() {
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
                minuteBytes[key] = Pair(
                    nextBytes.first - previewByte.first,
                    nextBytes.second - previewByte.second
                )
            }
        }
        // replace previewBytes with nextBytes
        previewBytes.clear()
        nextBytes.forEach { (key, nextBytes) ->
            previewBytes[key] = nextBytes
        }
    }

    private fun fillBytes(hashBytes: HashMap<String, Pair<Long, Long>>) {
        hashBytes.clear()
        calculators.forEach {
            with(it.value) {
                hashBytes[packageName] = getLastMinutesInfo()
            }
        }
    }

    private fun log() {
        minuteBytes.forEach { (packageName, stat) ->
            Log.d(
                "LogStatMinutes",
                "$packageName - received: ${stat.first.toMb()}, transmitted - ${stat.second.toMb()}"
            )
        }
        Log.d("LogStatMinutes", "/////////////////////////////////////////////////////////////")
    }
}