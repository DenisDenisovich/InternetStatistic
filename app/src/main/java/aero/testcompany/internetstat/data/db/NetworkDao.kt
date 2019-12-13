package aero.testcompany.internetstat.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NetworkDao {

    @Query("SELECT * FROM NetworkEntity")
    fun getAll(): List<NetworkEntity>

    @Query("SELECT * FROM NetworkEntity WHERE time >= :start and time <= :end ORDER by time DESC")
    fun getByInterval(start: Long, end: Long): List<NetworkEntity>

    @Insert
    fun addNetworkEntity(dao: NetworkEntity)
}