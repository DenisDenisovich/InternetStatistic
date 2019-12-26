package aero.testcompany.internetstat.domain.network.api

import aero.testcompany.internetstat.data.api.dto.*
import aero.testcompany.internetstat.domain.packageinfo.GetPackagesUseCase
import aero.testcompany.internetstat.models.MyPackageInfo
import aero.testcompany.internetstat.models.NetworkPeriod
import aero.testcompany.internetstat.util.isNetworkConnected
import aero.testcompany.internetstat.view.App
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.google.gson.Gson
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.lang.Exception

class SyncNetworkDataWorker(val context: Context) {
    private val api = App.api
    private val gson = Gson()

    @SuppressLint("HardwareIds")
    private val userId =
        Settings.Secure.getString(App.instance.contentResolver, Settings.Secure.ANDROID_ID)
    private var packages = arrayListOf<MyPackageInfo>()
    private var networkDataCalculator: ApiNetworkDataCalculator? = null
    private val packagesList = GetPackagesUseCase(context.packageManager)
    private var job: Job = Job()
    private var scope: CoroutineScope = CoroutineScope(Dispatchers.Default + job)

    fun start() {
        scope.launch {
            sendUserId()
            sendPackages()
            syncData()
        }
    }

    fun startBlocking() {
        runBlocking {
            sendUserId()
            sendPackages()
            syncData()
        }
    }

    private suspend fun sendUserId() {

        try {
            val info = "${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}"
            val result = api.addUser(User(userId, info))
        } catch (e: Exception) {
            if (e is HttpException) {
                val error = gson.fromJson(
                    e.response()?.errorBody()?.string(),
                    ErrorResponse::class.java
                )
                // TODO send metric there
            }
            e.printStackTrace()
            // No network exception
        }
    }

    private suspend fun syncData() {
        try {
            val minutesLastTime = getLastTime(ApiNetworkPeriod.MINUTES)
            val hourLastTime = getLastTime(ApiNetworkPeriod.HOUR)
            networkDataCalculator = ApiNetworkDataCalculator(
                userId,
                packages,
                context,
                scope,
                minutesLastTime,
                hourLastTime
            )
            Log.d("OnTikTockCheck", "start")
            Log.d("OnTikTockCheck", "hour")
            val lastHour = networkDataCalculator?.getData(NetworkPeriod.HOUR)
            Log.d("OnTikTockCheck", "hourSend")
            lastHour?.let {
                Log.d("OnTikTockCheck", "hourSend is not null")
                sendNetworkData(lastHour)
            }
            Log.d("OnTikTockCheck", "minutes")
            val lastMinutes = networkDataCalculator?.getData(NetworkPeriod.MINUTES)
            lastMinutes?.let {
                Log.d("OnTikTockCheck", "minutesSend is not null")
                sendNetworkData(lastMinutes)
            }
        } catch (t: Throwable) {
            if (isNetworkConnected(context)) {
                Crashlytics.logException(t)
            }
        }
    }

    private suspend fun sendNetworkData(data: ArrayList<NetworkData>) {
        try {
            api.addNetworkData(data)
        } catch (e: Exception) {
            if (e is HttpException) {
                val error = gson.fromJson(
                    e.response()?.errorBody()?.string(),
                    ErrorResponse::class.java
                )
                error
                Log.e("OnTikTockCheck", error.toString())
                // TODO send metric there
            }
            e.printStackTrace()

            // No network exception

        }
    }

    private suspend fun getLastTime(period: ApiNetworkPeriod): Long {
        return try {
            api.getNetworkDataLastIndex(userId, period.name).lasTime
        } catch (e: Exception) {
            if (e is HttpException) {
                val error = gson.fromJson(
                    e.response()?.errorBody()?.string(),
                    ErrorResponse::class.java
                )
                error
                // TODO send metric there
            }
            e.printStackTrace()
            // No network exception
            0L
        }
    }

    private suspend fun sendPackages() {
        try {
            packages = ArrayList(packagesList.getPackages())
            val packagesApi = packagesList.getPackages().map { myPackageInfo ->
                ApiApp(myPackageInfo.packageName, myPackageInfo.packageName)
            }
            api.addApps(UserApps(packagesApi, userId))
        } catch (e: Exception) {
            if (e is HttpException) {
                val error = gson.fromJson(
                    e.response()?.errorBody()?.string(),
                    ErrorResponse::class.java
                )
                error
                // TODO send metric there
            }
            e.printStackTrace()
            // No network exception
            null
        }
    }
}