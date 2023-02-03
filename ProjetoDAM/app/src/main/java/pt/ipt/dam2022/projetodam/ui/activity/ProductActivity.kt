package pt.ipt.dam2022.projetodam.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam2022.projetodam.R
import pt.ipt.dam2022.projetodam.model.Product
import pt.ipt.dam2022.projetodam.model.Store
import pt.ipt.dam2022.projetodam.model.StorePrice
import pt.ipt.dam2022.projetodam.retrofit.RetrofitProductsInit
import pt.ipt.dam2022.projetodam.ui.activity.login.LoginActivity
import pt.ipt.dam2022.projetodam.ui.adapter.ProductPricesAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProductActivity : AppCompatActivity() {
    private lateinit var productKey: String
    private lateinit var product: Product
    private var storePrices = ArrayList<StorePrice>()
    lateinit var idToken: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val sharedPreference = getSharedPreferences("USER", MODE_PRIVATE)
        setContentView(R.layout.activity_product)
        if (intent.getSerializableExtra("Product") == null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        product = (intent.getSerializableExtra("Product") as Product?)!!
        productKey = intent.getStringExtra("ProductKey").toString()
        idToken = sharedPreference.getString("idTokenUser", null).toString()
        if (idToken == null.toString()) {
            val intent = Intent(this, LoginActivity::class.java)
            this.startActivity(intent)
        }

        product.stores?.forEach { entry ->
            /*Toast.makeText(
                this,
                "PRODUCT KEY =  $productKey STORE KEY= ${entry.key}",
                Toast.LENGTH_LONG
            ).show()*/
            getStore(entry.key)

            // var allStorePrices: Map<String keyStore, >
            // getStorePrice(entry.key, productKey)
        }
        /*showListStores();*/
        val category: TextView = findViewById(R.id.product_category)
        val name: TextView = findViewById(R.id.product_name)
        val price: TextView = findViewById(R.id.product_price)
        category.text = product.category
        name.text = product.name
        price.text = product.price.toString()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return false
    }

    /**
     * get the Name of a particular Store via the storeKey
     * then pass that information to get the Product Price in that store
     * */
    private fun getStore(storeKey: String) {
        val call = RetrofitProductsInit(applicationContext).productService()
            .getStore(storeKey, idToken)
        call.enqueue(object : Callback<Store> {

            override fun onResponse(call: Call<Store>, response: Response<Store>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        // Get the Price of the Product in the Store
                        // and associate it with the store name that was fetched
                        getProductPrice(it, storeKey)

                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Ocorreu um erro",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }


            override fun onFailure(call: Call<Store>, t: Throwable) {
                t.message?.let { Log.e("Can't read data ", it) }
            }
        })
    }


    /**
     * Process the Call to get ProductPrice
     */
    private fun getProductPrice(store: Store, storeKey: String) {
        val call = RetrofitProductsInit(applicationContext).productService()
            .getProductPriceFromStore(storeKey, productKey, idToken)
        // use data read
        call.enqueue(object : Callback<StorePrice> {
            override fun onResponse(call: Call<StorePrice>, response: Response<StorePrice>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val result: StorePrice = it
                        result.storeName = store.name
                        result.storeKey = storeKey
                        storePrices.add(result)
                        configureListPrice()
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Ocorreu um erro",
                        Toast.LENGTH_LONG
                    ).show()
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