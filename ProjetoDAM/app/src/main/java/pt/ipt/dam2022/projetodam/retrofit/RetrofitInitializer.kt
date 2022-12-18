package pt.ipt.dam2022.projetodam.retrofit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import pt.ipt.dam2022.projetodam.retrofit.service.ProductService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * access to API
 */
class RetrofitInitializer {
    //'url' to access the REST API
    val host = "https://projetodam-20946-23033-default-rtdb.europe-west1.firebasedatabase.app/"
    // create Gson to interpret Json
    private val gson:Gson = GsonBuilder().setLenient().create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(host)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    fun productService() = retrofit.create(ProductService::class.java)
}