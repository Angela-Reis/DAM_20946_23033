package pt.ipt.dam2022.projetodam.ui.activity.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.PatternsCompat
import org.json.JSONObject
import pt.ipt.dam2022.projetodam.LanguageUtil
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

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get reference to button
        val btnSignUp = findViewById<Button>(R.id.buttonSignUp)
        val emailTxt = findViewById<EditText>(R.id.txtEmail)
        val passwordTxt = findViewById<EditText>(R.id.txtPassword)
        val repeatPasswordTxt = findViewById<EditText>(R.id.txtRepeatPassword)
        val btnVerPass = findViewById<ImageButton>(R.id.btnSeePass)
        val btnVerRepPass = findViewById<ImageButton>(R.id.btnSeeRepPass)
        seePassword(passwordTxt, btnVerPass)
        seePassword(repeatPasswordTxt, btnVerRepPass)

        // set on-click listener
        btnSignUp.setOnClickListener {
            val email = emailTxt.text.toString()
            val pass = passwordTxt.text.toString()
            if(!isValidPassword(passwordTxt.text.toString())){
                passwordTxt.error = getString(R.string.password_error)
            }
            else if (pass != repeatPasswordTxt.text.toString()) {
                repeatPasswordTxt.error = getString(R.string.password_error_equal)
            }else if(!checkForEmail(email)){
                emailTxt.error = getString(R.string.email_error)
            }else{
                signUpUser(email, pass)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            finish()
        }
        return false
    }

    /**
     * Verify if password is valid, longer than 6 and with an Upper Case letter
     */
    private fun isValidPassword(password: String): Boolean {
        if (password.length < 6) return false
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
                Toast.makeText(applicationContext, getString(R.string.error_message), Toast.LENGTH_LONG).show()
            }
        })
    }


    /**
     * Show password in textField when Image Button is pressed and hides password when button is released
     */
    @SuppressLint("ClickableViewAccessibility")
    fun seePassword(textField: EditText, btn:ImageButton) {

        btn.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> textField.transformationMethod = null
                MotionEvent.ACTION_UP -> textField.transformationMethod = PasswordTransformationMethod()
            }

            v?.onTouchEvent(event) ?: true
        }
    }


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageUtil.updateConfigLang(newBase))
    }
}