package aero.testcompany.internetstat.domain.network.api

import aero.testcompany.internetstat.domain.network.GetApiNetworkHourUseCase
import aero.testcompany.internetstat.domain.network.minutes.GetPackageNetworkMinutesUseCase
import aero.testcompany.internetstat.domain.packageinfo.GetPackageUidUseCase
import aero.testcompany.internetstat.models.MyPackageInfo
import aero.testcompany.internetstat.models.NetworkPeriod
import aero.testcompany.internetstat.models.bucket.BucketInfo
import aero.testcompany.internetstat.util.getFullDate
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope

class ApiNetworkDataProvider(
    val context: Context,
    val scope: CoroutineScope
) {
    private lateinit var hourUseCase: GetApiNetworkHourUseCase
    private lateinit var minutesUseCase: GetPackageNetworkMinutesUseCase

    suspend fun getData(
        myPackageInfo: MyPackageInfo,
        startTime: Long,
        endTime: Long,
        period: NetworkPeriod
    ): BucketInfo {
        initUseCases(myPackageInfo, period)
        return if (period == NetworkPeriod.HOUR) {
            Log.d("OnTikTock", "hourUseCase")
            hourUseCase.getNetworkInfo(startTime, endTime) ?: BucketInfo()
        } else {
            minutesUseCase.calculateBytesMinutes(startTime, endTime).getOrNull(0) ?: BucketInfo()
        }
    }

    private fun initUseCases(myPackageInfo: MyPackageInfo, period: NetworkPeriod) {
        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val getPackageUidUseCase = GetPackageUidUseCase(context)
        if (period == NetworkPeriod.HOUR) {
            hourUseCase = GetApiNetworkHourUseCase(
                getPackageUidUseCase.getUid(myPackageInfo.packageName),
                context,
                networkStatsManager,
                scope
            )
        } else {
            minutesUseCase = GetPackageNetworkMinutesUseCase(
                myPackageInfo.packageName,
                getPackageUidUseCase.getUid(myPackageInfo.packageName),
                context,
                networkStatsManager
            )
        }
    }
}