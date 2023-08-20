package pt.ipt.dam2022.projetodam

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.ArrayList
import java.util.Locale


object FunctionsUtil {

    /**
     * Update the locale of the app, in order to use right language
     */
    fun updateConfigLang(context: Context): Context? {
        //Get the saved language in sharedPreferences LANG
        val sharedLang = context.getSharedPreferences("LANG", AppCompatActivity.MODE_PRIVATE)
        //get language code from sharedPreferences, if there is none get "pt" by default
        val lang = sharedLang.getString("language", "pt")!!
        //get locale from the language code
        val newLocale = Locale(lang)

        val configuration = context.resources.configuration
        configuration.setLocale(newLocale)
        // create a new configuration context and return it
        return context.createConfigurationContext(configuration)
    }

    /**
     * function to collect user permission
     */
     fun requestPermissionsIfNecessary(permissions: Array<out String>, context: Context, activity: Activity) {
        val permissionsToRequest = ArrayList<String>()
        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(
                    context, permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toArray(arrayOf<String>()),
                1
            )
        }
    }

}