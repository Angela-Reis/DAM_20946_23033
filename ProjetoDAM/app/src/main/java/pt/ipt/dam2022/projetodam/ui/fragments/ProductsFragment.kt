package pt.ipt.dam2022.projetodam.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
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
import pt.ipt.dam2022.projetodam.model.Store
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
    lateinit var textStore: TextView
    lateinit var textCategory: TextView
    lateinit var selectedStore: BooleanArray
    lateinit var selectedCategory: BooleanArray
    lateinit var stores: Map<String, Store>
    lateinit var categories: Array<String?>


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
        textStore = view.findViewById(R.id.selectStore)
        textCategory = view.findViewById(R.id.selectCategory)
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
                        getStore()
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

        //add menu after products are loaded
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        getStore()
    }


    /**
     * Get list of all stores and save it in the variable stores of type Map<String, Stores>
     */
    fun getStore() {
        val call =
            RetrofitProductsInit(requireContext()).productService().listAllStores(idToken)
        // use data read
        call.enqueue(object : Callback<Map<String, Store>> {
            override fun onResponse(
                call: Call<Map<String, Store>>, response: Response<Map<String, Store>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        stores = it
                        // takes the data read from API and shows it the interface
                        setLists()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Ocorreu um erro a listar os produtos",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }

            override fun onFailure(call: Call<Map<String, Store>>, t: Throwable) {
                t.message?.let { Log.e("Can't read data ", it) }
            }
        })
    }


    /**
     * Add options pop up to filter products when clicking on the textView for stores and categories
     */
    fun setLists() {
        //extract all categories from the products
        categories = (productsList.map { it.value.category }.distinct()).toTypedArray()

        //initialize selected filter as all selected
        selectedStore = BooleanArray(stores.size) { true }
        selectedCategory = BooleanArray(categories.size) { true }


        val storeArray = stores.map { it.value.name }.toTypedArray()
        buildSelectorDialog(categories, textCategory, selectedCategory, "Selecione Categorias")
        buildSelectorDialog(storeArray, textStore, selectedStore, "Selecione Lojas")
    }


    /**
    + Build a clickListener for a TextView that opens a dialog with multi selection
     */
    private fun buildSelectorDialog(
        strings: Array<String?>,
        txtView: TextView,
        selectedArray: BooleanArray,
        title: String
    ) {
        txtView.setOnClickListener {
            val dialogList: Array<String?> = strings
            val builderDialog = android.app.AlertDialog.Builder(requireContext())
            builderDialog.setTitle(title)

            // alert dialog shouldn't be cancellable
            builderDialog.setCancelable(false)

            // Creating multiple selection by using setMutliChoiceItem method
            builderDialog.setMultiChoiceItems(
                dialogList, selectedArray
            ) { dialog, whichButton, isChecked ->
                selectedArray[whichButton] = isChecked
            }


            // handle the positive button of the dialog
            builderDialog.setPositiveButton("OK") { dialog, which ->
                filterData()
            }

            val alert = builderDialog.create()
            alert.show()
        }

    }

    /**
     * filter tempList depending on the stores and categories selected
     * remove from tempList the products that are not from the stores and categories selected
     */
    private fun filterData() {
        //clear the tmpList, the list that is shown in the RecyclerView
        tempList.clear()
        //put all the products in tempList
        tempList.putAll(productsList)
        //get all the stores keys, these will be in the same order as tempList
        var stores: List<String> = ArrayList(stores.keys)
        //keys of stores selected
        stores = stores.filterIndexed { index, value -> selectedStore[index] }
        tempList.entries.retainAll {
            elementCommon(ArrayList(it.value.stores?.keys), stores)
        }

        var categories: List<String> = categories.asList() as List<String>
        categories = categories.filterIndexed { index, value -> selectedCategory[index] }
        tempList.entries.retainAll {
            categories.contains(it.value.category)
        }
        recyclerView.adapter?.notifyDataSetChanged()


    }

    /**
     * Verify if the two arrays have at least one element in common
     */
    fun elementCommon(a: List<String>, b: List<String>): Boolean {
        val set = a.toSet()
        return b.any { it in set }
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
                filterData()
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