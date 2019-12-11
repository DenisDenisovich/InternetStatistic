package aero.testcompany.internetstat.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ApplicationDao {

    @Query("SELECT * FROM ApplicationEntity")
    fun getAll(): List<ApplicationEntity>

    @Query("SELECT * FROM ApplicationEntity WHERE name = :name")
    fun getIdByName(name: String): Long

    @Insert
    fun addApplication(app: ApplicationEntity)

    @Delete
    fun deleteApplication(app: ApplicationEntity)
}