package pt.ipt.dam2022.projetodam.retrofit.service

import pt.ipt.dam2022.projetodam.model.auth.ChangePassResponse
import pt.ipt.dam2022.projetodam.model.auth.RefreshTokenResponse
import pt.ipt.dam2022.projetodam.model.auth.LoginUserResponse
import pt.ipt.dam2022.projetodam.model.auth.SignUpResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Specify the specific 'url' path to access
 * all the call to the url need the api key of the firebase project
 * which in this case is "AIzaSyBcGnZpZRKNtzHLLHgfqj1jMns8G2x_uPQ"
 */
interface AuthService {

    //https://firebase.google.com/docs/reference/rest/auth
    /*
     * signup a new user to the app
     */
    @FormUrlEncoded
    @POST("./accounts:signUp?key=AIzaSyBcGnZpZRKNtzHLLHgfqj1jMns8G2x_uPQ")
    fun signupNewUser(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("returnSecureToken") returnSecure: Boolean
    ): Call<SignUpResponse>

    @FormUrlEncoded
    @POST("./accounts:signInWithPassword?key=AIzaSyBcGnZpZRKNtzHLLHgfqj1jMns8G2x_uPQ")
    fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("returnSecureToken") returnSecure: Boolean
    ): Call<LoginUserResponse>

    @FormUrlEncoded
    @POST("./token?key=AIzaSyBcGnZpZRKNtzHLLHgfqj1jMns8G2x_uPQ&grant_type=refresh_token")
    fun refreshToken(
        @Field("refresh_token") refreshToken: String?,
    ): Call<RefreshTokenResponse>


    @FormUrlEncoded
    @POST("./accounts:update?key=AIzaSyBcGnZpZRKNtzHLLHgfqj1jMns8G2x_uPQ&grant_type=refresh_token")
    fun changePassword(
        @Field("idToken") idToken: String?,
        @Field("password") password: String?,
        @Field("returnSecureToken") returnSecureToken: Boolean?,
    ): Call<ChangePassResponse>


}


