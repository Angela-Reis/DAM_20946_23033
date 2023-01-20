package pt.ipt.dam2022.projetodam.ui.activity

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam2022.projetodam.R


/**
 * Atividade genérica
 */
class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}