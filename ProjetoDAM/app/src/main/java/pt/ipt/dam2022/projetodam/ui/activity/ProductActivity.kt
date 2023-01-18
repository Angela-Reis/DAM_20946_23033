package pt.ipt.dam2022.projetodam.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam2022.projetodam.R
import pt.ipt.dam2022.projetodam.model.Product
import pt.ipt.dam2022.projetodam.model.StorePrice
import pt.ipt.dam2022.projetodam.retrofit.RetrofitInitializer
import pt.ipt.dam2022.projetodam.ui.adapter.ProductPricesAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProductActivity : AppCompatActivity() {
    private lateinit var productKey: String
    private lateinit var product: Product
    private var storePrices = ArrayList<StorePrice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)
        product = (intent.getSerializableExtra("Product") as Product?)!!
        productKey = intent.getStringExtra("ProductKey").toString()

        val category: TextView = findViewById(R.id.product_category)
        val name: TextView = findViewById(R.id.product_name)
        val price: TextView = findViewById(R.id.product_price)

        if (product != null) {
            category.text = product.category
            name.text = product.name
            price.text = product.price.toString()

            product.stores?.forEach { entry ->
                /*Toast.makeText(
                    this,
                    "PRODUCT KEY =  $productKey STORE KEY= ${entry.key}",
                    Toast.LENGTH_LONG
                ).show()*/
                getStoreName(entry.key)

                // var allStorePrices: Map<String keyStore, >
                // getStorePrice(entry.key, productKey)
            }
            /*showListStores();*/

        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    /**
     * get the Name of a particular Store via the storeKey
     * then pass that information to get the Product Price in that store
     * */
    private fun getStoreName(storeKey: String) {
        val call = RetrofitInitializer().productService().getStoreName(storeKey)
        call.enqueue(object : Callback<String> {

            override fun onResponse(call: Call<String>, response: Response<String>) {
                response.body()?.let {
                    val name: String = it
                    // Get the Price of the Product in the Store
                    // and associate it with the store name that was fetched
                    val callPrice = RetrofitInitializer().productService()
                        .getProductPriceFromStore(storeKey, productKey)
                    processProductPrice(callPrice, name, storeKey)

                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                t.message?.let { Log.e("Can't read data ", it) }
            }
        })
    }


    /**
     * Process the Call to get ProductPrice
     */
    private fun processProductPrice(call: Call<StorePrice>, name: String, storeKey: String) {
        // use data read
        call.enqueue(object : Callback<StorePrice> {
            override fun onResponse(call: Call<StorePrice>, response: Response<StorePrice>) {
                response.body()?.let {
                    val result: StorePrice = it
                    result.storeName = name
                    result.storeKey = storeKey
                    storePrices.add(result)
                    configureListPrice()
                }
            }

            override fun onFailure(call: Call<StorePrice>, t: Throwable) {
                t.message?.let { Log.e("Can't read data ", it) }
            }
        })
    }


    /**
     * configure each 'fragment' to show the data of the store Price
     */
    private fun configureListPrice() {
        if (storePrices.size != product.stores?.size) {
            return
        }
        val recyclerView = findViewById<RecyclerView>(R.id.productList_recyclerview)
        recyclerView.adapter = ProductPricesAdapter(storePrices, this)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

    }

}