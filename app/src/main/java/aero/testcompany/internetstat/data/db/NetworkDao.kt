package aero.testcompany.internetstat.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NetworkDao {

    @Query("SELECT * FROM networkentity")
    suspend fun getAll(): List<NetworkDao>

    @Query("SELECT * FROM networkentity WHERE time >= :start and time < :end")
    suspend fun getByInterval(start: Long, end: Long)

    @Insert
    suspend fun addNetworkEntity(dao: NetworkEntity)
}