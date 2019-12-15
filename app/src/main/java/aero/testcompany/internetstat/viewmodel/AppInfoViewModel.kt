package aero.testcompany.internetstat.viewmodel

import aero.testcompany.internetstat.domain.packageinfo.GetPackageUidUseCase
import aero.testcompany.internetstat.domain.network.GetPackageNetworkUseCase
import aero.testcompany.internetstat.domain.network.minutes.GetPackageNetworkMinutesUseCase
import aero.testcompany.internetstat.models.MyPackageInfo
import aero.testcompany.internetstat.models.NetworkInterval
import aero.testcompany.internetstat.models.NetworkPeriod
import aero.testcompany.internetstat.models.bucket.BucketInfo
import android.app.usage.NetworkStatsManager
import android.content.Context
import androidx.lifecycle.*
import kotlinx.coroutines.*

class AppInfoViewModel : ViewModel() {

    var receivedSum: Long = 0
    var transmittedSum: Long = 0
    val timeLine: MutableLiveData<List<Long>> by lazy { MutableLiveData<List<Long>>() }
    val networkInfo: MutableLiveData<List<BucketInfo>> by lazy { MutableLiveData<List<BucketInfo>>() }
    val totalReceived: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }
    val totalTransmitted: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }

    var buckets: MutableLiveData<List<BucketInfo>>? = null
    var bucketsObserver = Observer<List<BucketInfo>> { bucketList ->
        // update current sum
        bucketList.forEach { bucket ->
            receivedSum += bucket.all.mobile.received + bucket.all.wifi.received
            transmittedSum += bucket.all.mobile.transmitted + bucket.all.wifi.transmitted
        }
        totalReceived.value = receivedSum
        totalTransmitted.value = transmittedSum
        // send new part of network data
        networkInfo.value = bucketList
    }
    private var currentNetworkJob: Job? = null

    private lateinit var networkStatsManager: NetworkStatsManager
    private lateinit var getPackageUidUseCase: GetPackageUidUseCase
    private var currentPackageNetworkUseCase: GetPackageNetworkUseCase? = null
    private lateinit var packageNetworkUseCase: GetPackageNetworkUseCase
    private lateinit var packageNetworkMinutesUseCase: GetPackageNetworkMinutesUseCase
    private var networkPeriod: NetworkPeriod = NetworkPeriod.MONTH

    fun initData(context: Context, myPackageInfo: MyPackageInfo) {
        networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        getPackageUidUseCase = GetPackageUidUseCase(context)
        packageNetworkUseCase = GetPackageNetworkUseCase(
            getPackageUidUseCase.getUid(myPackageInfo.packageName),
            context,
            networkStatsManager
        )
        packageNetworkMinutesUseCase = GetPackageNetworkMinutesUseCase(
            myPackageInfo.packageName,
            getPackageUidUseCase.getUid(myPackageInfo.packageName),
            context,
            networkStatsManager
        )
    }

    fun update(interval: NetworkInterval, period: NetworkPeriod) {
        networkPeriod = period
        currentPackageNetworkUseCase?.stop()
        currentPackageNetworkUseCase = if (networkPeriod == NetworkPeriod.MINUTES) {
            packageNetworkMinutesUseCase
        } else {
            packageNetworkUseCase
        }
        currentPackageNetworkUseCase?.let { useCase ->
            currentNetworkJob = viewModelScope.launch {
                receivedSum = 0
                transmittedSum = 0
                buckets = useCase.setup(
                    interval.getInterval(),
                    period,
                    this@launch + Dispatchers.Default
                )
                timeLine.value = useCase.timeLine
                buckets?.observeForever(bucketsObserver)
                useCase.start()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        buckets?.removeObserver(bucketsObserver)
    }
}