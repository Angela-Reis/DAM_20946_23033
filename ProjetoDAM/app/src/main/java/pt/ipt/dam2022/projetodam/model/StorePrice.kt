package pt.ipt.dam2022.projetodam.model

import com.google.gson.annotations.SerializedName

data class StorePrice(
    @SerializedName("date") val date: String?,
    @SerializedName("price") val price: Double?,
    var storeName: String?,
    var storeKey: String?,
)