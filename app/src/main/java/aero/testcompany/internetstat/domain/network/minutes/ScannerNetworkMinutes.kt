package aero.testcompany.internetstat.domain.network.minutes

import aero.testcompany.internetstat.domain.packageinfo.GetPackageUidUseCase
import aero.testcompany.internetstat.domain.packageinfo.GetPackagesUseCase
import aero.testcompany.internetstat.models.MyPackageInfo
import android.app.usage.NetworkStatsManager
import android.content.Context
import kotlinx.coroutines.*

class ScannerNetworkMinutes(private val context: Context) {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private val networkStartManager =
        context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
    private val calculators: HashMap<String, GetPackageNetworkMinutesUseCase> = HashMap()
    private val packagesList = GetPackagesUseCase(context.packageManager)
    private val packageUid = GetPackageUidUseCase(context)

    private val previewBytes: HashMap<String, Long> = HashMap()

    fun start() {

    }

    private fun startScanning() {
        scope.launch {
            val calcWorks = ArrayList<Deferred<Pair<Long, Long>>>()
            while (isActive) {
                calcWorks.clear()
                updateCalculatorsList()

                delay(1000 * 60)
            }
        }
    }

    private fun updateCalculatorsList() {
        val packages = packagesList.getPackages()
        val packagesMap = HashMap<String, MyPackageInfo>()
        // get packages and update list
        packages.forEach {
            packagesMap[it.packageName] = it
            if (calculators.containsKey(it.packageName)) {
                calculators[it.packageName]?.packageUid = packageUid.getUid(it.packageName)
            } else {
                calculators[it.packageName] = GetPackageNetworkMinutesUseCase(
                    context,
                    networkStartManager,
                    packageUid.getUid(it.packageName)
                )
            }
        }
        // check deleted packages and remove it from list
        calculators.keys.forEach {
            if (!packagesMap.containsKey(it)) {
                calculators.remove(it)
            }
        }
    }

    fun stop() {
        scope.cancel()
    }
}