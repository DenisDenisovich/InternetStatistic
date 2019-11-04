package aero.testcompany.internetstat.models

import java.io.Serializable

data class NetworkInfo(
    val packageInfo: MyPackageInfo,
    val received: List<Long>,
    val transmitted: List<Long>,
    val timeLine: List<Long>
) : Serializable