package pt.ipt.dam2022.projetodam.model.auth

import com.google.gson.annotations.SerializedName

/**
 * Class that represents response received from the AuthService call changePassword
 */
data class ChangePassResponse(
    @SerializedName("localId") val localId: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("passwordHash") val passwordHash: String?,
    @SerializedName("idToken") val idToken: String?,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("expiresIn") val expiresIn: String?,
)
