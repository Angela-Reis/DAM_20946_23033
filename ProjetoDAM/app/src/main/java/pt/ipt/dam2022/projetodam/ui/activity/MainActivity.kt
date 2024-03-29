package pt.ipt.dam2022.projetodam.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.navigation.NavigationView
import pt.ipt.dam2022.projetodam.FunctionsUtil
import pt.ipt.dam2022.projetodam.R
import pt.ipt.dam2022.projetodam.ui.activity.login.LoginActivity
import pt.ipt.dam2022.projetodam.ui.fragments.AboutAppFragment
import pt.ipt.dam2022.projetodam.ui.fragments.ChangePassFragment
import pt.ipt.dam2022.projetodam.ui.fragments.ProductsFragment


class MainActivity : AppCompatActivity() {
    private lateinit var toggleDrawer: ActionBarDrawerToggle
    private lateinit var idToken: String
    private lateinit var fragmentManager: FragmentManager
    private lateinit var drawerLayout: DrawerLayout
    private val languages: Array<String> = arrayOf("Português", "English", "Español")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Verify if user is logged in
        val sharedPreference = getSharedPreferences("USER", MODE_PRIVATE)
        idToken = sharedPreference.getString("idTokenUser", null).toString()
        //if user is not logged in redirect to the logged in activity
        if (idToken == null.toString()) {
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            val intent = Intent(this, LoginActivity::class.java)
            this.startActivity(intent)
            finish()
            return
        }

        //find references for the drawerLayout and the navigationView
        drawerLayout = findViewById(R.id.drawerLayout)
        val navView: NavigationView = findViewById(R.id.navigation_view)
        val headerView: View = navView.getHeaderView(0)

        //set in the navBarHeader the email of the current user
        val navUsername = headerView.findViewById(R.id.userEmail) as TextView
        navUsername.text = sharedPreference.getString("emailUser", null)

        // Enable the "home" button in the corner of the actionBar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Toggle that is connect to the drawerLayout
        //toggle decides what item it should show an arrow or the menu item
        toggleDrawer =
            ActionBarDrawerToggle(this, drawerLayout, R.string.menu_open, R.string.menu_closed)
        drawerLayout.addDrawerListener(toggleDrawer)

        //Synchronize the state of the drawer toggle with the DrawerLayout
        toggleDrawer.syncState()

        //Actions to take when the item in the navigationView is selected
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> changeFragment(
                    ProductsFragment(), resources.getString(R.string.app_name)
                )
                R.id.changePass -> changeFragment(ChangePassFragment(), it.title.toString())
                R.id.language -> chooseLanguage()
                R.id.about -> changeFragment(AboutAppFragment(), it.title.toString())
                R.id.contacts -> openEmailApp()
                R.id.exit -> logOut()
            }
            true
        }
        //Request that have not been granted at this point
        FunctionsUtil.requestPermissionsIfNecessary(
            arrayOf(
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CAMERA
            ),
            this,this
        )

        if (savedInstanceState == null) {
            //Change to the ProductsFragment
            changeFragment(ProductsFragment(), resources.getString(R.string.app_name))
        }

    }

    //Change the fragment
    private fun changeFragment(fragment: Fragment, title: String) {
        fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
        setTitle(title)
        //close the menu
        drawerLayout.closeDrawers()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //enabling drawer toggle by clicking on the icon
        if (toggleDrawer.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Open app to send email to the pricer contact app
     */
    private fun openEmailApp() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("pricer.app.contact@gmail.com"))
        // intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        startActivity(intent)
    }

    /**
     * function to set the on Click Listener in order to logOut
     */
    private fun logOut() {

        val settings: SharedPreferences = getSharedPreferences("USER", MODE_PRIVATE)
        settings.edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        this.startActivity(intent)
        finish()

    }


    /**
     * Open the dialog with the options of languages and respond to the user selection
     */
    private fun chooseLanguage() {
        val preferenceLang = getSharedPreferences("LANG", MODE_PRIVATE)

        val builderDialog = AlertDialog.Builder(this)
        builderDialog.setTitle(getString(R.string.order_by))
        //get the language id of the current language, if there is none set 0 by default
        val selected: Int = preferenceLang.getInt("languageID", 0)

        //choose what to do depending on the user selection
        builderDialog.setSingleChoiceItems(languages, selected) { dialogInterface, i ->
            when (i) {
                0 -> changeLanguage("pt", i)
                1 -> changeLanguage("en", i)
                2 -> changeLanguage("es", i)
            }
            dialogInterface.dismiss()
        }

        builderDialog.setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }

        val dialog = builderDialog.create()
        dialog.show()
    }

    /**
     * Change the language of the app
     */
    private fun changeLanguage(languageCode: String, selectedId: Int) {
        //set the app language in sharedPreference "LANG" so it persists after turning off
        val preferenceLang = getSharedPreferences("LANG", MODE_PRIVATE)
        val editor = preferenceLang.edit()
        editor.putString("language", languageCode)
        editor.putInt("languageID", selectedId)
        editor.apply()
        //call the update resources of the class LanguageUtil to change the language
        FunctionsUtil.updateConfigLang(this)
        //recreate activity
        recreate()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(FunctionsUtil.updateConfigLang(newBase))
    }


}


