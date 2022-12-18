package pt.ipt.dam2022.projetodam.model

import com.google.gson.annotations.SerializedName

/*
 * Class that represents product received from API
 */
data class Product(
    @SerializedName("category") val category: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("price") val price: Double?
)
