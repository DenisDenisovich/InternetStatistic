package aero.testcompany.internetstat.data.api.dto

data class UserApps(
    val apps: List<App>,
    val user: String
)