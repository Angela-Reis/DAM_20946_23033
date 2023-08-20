package pt.ipt.dam2022.projetodam

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import pt.ipt.dam2022.projetodam.FunctionsUtil.updateConfigLang

/**
 * This class will be used to overwrite some methods
 * in order for it to use the language chosen by the user
 * To use it in all app Activities it was added in the manifest in the tag <application
 */
class MyApp : Application() {

    override fun onCreate() {
        updateConfigLang(this)
        super.onCreate()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(updateConfigLang(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateConfigLang(this)
    }

}