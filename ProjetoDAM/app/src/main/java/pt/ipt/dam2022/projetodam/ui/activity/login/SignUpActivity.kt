package pt.ipt.dam2022.projetodam.ui.activity.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.PatternsCompat
import org.json.JSONObject
import pt.ipt.dam2022.projetodam.R
import pt.ipt.dam2022.projetodam.model.auth.SignUpResponse
import pt.ipt.dam2022.projetodam.retrofit.RetrofitAuthInit
import pt.ipt.dam2022.projetodam.ui.activity.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)


        // get reference to button
        val btnRegister = findViewById<Button>(R.id.buttonSignUp)
        val emailTxt = findViewById<EditText>(R.id.txtEmail)
        val passwordTxt = findViewById<EditText>(R.id.txtPassword)
        val repeatPasswordTxt = findViewById<EditText>(R.id.txtRepeatPassword)

        // set on-click listener
        btnRegister.setOnClickListener {
            val email = emailTxt.text.toString()
            val pass = passwordTxt.text.toString()
            if(!isValidPassword(passwordTxt.text.toString())){
                passwordTxt.error = "Password tem de ter 6 caracteres e 1 maiúsculo"
            }
            else if (pass != repeatPasswordTxt.text.toString()) {
                repeatPasswordTxt.error = "Passwords tem de ser iguais"
            }else if(!checkForEmail(email)){
                emailTxt.error = "Coloque um email válido"
            }else{
                signUpUser(email, pass)
            }
        }
    }

    private fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        if (password.filter { it.isDigit() }.firstOrNull() == null) return false
        if (password.filter { it.isLetter() }.filter { it.isUpperCase() }.firstOrNull() == null) return false
        return true
    }

    private fun checkForEmail(email: String): Boolean {
        if (PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            return true
        }
        return false
    }
    /**
     * access api with the call specified in signUpUser
     */
    private fun signUpUser(email: String, password: String) {
        val call = RetrofitAuthInit().authService().signupNewUser(email, password, true)
        processSignUp(call)
    }


    /**
     * add the User to sharedPreference to keep token when the app is shut down
     */
    private fun processSignUp(call: Call<SignUpResponse>) {
        // use data read
        call.enqueue(object : Callback<SignUpResponse> {
            override fun onResponse(
                call: Call<SignUpResponse>,
                response: Response<SignUpResponse>
            ) {

                if (response.isSuccessful) {
                    response.body()?.let {
                        val response: SignUpResponse = it
                        // takes the data read from API and saves it
                        val sharedPreference = getSharedPreferences("USER", MODE_PRIVATE)
                        val editor = sharedPreference.edit()
                        editor.putString("emailUser", response.email)
                        editor.putString("refreshTokenUser", response.refreshToken)
                        editor.putString("idTokenUser", response.id_token)
                        editor.apply()
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        Toast.makeText(
                            applicationContext,
                            jObjError.getJSONObject("error").getString("message"),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                t.message?.let { Log.e("Something went wrong ", it) }
                Toast.makeText(applicationContext, "Aconteceu um erro", Toast.LENGTH_LONG).show()
            }
        })
    }
}