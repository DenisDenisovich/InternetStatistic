package aero.testcompany.internetstat.domain.network.minutes

import aero.testcompany.internetstat.domain.network.GetPackageNetworkUseCase
import aero.testcompany.internetstat.models.bucket.BucketInfo
import android.app.usage.NetworkStatsManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import java.util.*

class GetPackageNetworkMinutesUseCase(
    val packageName: String,
    packageUid: Int,
    context: Context,
    networkStatsManager: NetworkStatsManager,
    myScope: CoroutineScope
) : GetPackageNetworkUseCase(packageUid, context, networkStatsManager, myScope) {

    suspend fun getLastMinutesInfo(): BucketInfo {
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
        val minutesBytes = calculateBytes(startTime, endTime)
        bucketsList.add(minutesBytes)
        return minutesBytes
    }
}