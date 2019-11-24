package aero.testcompany.internetstat.domain.network.minutes

import aero.testcompany.internetstat.domain.network.GetPackageNetworkUseCase
import android.app.usage.NetworkStatsManager
import android.content.Context
import java.util.*

class GetPackageNetworkMinutesUseCase(
    val packageName: String,
    packageUid: Int,
    context: Context,
    networkStatsManager: NetworkStatsManager
) : GetPackageNetworkUseCase(packageUid, context, networkStatsManager) {

    fun getLastMinutesInfo(): Pair<Long, Long> {
        receiverList.clear()
        transmittedList.clear()
        val calendar = GregorianCalendar().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        calendar.add(Calendar.HOUR_OF_DAY, 3)
        val endTime = calendar.timeInMillis
        val minutesBytes = calculateBytes(startTime, endTime)
        receiverList.add(minutesBytes.first)
        transmittedList.add(minutesBytes.second)
        return minutesBytes
    }
}