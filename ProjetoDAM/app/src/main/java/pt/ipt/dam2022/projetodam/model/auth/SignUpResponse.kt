package pt.ipt.dam2022.projetodam.model.auth

import com.google.gson.annotations.SerializedName

/**
 * Class that represents response received from the AuthService call refreshToken
 */
data class SignUpResponse(
    @SerializedName("idToken") val id_token: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("expiresIn") val expiresIn: String?,
    @SerializedName("localId") val localId: String?,
)