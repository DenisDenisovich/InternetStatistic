package aero.testcompany.internetstat.data.api

import aero.testcompany.internetstat.data.api.dto.UserApps
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Query

interface Api {

    @PUT("/user")
    suspend fun addUser(@Query("name")username: String): String

    @PUT("/apps")
    suspend fun addApps(@Body userApps: UserApps): String

    @PUT("/network")
    suspend fun getNetwork()
}