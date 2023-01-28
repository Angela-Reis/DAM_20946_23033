package pt.ipt.dam2022.projetodam.ui.activity.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import org.json.JSONObject
import pt.ipt.dam2022.projetodam.R
import pt.ipt.dam2022.projetodam.model.auth.LoginUserResponse
import pt.ipt.dam2022.projetodam.model.auth.SignUpResponse
import pt.ipt.dam2022.projetodam.retrofit.RetrofitAuthInit
import pt.ipt.dam2022.projetodam.ui.activity.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        // get reference to button
        val btnRegister = findViewById<Button>(R.id.buttonRegister)
        // set on-click listener
        btnRegister.setOnClickListener {
            this.startActivity(Intent(this, SignUpActivity::class.java))
        }

        // get reference to button
        val btnLogin = findViewById<Button>(R.id.buttonLogin)

        // set on-click listener
        btnLogin.setOnClickListener {
            loginUser("teste999@ipt.pt", "teste1234!")
        }
    }

    /**
     * access api with the call specified in registerUser
     */
    private fun loginUser(email: String, password: String) {
        val call = RetrofitAuthInit().authService().loginUser(email, password, true)
        processRegister(call)
    }


    /**
     * add the User to sharedPreference to keep token when the app is shut down
     */
    private fun processRegister(call: Call<LoginUserResponse>) {
        // use data read
        call.enqueue(object : Callback<LoginUserResponse> {
            override fun onResponse(
                call: Call<LoginUserResponse>,
                response: Response<LoginUserResponse>
            ) {

                if (response.isSuccessful) {
                    response.body()?.let {
                        val response: LoginUserResponse = it
                        // takes the data read from API and saves it
                        val sharedPreference = getSharedPreferences("USER", MODE_PRIVATE)
                        val editor = sharedPreference.edit()
                        editor.putString("emailUser", response.email)
                        editor.putString("refreshTokenUser", response.refreshToken)
                        editor.putString("idTokenUser", response.idToken)
                        editor.apply()
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    try {
                        Toast.makeText(applicationContext, "Ocorreu um erro a fazer login", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<LoginUserResponse>, t: Throwable) {
                t.message?.let { Log.e("Something went wrong ", it) }
                Toast.makeText(applicationContext, "Aconteceu um erro a fazer login", Toast.LENGTH_LONG).show()
            }
        })
    }
}