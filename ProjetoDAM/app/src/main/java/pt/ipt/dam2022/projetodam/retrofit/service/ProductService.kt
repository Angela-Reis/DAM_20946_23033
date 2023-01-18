package pt.ipt.dam2022.projetodam.retrofit.service

import pt.ipt.dam2022.projetodam.model.Product
import pt.ipt.dam2022.projetodam.model.StorePrice
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

    /*
     * get a specific Store Information
     */
    @GET("/stores/{store}/name.json")
    fun getStoreName(@Path("store") storeKey: String): Call<String>

    /*
     * get a product Price from a Specific Store
     */
    @GET("/stores/{store}/productsPrices/{product}.json")
    fun getProductPriceFromStore(
        @Path("store") storeKey: String, @Path("product") productKey: String
    ): Call<StorePrice>


}


