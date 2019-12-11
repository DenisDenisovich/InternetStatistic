package aero.testcompany.internetstat.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [NetworkEntity::class, ApplicationEntity::class], version = 1)
abstract class NetworkDatabase: RoomDatabase() {
    abstract fun applicationDao(): ApplicationDao

    abstract fun networkDao(): NetworkDao
}