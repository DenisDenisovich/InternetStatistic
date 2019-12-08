package aero.testcompany.internetstat.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MinutesData(
    @PrimaryKey val uid: Int,
    val time: Long,
    val data: String
)