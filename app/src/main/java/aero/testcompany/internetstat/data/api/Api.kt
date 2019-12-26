package aero.testcompany.internetstat.data.api

import aero.testcompany.internetstat.data.api.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface Api {

    @PUT("/user")
    suspend fun addUser(@Body user: User): AddUserResponse

    @PUT("/apps")
    suspend fun addApps(@Body userApps: UserApps): SuccessResponse

    @GET("/networkdata/last")
    suspend fun getNetworkDataLastIndex(
        @Query("name") userName: String,
        @Query("period") period: String
    ): GetLastNetworkResponse

    @PUT("/networkdata")
    suspend fun addNetworkData(@Body data: ArrayList<NetworkData>): SuccessResponse
}