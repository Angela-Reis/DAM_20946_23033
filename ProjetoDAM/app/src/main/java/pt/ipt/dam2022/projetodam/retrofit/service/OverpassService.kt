package pt.ipt.dam2022.projetodam.retrofit.service

import pt.ipt.dam2022.projetodam.model.overpass.OverpassResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Define an interface named OverpassService to represent an API service
 * Specify the specific 'url' path to access the data
 */
interface OverpassService {
    @GET("/api/interpreter")
    fun queryOverpass(
        @Query("data") query: String
    ): Call<OverpassResponse>
}