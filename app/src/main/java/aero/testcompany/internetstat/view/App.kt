package aero.testcompany.internetstat.view

import aero.testcompany.internetstat.data.api.Api
import aero.testcompany.internetstat.data.db.NetworkDatabase
import android.app.Application
import androidx.room.Room
import retrofit2.Retrofit
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T



class App : Application() {

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(
            applicationContext,
            NetworkDatabase::class.java, "networkDatabase"
        ).build()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://vdenisov.shefer.space")
            .build()

        api = retrofit.create<Api>(Api::class.java)
    }

    companion object {
        lateinit var db: NetworkDatabase
        lateinit var api: Api
    }
}