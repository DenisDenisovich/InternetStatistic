package aero.testcompany.internetstat.data.api.dto

data class NetworkData(
    val user: String,
    val timestamp: Long,
    val period: ApiNetworkPeriod,
    val data: String
)

enum class ApiNetworkPeriod {
    MINUTES,
    HOUR
}