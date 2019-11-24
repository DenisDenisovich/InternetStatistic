package aero.testcompany.internetstat.viewmodel

import aero.testcompany.internetstat.domain.packageinfo.GetPackageUidUseCase
import aero.testcompany.internetstat.domain.network.GetPackageNetworkUseCase
import aero.testcompany.internetstat.domain.GetTimeLineUseCase
import aero.testcompany.internetstat.models.MyPackageInfo
import aero.testcompany.internetstat.models.NetworkInfo
import aero.testcompany.internetstat.models.NetworkPeriod
import aero.testcompany.internetstat.models.bucket.BucketInfo
import android.app.usage.NetworkStatsManager
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppInfoViewModel : ViewModel() {
    private lateinit var networkStatsManager: NetworkStatsManager
    private lateinit var getPackageUidUseCase: GetPackageUidUseCase
    private lateinit var packageNetworkUseCase: GetPackageNetworkUseCase
    private lateinit var myPackageInfo: MyPackageInfo
    private var networkPeriod: NetworkPeriod = NetworkPeriod.MONTH

    val networkInfo: MutableLiveData<NetworkInfo> by lazy { MutableLiveData<NetworkInfo>() }
    val totalReceived: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }
    val totalTransmitted: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }

    fun initData(context: Context, myPackageInfo: MyPackageInfo) {
        this.myPackageInfo = myPackageInfo
        networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        getPackageUidUseCase =
            GetPackageUidUseCase(context)
        val uid = getPackageUidUseCase.getUid(myPackageInfo.packageName)
        packageNetworkUseCase =
            GetPackageNetworkUseCase(
                uid,
                context,
                networkStatsManager
            )
    }

    fun update(interval: Long, period: NetworkPeriod) {
        networkPeriod = period
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val bucketList = packageNetworkUseCase.getInfo(interval, period)
                var receivedSum: Long = 0
                var transmittedSum: Long = 0
                bucketList.forEach { bucket ->
                    receivedSum += bucket.all.mobile.received + bucket.all.wifi.received
                    transmittedSum += bucket.all.mobile.transmitted + bucket.all.wifi.transmitted
                }
                totalReceived.postValue(receivedSum)
                totalTransmitted.postValue(transmittedSum)
                val getTimeLineUseCase = GetTimeLineUseCase(interval, period)
                val timeLine = getTimeLineUseCase.getTimeLine()
                networkInfo.postValue(
                    NetworkInfo(
                        myPackageInfo,
                        bucketList,
                        timeLine
                    )
                )
            }
        }
    }
}