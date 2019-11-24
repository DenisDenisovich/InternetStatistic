package aero.testcompany.internetstat.models

import android.net.ConnectivityManager

enum class NetworkSource(val value: Int) {
    ALL(-100500),
    MOBILE(ConnectivityManager.TYPE_MOBILE),
    WIFI(ConnectivityManager.TYPE_WIFI)
}