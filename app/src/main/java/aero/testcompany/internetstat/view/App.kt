package aero.testcompany.internetstat.view

import aero.testcompany.internetstat.data.api.Api
import aero.testcompany.internetstat.data.db.NetworkDatabase
import android.app.Application
import androidx.room.Room
import retrofit2.Retrofit
import com.google.gson.GsonBuilder
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


class App : Application() {

    override fun onCreate() {
        super.onCreate()

        instance = this

        db = Room.databaseBuilder(
            applicationContext,
            NetworkDatabase::class.java, "networkDatabase"
        ).build()

        val gson = GsonBuilder()
            .setLenient()
            .create()
        val httpClient = OkHttpClient.Builder()
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        httpClient.interceptors().add(interceptor)
        api = Retrofit.Builder()
            .baseUrl("http://vdenisov.shefer.space/")
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(Api::class.java)
    }

    companion object {
        lateinit var db: NetworkDatabase
        lateinit var api: Api
        lateinit var instance: App
    }
}