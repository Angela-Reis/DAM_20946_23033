package pt.ipt.dam2022.projetodam.model.auth

import com.google.gson.annotations.SerializedName

data class LoginUserResponse(
    @SerializedName("idToken") val idToken: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("expiresIn") val expiresIn: String?,
    @SerializedName("localId") val localId: String?,
    @SerializedName("registered") val registered: String?,
)
