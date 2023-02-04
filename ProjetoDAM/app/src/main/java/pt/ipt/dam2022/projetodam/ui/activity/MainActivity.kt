package pt.ipt.dam2022.projetodam.ui.activity

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.navigation.NavigationView
import pt.ipt.dam2022.projetodam.R
import pt.ipt.dam2022.projetodam.ui.activity.login.LoginActivity
import pt.ipt.dam2022.projetodam.ui.fragments.AboutAppFragment
import pt.ipt.dam2022.projetodam.ui.fragments.ChangePassFragment
import pt.ipt.dam2022.projetodam.ui.fragments.ContactFragment
import pt.ipt.dam2022.projetodam.ui.fragments.ProductsFragment


class MainActivity : AppCompatActivity() {
    private lateinit var toggleDrawer: ActionBarDrawerToggle
    private lateinit var idToken: String
    private lateinit var fragmentManager: FragmentManager
    private lateinit var drawerLayout: DrawerLayout


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
        //tooggle decides what item it should show an arrow or the menu item
        toggleDrawer = ActionBarDrawerToggle(this, drawerLayout, R.string.aberto, R.string.fechado)
        drawerLayout.addDrawerListener(toggleDrawer)

        //Synchronize the state of the drawer toggle with the DrawerLayout
        toggleDrawer.syncState()

        //Actions to take when the item in the navigationView is selected
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> changeFragment(
                    ProductsFragment(), resources.getString(R.string.app_name)
                )
                R.id.sobre -> changeFragment(AboutAppFragment(), it.title.toString())
                R.id.contactos -> changeFragment(ContactFragment(), it.title.toString())
                R.id.alterarPass -> changeFragment(ChangePassFragment(), it.title.toString())
                R.id.sair -> logOut()
            }
            true
        }
        //Request that have not been granted at this point
        requestPermissionsIfNecessary(
            arrayOf(
                Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.CAMERA
            )
        )

        if(savedInstanceState == null){
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
     * function to collect user permission
     */
    private fun requestPermissionsIfNecessary(permissions: Array<out String>) {
        val permissionsToRequest = ArrayList<String>()
        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(
                    this, permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this, permissionsToRequest.toArray(arrayOf<String>()), 1
            )
        }
    }


}


