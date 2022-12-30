package pt.ipt.dam2022.projetodam.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import pt.ipt.dam2022.projetodam.R
import pt.ipt.dam2022.projetodam.model.Product

class ProductActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)
        val product = intent.getSerializableExtra("Product") as Product?
        val category: TextView = findViewById(R.id.product_category)
        val name: TextView = findViewById(R.id.product_name)
        val price: TextView = findViewById(R.id.product_price)

        if (product != null) {
            category.text = product.category
            name.text = product.name
            price.text = product.price.toString()
        };
    }
}