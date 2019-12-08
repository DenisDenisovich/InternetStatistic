package aero.testcompany.internetstat.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ApplicationDao {

    @Query("SELECT * FROM applicationentity")
    suspend fun getAll(): List<ApplicationDao>

    @Query("SELECT * FROM applicationentity WHERE name = :name")
    suspend fun getIdByName(name: String): Long


    @Insert
    suspend fun addApplication(app: ApplicationDao)

    @Delete
    suspend fun deleteApplication(app: ApplicationDao)
}