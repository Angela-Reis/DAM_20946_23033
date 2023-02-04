package pt.ipt.dam2022.projetodam.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam2022.projetodam.R


/**
 * Atividade que contém informação sobre a aplicação e os seus desenvolvedores
 */
class AboutUsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_us_activity_layout)

        val backButton: ImageButton = findViewById(R.id.back_button)

        backButton.setOnClickListener {
            goBack(it)
        }

    }

    fun goBack(view: View){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}