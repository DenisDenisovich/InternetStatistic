package aero.testcompany.internetstat.view

import aero.testcompany.internetstat.data.db.NetworkDatabase
import android.app.Application
import androidx.room.Room

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(
            applicationContext,
            NetworkDatabase::class.java, "networkDatabase"
        ).build()
    }

    companion object {
        lateinit var db: NetworkDatabase
    }
}