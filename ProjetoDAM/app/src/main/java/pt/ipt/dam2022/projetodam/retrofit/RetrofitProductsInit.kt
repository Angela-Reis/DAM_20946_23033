package pt.ipt.dam2022.projetodam.retrofit

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import pt.ipt.dam2022.projetodam.retrofit.service.ProductService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


/**
 * access to API
 */
class RetrofitProductsInit(private var context: Context) {


    //'url' to access the REST API
    private val host = "https://projetodam-20946-23033-default-rtdb.europe-west1.firebasedatabase.app/"
    // create Gson to interpret Json
    private val gson:Gson = GsonBuilder().setLenient().create()

    var client: OkHttpClient = OkHttpClient().newBuilder()
        .authenticator(RefreshAuth(context))
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(host)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .client(client)
        .build()

    fun productService(): ProductService = retrofit.create(ProductService::class.java)

}