package pt.ipt.dam2022.projetodam.retrofit.service

import pt.ipt.dam2022.projetodam.model.Product
import pt.ipt.dam2022.projetodam.model.Store
import pt.ipt.dam2022.projetodam.model.StorePrice
import retrofit2.Call
import retrofit2.http.*

/**
 * Specify the specific 'url' path to access
 *  all the queries need an authToken at the end
 *  this token is the idToken of the authenticated user
 */
interface ProductService {

    /*
     * get a list with all Products
     */
    @GET("/product.json")
    fun listAllProducts(@Query("auth") authToken : String): Call<Map<String, Product>>


    @GET("/store/{store}.json")
    fun getStore(@Path("store") storeKey: String, @Query("auth") authToken : String): Call<Store>

    /*
     * get a list with all Stores
     */
    @GET("/store.json")
    fun listAllStores(@Query("auth") authToken : String): Call<Map<String, Store>>


    /*
     * get a product Price from a Specific Store
     */
    @GET("/store_products/{store}/{product}.json")
    fun getProductPriceFromStore(
        @Path("store") storeKey: String, @Path("product") productKey: String, @Query("auth") authToken : String
    ): Call<StorePrice>

}


