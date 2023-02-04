package pt.ipt.dam2022.projetodam.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
* Class that represents product received from API
*/
data class Product (
    @SerializedName("category") val category: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("image") var image: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("stores") val stores: Map<String, Boolean>?
): Serializable


