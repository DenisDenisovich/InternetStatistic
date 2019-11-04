package aero.testcompany.internetstat.domain.network.minutes

import aero.testcompany.internetstat.domain.network.GetPackageNetworkUseCase
import aero.testcompany.internetstat.models.NetworkPeriod
import android.app.usage.NetworkStatsManager
import android.content.Context

class GetPackageNetworkMinutesUseCase(
    context: Context,
    networkStatsManager: NetworkStatsManager,
    packageUid: Int
) : GetPackageNetworkUseCase(context, networkStatsManager, packageUid) {

    override fun getInfo(interval: Long, period: NetworkPeriod): Pair<List<Long>, List<Long>> {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + NetworkPeriod.HOUR.getStep()
        calculateBytes(startTime, endTime)
        return Pair(receiverList, transmittedList)
    }
}