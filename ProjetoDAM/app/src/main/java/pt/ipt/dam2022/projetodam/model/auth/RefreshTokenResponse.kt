package pt.ipt.dam2022.projetodam.model.auth

import com.google.gson.annotations.SerializedName

/**
 * Class that represents response received from the AuthService call changePassword
 */
data class RefreshTokenResponse(
    @SerializedName("expires_in") val expires_in: String?,
    @SerializedName("token_type") val token_type: String?,
    @SerializedName("refresh_token") val refresh_token: String?,
    @SerializedName("id_token") val id_token: String?,
    @SerializedName("user_id") val user_id: String?,
    @SerializedName("project_id") val project_id: String?,
)
