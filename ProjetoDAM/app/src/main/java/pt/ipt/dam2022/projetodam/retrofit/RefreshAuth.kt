package pt.ipt.dam2022.projetodam.retrofit

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import pt.ipt.dam2022.projetodam.model.auth.RefreshTokenResponse


/*
 * If an retrofit request receives a 401, unauthorized
 * it triggers this class that asks the server for an new token
 * to use in the queries, in the auth
 */
class RefreshAuth(private var context: Context) : Authenticator {

    override fun authenticate(route: Route, response: Response): Request? {
        val sharedPreference = context.getSharedPreferences("USER", AppCompatActivity.MODE_PRIVATE)
        val request = response.request()


        val refresh: String = sharedPreference.getString("refreshTokenUser", null)
            ?: return response.request()

        //exchange refresh token with a ID token
        val newTokens: RefreshTokenResponse? = RetrofitAuthInit()
            .authService()
            .refreshToken(refresh)
            .execute()
            .body()
        //add new tokens to sharedPreference so it persists when app is shut off
        if (newTokens != null) {
            val editor = sharedPreference.edit()
            editor.putString("refreshTokenUser", newTokens.refresh_token)
            editor.putString("idTokenUser", newTokens.id_token)
            editor.apply()

            //edit the request with the new token
            val newUrl: String? =
                newTokens.id_token?.let { request.url().toString().replaceAfter("auth=", it) }


            return newUrl?.let {
                request.newBuilder()
                    .url(it)
                    .build()
            }
        }
        return request.newBuilder().build()
    }
}