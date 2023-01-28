package pt.ipt.dam2022.projetodam.retrofit.service

import pt.ipt.dam2022.projetodam.model.Product
import pt.ipt.dam2022.projetodam.model.StorePrice
import retrofit2.Call
import retrofit2.http.*

/**
 * Specify the specific 'url' path to access
 *  all the queries need an auth which is the idToken of the authenticated user
 */
interface ProductService {

    /*
     * get a list with all Products
     */
    @GET("/products.json")
    fun listAllProducts(@Query("auth") authToken : String): Call<Map<String, Product>>


    /*
     * get a specific Store Information
     */
    @GET("/stores/{store}/name.json")
    fun getStoreName(@Path("store") storeKey: String, @Query("auth") authToken : String): Call<String>

    /*
     * get a product Price from a Specific Store
     */
    @GET("/stores/{store}/productsPrices/{product}.json")
    fun getProductPriceFromStore(
        @Path("store") storeKey: String, @Path("product") productKey: String, @Query("auth") authToken : String
    ): Call<StorePrice>

}


