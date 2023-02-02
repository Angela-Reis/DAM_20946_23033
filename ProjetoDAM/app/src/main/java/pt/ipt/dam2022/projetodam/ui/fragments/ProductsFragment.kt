package pt.ipt.dam2022.projetodam.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import pt.ipt.dam2022.projetodam.R
import pt.ipt.dam2022.projetodam.model.Product
import pt.ipt.dam2022.projetodam.retrofit.RetrofitProductsInit
import pt.ipt.dam2022.projetodam.ui.adapter.ProductsListAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * Fragment with recycler View that shows the products
 * extends MenuProvider in  order to add menu with SearchView
 */
class ProductsFragment : Fragment(), MenuProvider {
    private lateinit var productsList: Map<String, Product>
    private lateinit var tempList: MutableMap<String, Product>
    private lateinit var idToken: String
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val sharedPreference =
            activity?.getSharedPreferences("USER", AppCompatActivity.MODE_PRIVATE)
        if (sharedPreference != null) {
            idToken = sharedPreference.getString("idTokenUser", null).toString()
        }
        recyclerView = view.findViewById(R.id.productList_view)
        listProducts()
        super.onViewCreated(view, savedInstanceState)
    }


    /**
     * access api with the call specified in listAllProducts
     */
    private fun listProducts() {
        val call =
            RetrofitProductsInit(requireContext()).productService().listAllProducts(idToken)
        processListProducts(call)
    }


    /**
     * add the Products to the interface
     */
    private fun processListProducts(call: Call<Map<String, Product>>) {
        // use data read
        call.enqueue(object : Callback<Map<String, Product>> {
            override fun onResponse(
                call: Call<Map<String, Product>>, response: Response<Map<String, Product>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val products: Map<String, Product> = it
                        // takes the data read from API and shows it the interface
                        configureListProduct(products)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Ocorreu um erro a listar os produtos",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }

            override fun onFailure(call: Call<Map<String, Product>>, t: Throwable) {
                t.message?.let { Log.e("Can't read data ", it) }
            }
        })
    }

    /**
     * configure each 'fragment' to show the data
     */
    private fun configureListProduct(products: Map<String, Product>) {
        if (!(isAdded && context != null)) {
            return
        }
        //save products to arrayList
        productsList = products
        tempList = products.toMutableMap()
        recyclerView.adapter = ProductsListAdapter(tempList, requireContext())

        //get current screen orientation
        val orientation = resources.configuration.orientation
        var columns = 2
        //if screen is in landscape change number of columns
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            columns = 4
        }

        val layoutManager = StaggeredGridLayoutManager(
            columns, StaggeredGridLayoutManager.VERTICAL
        )
        recyclerView.layoutManager = layoutManager
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /**
     * Initialize the menu, adding searchView
     * Adding the QueryTextListener to search for products
     */
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.search, menu)
        val item = menu.findItem(R.id.menu_search)
        val searchView = item?.actionView as SearchView
        //Reference to the searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //clear the tmpList, the list that is shown in the RecyclerView
                tempList.clear()
                //put all the products in tempList
                tempList.putAll(productsList)
                //if text in searchView Exists
                if (!newText.isNullOrEmpty()) {
                    //Filter the tmpList to only contain the products that contain the search text
                    val searchTxt = newText.lowercase()
                    tempList.entries.retainAll {
                        (it.value.name?.lowercase()?.contains(searchTxt)) == true
                    }
                }
                //notify the recyclerView in order for it to update with the right products
                recyclerView.adapter?.notifyDataSetChanged()
                return false
            }
        })
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }


}