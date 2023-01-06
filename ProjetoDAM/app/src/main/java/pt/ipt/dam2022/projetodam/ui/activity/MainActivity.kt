package pt.ipt.dam2022.projetodam.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.navigation.NavigationView
import pt.ipt.dam2022.projetodam.R
import pt.ipt.dam2022.projetodam.model.Product
import pt.ipt.dam2022.projetodam.retrofit.RetrofitInitializer
import pt.ipt.dam2022.projetodam.ui.adapter.ProductsListAdapter
import pt.ipt.dam2022.projetodam.ui.fragments.MainActivityHeaderFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    lateinit var header:MainActivityHeaderFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        header = MainActivityHeaderFragment()

        addMenuOptions()

        val headerTransaction = supportFragmentManager.beginTransaction()
        headerTransaction.add(R.id.headerFragment,header)
        headerTransaction.addToBackStack(null)
        headerTransaction.commit()

        //Request that have not been granted at this point
        requestPermissionsIfNecessary(
            arrayOf(

                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
            )
        )


        listProducts()
    }

    /**
     * function to programmatically add options to the NavigationView Menu
     */
    private fun addMenuOptions(){
        var menu:NavigationView = findViewById(R.id.navigation_view)

        menu.menu.add("Test")
        menu.menu.add("Test")
        menu.menu.add("Test")
        menu.menu.add("Test")
        menu.menu.add("Test")
        menu.menu.add("Test")
    }

    /**
     * function to collect user permission
     */
    private fun requestPermissionsIfNecessary(permissions: Array<out String>) {
        val permissionsToRequest = ArrayList<String>()
        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toArray(arrayOf<String>()),
                1
            )
        }
    }

    /**
     * access api with the call specified in listAllProducts
     */
    private fun listProducts() {
        val call = RetrofitInitializer().productService().listAllProducts()
        processListProducts(call)
    }


    /**
     * add the Products to the interface
     */
    private fun processListProducts(call: Call<Map<String, Product>>) {
        // use data read
        call.enqueue(object : Callback<Map<String, Product>> {
            override fun onResponse(
                call: Call<Map<String, Product>>?, response: Response<Map<String, Product>>?
            ) {
                response?.body()?.let {
                    val products: Map<String, Product> = it
                    // takes the data read from API and shows it the interface
                    configureListProduct(products)
                }
            }

            override fun onFailure(call: Call<Map<String, Product>>, t: Throwable) {
                t?.message?.let { Log.e("I can not read data...", it) }
            }
        })
    }

    /**
     * configure each 'fragment' to show the data
     */
    private fun configureListProduct(products: Map<String, Product>) {
        val recyclerView = findViewById<RecyclerView>(R.id.productList_recyclerview)
        recyclerView.adapter = ProductsListAdapter(products, this)
        val layoutManager = StaggeredGridLayoutManager(
            2, StaggeredGridLayoutManager.VERTICAL
        )
        recyclerView.layoutManager = layoutManager
    }

    /**
     * função genérica para mudar de view
     */
    fun changeActivity(view: View){
        val intent = Intent(this, TestActivity::class.java)
        startActivity(intent)
    }

    fun changeToAboutUsActivity(){
        val intent = Intent(this, AboutUsActivity::class.java)
        startActivity(intent)
    }
}


