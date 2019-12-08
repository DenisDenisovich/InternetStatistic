package aero.testcompany.internetstat.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ApplicationEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val name: String
)