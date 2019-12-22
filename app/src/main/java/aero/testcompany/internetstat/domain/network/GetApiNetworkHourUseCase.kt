package aero.testcompany.internetstat.domain.network

import android.app.usage.NetworkStatsManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

class GetApiNetworkHourUseCase(
    packageUid: Int,
    context: Context,
    networkStatsManager: NetworkStatsManager,
    scope: CoroutineScope
) : GetPackageNetworkUseCase(packageUid, context, networkStatsManager) {

    init {
        workScope?.cancel()
        workScope = scope
    }

    suspend fun getNetworkInfo(startTime: Long, endTime: Long) = calculateBytes(startTime, endTime)
}