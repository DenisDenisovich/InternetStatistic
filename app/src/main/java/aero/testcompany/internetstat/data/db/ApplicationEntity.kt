package aero.testcompany.internetstat.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ApplicationEntity")
class ApplicationEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    val name: String
) {
    constructor(): this(0, "")
}
