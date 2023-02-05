package pt.ipt.dam2022.projetodam

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale


object LanguageUtil {

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

}