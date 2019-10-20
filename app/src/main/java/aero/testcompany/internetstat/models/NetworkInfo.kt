package aero.testcompany.internetstat.models

import java.io.Serializable

data class NetworkInfo(
    val packageName: String,
    val label: String,
    val received: List<Long>,
    val transmitted: List<Long>,
    val interval: Long,
    val step: Long
): Serializable