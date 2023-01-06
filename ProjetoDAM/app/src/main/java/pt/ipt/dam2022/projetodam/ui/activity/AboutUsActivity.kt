package pt.ipt.dam2022.projetodam.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam2022.projetodam.R
import pt.ipt.dam2022.projetodam.ui.fragments.SecundaryHeaderFragment


/**
 * Atividade que contém informação sobre a aplicação e os seus desenvolvedores
 */
class AboutUsActivity : AppCompatActivity() {

    lateinit var header:SecundaryHeaderFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_us_activity_layout)

        header = SecundaryHeaderFragment()

        val headerTransaction = supportFragmentManager.beginTransaction()
        headerTransaction.add(R.id.secondaryHeaderFragment,header)
        headerTransaction.addToBackStack(null)
        headerTransaction.commit()
    }
}