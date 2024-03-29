package pt.ipt.dam2022.projetodam.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import pt.ipt.dam2022.projetodam.R
import pt.ipt.dam2022.projetodam.model.auth.ChangePassResponse
import pt.ipt.dam2022.projetodam.model.auth.LoginUserResponse
import pt.ipt.dam2022.projetodam.retrofit.RetrofitAuthInit
import pt.ipt.dam2022.projetodam.ui.activity.MainActivity
import pt.ipt.dam2022.projetodam.ui.activity.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * Fragment for user to change password
 */
class ChangePassFragment : Fragment() {
    private lateinit var idToken: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_pass, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val btnSeeOld = view.findViewById<ImageButton>(R.id.btnSeeOldPass)
        val oldPasswordTxt = view.findViewById<EditText>(R.id.txtOldPassword)
        val btnSeePass = view.findViewById<ImageButton>(R.id.btnSeePass)
        val passwordTxt = view.findViewById<EditText>(R.id.txtPassword)
        val btnSeePassRep = view.findViewById<ImageButton>(R.id.btnSeeRepPass)
        val passwordTxtRep = view.findViewById<EditText>(R.id.txtRepeatPassword)


        //get idToken of currentUser
        val sharedPreference =
            activity?.getSharedPreferences("USER", AppCompatActivity.MODE_PRIVATE)
        if (sharedPreference != null) {
            idToken = sharedPreference.getString("refreshTokenUser", null).toString()
        }


        //function that sets onTouchListener in order to show password
        seePassword(passwordTxt, btnSeePass)
        seePassword(passwordTxtRep, btnSeePassRep)
        seePassword(oldPasswordTxt, btnSeeOld)


        val buttonChangePass = view.findViewById<Button>(R.id.buttonChangePass)
        buttonChangePass.setOnClickListener {
            //Close keyboard
            val inputMng: InputMethodManager =
                context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMng.hideSoftInputFromWindow(view.windowToken, 0)


            val newPass = passwordTxt.text.toString()
            if (!isValidPassword(passwordTxt.text.toString())) {
                passwordTxt.error = getString(R.string.password_error)
            } else if (newPass != passwordTxtRep.text.toString()) {
                passwordTxtRep.error = getString(R.string.password_error_equal)
            } else {
                if (sharedPreference != null) {
                    //it first test if the user put the right password
                    testPassword(
                        sharedPreference.getString("emailUser", null).toString(),
                        oldPasswordTxt.text.toString(),
                        newPass
                    )
                } else {
                    //if shared preferences it's null redirect to login screen
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }
            }
        }


        super.onViewCreated(view, savedInstanceState)
    }


    /**
     * Check if password is correct, by sending login Request to the Firebase Auth REST API
     * if it's correct send request to api to change password
     */
    private fun testPassword(email: String, password: String, newPassword: String) {
        val call = RetrofitAuthInit().authService().loginUser(email, password, true)

        // use data read
        call.enqueue(object : Callback<LoginUserResponse> {
            override fun onResponse(
                call: Call<LoginUserResponse>,
                response: Response<LoginUserResponse>
            ) {
                //If response is Successful this means the password is correct
                if (response.isSuccessful) {
                    response.body()?.let {
                        val responseReceived: LoginUserResponse = it
                        // takes the data read from API and saves it
                        val sharedPreference = requireContext().getSharedPreferences(
                            "USER",
                            AppCompatActivity.MODE_PRIVATE
                        )
                        //Save the refreshToken and idToken generated
                        val editor = sharedPreference.edit()
                        editor.putString("refreshTokenUser", responseReceived.refreshToken)
                        editor.putString("idTokenUser", responseReceived.idToken)
                        editor.apply()
                        idToken = responseReceived.idToken.toString()
                        //Since the old password it's correct call the api to change the password
                        changePassword(newPassword)
                    }
                } else {
                    try {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_message),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<LoginUserResponse>, t: Throwable) {
                t.message?.let { Log.e("Something went wrong ", it) }
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_message),
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }


    /**
     * access api with the call specified in changePass
     */
    private fun changePassword(password: String) {
        //call to exchange refresh token with a ID token
        val call =
            RetrofitAuthInit().authService().changePassword(idToken, password, true)
        processChangePass(call)
    }

    /**
     * Process call to change password
     */
    private fun processChangePass(call: Call<ChangePassResponse>) {
        call.enqueue(object : Callback<ChangePassResponse> {
            override fun onResponse(
                call: Call<ChangePassResponse>,
                response: Response<ChangePassResponse>
            ) {

                if (response.isSuccessful) {
                    response.body()?.let {
                        val result: ChangePassResponse = it
                        // takes the data read from API and saves it
                        val sharedPreference = requireContext().getSharedPreferences(
                            "USER",
                            AppCompatActivity.MODE_PRIVATE
                        )
                        //if password change is sucessful, save the new idToken and refreshToken
                        val editor = sharedPreference.edit()
                        editor.putString("refreshTokenUser", result.refreshToken)
                        editor.putString("idTokenUser", result.idToken)
                        editor.apply()
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.password_changed),
                            Toast.LENGTH_LONG
                        )
                            .show()
                        //Redirect to mainActivity
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        startActivity(intent)
                        activity?.finish()
                    }
                } else {
                    try {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_message),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<ChangePassResponse>, t: Throwable) {
                t.message?.let { Log.e("Something went wrong ", it) }
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_message),
                    Toast.LENGTH_LONG
                ).show()
            }
        })

    }

    /**
     * Verify if password is valid, longer than 6 and with an Upper Case letter
     */
    private fun isValidPassword(password: String): Boolean {
        if (password.length < 6) return false
        if (password.firstOrNull { it.isDigit() } == null) return false
        if (password.filter { it.isLetter() }.firstOrNull { it.isUpperCase() } == null) return false
        return true
    }

    /**
     * Show password in textField when Image Button is pressed and hides password when button is released
     */
    @SuppressLint("ClickableViewAccessibility")
    fun seePassword(textField: EditText, btn: ImageButton) {

        btn.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> textField.transformationMethod = null
                MotionEvent.ACTION_UP -> textField.transformationMethod =
                    PasswordTransformationMethod()
            }
            v?.onTouchEvent(event) ?: true
        }
    }
}