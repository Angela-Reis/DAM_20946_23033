package pt.ipt.dam2022.projetodam.retrofit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import pt.ipt.dam2022.projetodam.retrofit.service.AuthService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * access Firebase Auth REST API
 * API Documentation can be found in
 * https://firebase.google.com/docs/reference/rest/auth
 */
class RetrofitAuthInit {
    //'url' to access the REST API of the Firebase Auth REST API
    private val host = "https://identitytoolkit.googleapis.com/v1/"

    // create Gson to interpret Json
    private val gson: Gson = GsonBuilder().setLenient().create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(host)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    fun authService(): AuthService = retrofit.create(AuthService::class.java)
}