package pt.ipt.dam2022.projetodam.model

import com.google.gson.annotations.SerializedName

/*
<<<<<<< HEAD
 * Class that represents the products received from the API
 */
import java.io.Serializable

/**
 * Class that represents product received from API
 */
data class Store(
    @SerializedName("name") val name: String?,
) : Serializable

