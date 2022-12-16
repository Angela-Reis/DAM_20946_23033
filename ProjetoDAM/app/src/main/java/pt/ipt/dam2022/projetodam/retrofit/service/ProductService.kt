package pt.ipt.dam2022.projetodam.retrofit.service

import pt.ipt.dam2022.projetodam.model.Product
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Specify the specific 'url' path to access
 */
interface ProductService {

    /*
     * get a list with all Products
     */
    @GET("/products.json")
    fun listAllProducts(): Call<Map<String, Product>>

}


