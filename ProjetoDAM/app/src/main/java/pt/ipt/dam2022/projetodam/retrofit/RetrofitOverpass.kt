package pt.ipt.dam2022.projetodam.retrofit

import android.content.Context
import pt.ipt.dam2022.projetodam.retrofit.service.OverpassService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Access to Overpass API
 * API Documentation can be found in
 * https://wiki.openstreetmap.org/wiki/Overpass_API
 */
class RetrofitOverpass(context: Context) {

    //'url' to access the REST API
    private val host = "https://overpass-api.de"

    private val retrofit = Retrofit.Builder()
        .baseUrl(host)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun overpassService(): OverpassService = retrofit.create(OverpassService::class.java)

}