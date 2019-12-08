package aero.testcompany.internetstat.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NetworkEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val time: Long,
    val data: String
)