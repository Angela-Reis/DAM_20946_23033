package pt.ipt.dam2022.projetodam.ui.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
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
import java.io.InputStream
import java.net.URL
import java.text.DecimalFormat
import java.util.concurrent.Executors


class ProductActivity : AppCompatActivity() {
    private lateinit var productKey: String
    private lateinit var product: Product
    private var storePrices = ArrayList<StorePrice>()
    lateinit var idToken: String
    lateinit var recyclerView: RecyclerView
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
            getStore(entry.key)
        }
        val category: TextView = findViewById(R.id.product_category)
        val name: TextView = findViewById(R.id.product_name)
        val price: TextView = findViewById(R.id.product_price)
        val barcode: TextView = findViewById(R.id.product_barcode)
        category.text = product.category
        name.text = product.name
        if (product.barcode.isNullOrEmpty()) {
            barcode.text = getString(R.string.barcode_unknown)
        } else {
            barcode.text = buildString {
                append(getString(R.string.barcode) + " " + product.barcode)
            }
        }
        val pTxt = DecimalFormat("##.00").format(product.price).toString() + "€"
        price.text = pTxt

        product.image?.let { downloadImageTask(findViewById(R.id.imageView2), it) }


        //initialize recyclerView with list of stores
        recyclerView = findViewById(R.id.productList_recyclerview)
        recyclerView.adapter = ProductPricesAdapter(storePrices, this)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


    }


    private fun downloadImageTask(bmImage: ImageView, url: String) {
        val handler = Handler(Looper.getMainLooper())
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            var productImage: Bitmap? = null
            try {
                val `in`: InputStream =
                    URL(url).openStream()
                productImage = BitmapFactory.decodeStream(`in`)
            } catch (e: Exception) {
                Log.e("Error", e.message!!)
                e.printStackTrace()
            }
            handler.post {
                bmImage.setImageBitmap(productImage)
            }
        }

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
                        getString(R.string.error_message),
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
                        //Notify the view that storeArray was changed
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.error_message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<StorePrice>, t: Throwable) {
                t.message?.let { Log.e("Can't read data ", it) }
            }
        })
    }

}