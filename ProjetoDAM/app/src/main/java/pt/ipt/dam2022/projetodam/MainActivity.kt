package pt.ipt.dam2022.projetodam

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /**
     * função genérica para mudar de view
     */
    fun changeActivity(view: View){
        val intent = Intent(this, Test::class.java)
        startActivity(intent)
    }
}