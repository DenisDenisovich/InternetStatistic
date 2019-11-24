package aero.testcompany.internetstat.domain.network.minutes

import aero.testcompany.internetstat.domain.network.GetPackageNetworkUseCase
import aero.testcompany.internetstat.models.NetworkPeriod
import android.app.usage.NetworkStatsManager
import android.content.Context

class GetPackageNetworkMinutesUseCase(
    val packageName: String,
    packageUid: Int,
    context: Context,
    networkStatsManager: NetworkStatsManager
) : GetPackageNetworkUseCase(packageUid, context, networkStatsManager) {

    fun getLastMinutesInfo(): Pair<Long, Long> {
        receiverList.clear()
        transmittedList.clear()
        val startTime = System.currentTimeMillis()
        val endTime = startTime + NetworkPeriod.MINUTES.getStep()
        val minutesBytes = calculateBytes(startTime, endTime)
        receiverList.add(minutesBytes.first)
        transmittedList.add(minutesBytes.second)
        return minutesBytes
    }
}