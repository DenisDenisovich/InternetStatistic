package aero.testcompany.internetstat.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "NetworkEntity")
class NetworkEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    val time: Long,
    val data: String
) {
    constructor(): this(0, 0 ,"")
}
