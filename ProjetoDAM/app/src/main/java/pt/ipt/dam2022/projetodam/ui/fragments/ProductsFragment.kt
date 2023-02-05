package pt.ipt.dam2022.projetodam.ui.fragments

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import pt.ipt.dam2022.projetodam.R
import pt.ipt.dam2022.projetodam.model.Product
import pt.ipt.dam2022.projetodam.model.Store
import pt.ipt.dam2022.projetodam.retrofit.RetrofitProductsInit
import pt.ipt.dam2022.projetodam.ui.activity.CaptureBarCodeAct
import pt.ipt.dam2022.projetodam.ui.activity.ProductActivity
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
    private lateinit var selectStore: TextView
    private lateinit var selectCategory: TextView
    private lateinit var selectOrder: TextView
    private lateinit var selectedStore: BooleanArray
    private lateinit var selectedCategory: BooleanArray
    lateinit var stores: Map<String, Store>
    private lateinit var categories: Array<String?>
    private var selectedOrder: Int = 0
    private lateinit var orderOptions: Array<String>
    private var dialog: AlertDialog? = null

    // Register the launcher and result handler of the bar code Scanner
    private val barcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(context, getString(R.string.cancelled), Toast.LENGTH_LONG).show()
        } else {
            //Process the bar code to check if product exists
            checkProductExistence(result.contents)
            // adding ALERT Dialog builder object and passing activity as parameter
            val builder = AlertDialog.Builder(activity)
            val inflater = requireActivity().layoutInflater
            //show layout in loading.xml
            builder.setView(inflater.inflate(R.layout.loading, null))
            builder.setCancelable(false)

            dialog = builder.create()
            (dialog as AlertDialog).show()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //Options of how user can order the products
        orderOptions = arrayOf(
            getString(R.string.ascending_price), getString(
                R.string.descending_price
            )
        )

        //Get idToken
        val sharedPreference =
            activity?.getSharedPreferences("USER", AppCompatActivity.MODE_PRIVATE)
        if (sharedPreference != null) {
            idToken = sharedPreference.getString("idTokenUser", null).toString()
        }

        //Get references to the components of the fragment
        recyclerView = view.findViewById(R.id.productList_view)
        selectStore = view.findViewById(R.id.selectStore)
        selectCategory = view.findViewById(R.id.selectCategory)
        selectOrder = view.findViewById(R.id.selectOrder)

        //List all products, make request to api
        listProducts()

        //add refresh products when swiping down
        val pullToRefresh: SwipeRefreshLayout = view.findViewById(R.id.pullToRefresh)
        pullToRefresh.setOnRefreshListener {
            listProducts() // your code
            pullToRefresh.isRefreshing = false
        }

        //add menu provider in order to add menu options, barcode scanner and search
        // in the overwritten function onCreateMenu
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        super.onViewCreated(view, savedInstanceState)
    }


    /*
     * Function called by bar code scanner to check is product exists in the Database
     * this is checked by calling the API with a query
     */
    private fun checkProductExistence(barcode: String) {
        val call = RetrofitProductsInit(requireContext()).productService()
            .getProductBarCode("\"barcode\"", "\"" + barcode + "\"", idToken)
        // use data read
        call.enqueue(object : Callback<Map<String, Product>> {
            override fun onResponse(
                call: Call<Map<String, Product>>, response: Response<Map<String, Product>>
            ) {
                dialog?.dismiss()
                if (response.isSuccessful) {
                    // takes the data read from API and shows it the interface
                    processBarCodeResult(response.body(), barcode)

                } else {
                    Toast.makeText(
                        requireContext(),
                        context?.getString(R.string.error_message),
                        Toast.LENGTH_LONG
                    ).show()
                }

            }

            override fun onFailure(call: Call<Map<String, Product>>, t: Throwable) {
                dialog?.dismiss()
                t.message?.let { Log.e("Can't read data ", it) }
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_message),
                    Toast.LENGTH_LONG
                ).show()
            }
        })


    }

    /**
     * Process the barCode request after the request to API returned the results
     */
    fun processBarCodeResult(resultProduct: Map<String, Product>?, barcode: String) {
        if (resultProduct?.isNotEmpty() == true) {
            //if response is not null, grab the first item,
            val productKey = resultProduct.keys.toList()[0]
            val p = resultProduct[productKey]

            //open product page
            val intent = Intent(context, ProductActivity::class.java)
            intent.putExtra("Product", p)
            intent.putExtra("ProductKey", productKey)
            startActivity(intent)
        } else {
            //in case the product with corresponding barcode is not found in the DB
            // ask user if they wish to search online
            val builder = AlertDialog.Builder(context)
            builder.setTitle(getString(R.string.result_scan))
            builder.setMessage(
                buildString {
                    append(getString(R.string.product_not_found))
                    append(barcode + "\n")
                    append(getString(R.string.search_in_web_question))
                }
            )

            builder.setPositiveButton(getString(R.string.search)) { _, _ ->
                //Open Activity with web search of product bar code
                val intent = Intent(Intent.ACTION_WEB_SEARCH)
                intent.putExtra(SearchManager.QUERY, barcode)
                startActivity(intent)
            }

            builder.setNegativeButton(getString(R.string.cancel)) { _, _ ->

            }
            builder.show()

        }
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
                        getString(R.string.error_message),
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

        //order tempList by option selected
        orderProducts()

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

        getStore()
    }


    /**
     * Get list of all stores and save it in the variable stores of type Map<String, Stores>
     */
    private fun getStore() {
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
                        getString(R.string.error_message),
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
     * Set color of button of dialog
     */
    private fun setDialogButtonColor(dialog: AlertDialog) {
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
            ContextCompat.getColor(
                requireContext(), R.color.teal_200
            )
        )
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
            ContextCompat.getColor(
                requireContext(), R.color.teal_200
            )
        )
    }

    /**
     * Add options pop up to filter products when clicking on the textView for stores and categories
     */
    fun setLists() {
        //extract all categories from the products
        categories = (productsList.map { it.value.category }.distinct()).toTypedArray()

        //initialize selected filter, if they haven't yet been, as all selected
        selectedStore = BooleanArray(stores.size) { true }
        selectedCategory = BooleanArray(categories.size) { true }

        //transform Map of stores into Array
        val storeArray = stores.map { it.value.name }.toTypedArray()

        //build the multi selectors Dialog for stores and category
        buildSelectorDialog(
            categories,
            selectCategory,
            selectedCategory,
            getString(R.string.select_category)
        )
        buildSelectorDialog(
            storeArray,
            selectStore,
            selectedStore,
            getString(R.string.select_store)
        )

        //Defines what happens when clicked on the selectOrder TextView
        selectOrder.setOnClickListener {
            //Open dialog with options
            val builderDialog = AlertDialog.Builder(requireContext())
            builderDialog.setTitle(getString(R.string.order_by))
            builderDialog.setSingleChoiceItems(orderOptions, selectedOrder) { dialogInterface, i ->
                //If user selects item filterData according to the order selected
                selectedOrder = i
                filterData()
                dialogInterface.dismiss()
            }
            builderDialog.setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            val dialog = builderDialog.create()
            dialog.show()
            //set color of the dialog button
            setDialogButtonColor(dialog)
        }


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
            val builderDialog = AlertDialog.Builder(requireContext())
            builderDialog.setTitle(title)

            // set alert dialog so it isn't cancellable
            builderDialog.setCancelable(false)

            // Creating multiple selection by using setMultiChoiceItem method
            builderDialog.setMultiChoiceItems(
                dialogList, selectedArray
            ) { _, whichButton, isChecked ->
                //Change option so that array according to actions selected
                selectedArray[whichButton] = isChecked
            }


            // handle the positive button of the dialog, filter the data when it's selected
            builderDialog.setPositiveButton("OK") { _, _ ->
                filterData()
            }

            val dialog = builderDialog.create()
            dialog.show()
            setDialogButtonColor(dialog)

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
        var storesList: List<String> = ArrayList(stores.keys)
        //keys of stores selected
        storesList = storesList.filterIndexed { index, _ -> selectedStore[index] }
        tempList.entries.retainAll {
            elementCommon(ArrayList(it.value.stores?.keys!!), storesList)
        }

        //get all the
        var categories: List<String?> = categories.asList()
        categories = categories.filterIndexed { index, _ -> selectedCategory[index] }
        tempList.entries.retainAll {
            categories.contains(it.value.category)
        }

        //order tempList in accordance to the selected option
        orderProducts()

        //notify the recycler view adapter that the data changed so it can update it
        recyclerView.adapter?.notifyDataSetChanged()


    }

    /**
     * replace the product Map in the recyclerView with a sorted version, depending on which option is selected
     */
    private fun orderProducts() {
        //Sort by the selected option
        when (selectedOrder) {
            0 -> tempList =
                tempList.toList()
                    .sortedBy { (_, value) -> value.price }
                    .toMap().toMutableMap()
            1 -> tempList =
                tempList.toList()
                    .sortedByDescending { (_, value) -> value.price }
                    .toMap().toMutableMap()

        }

        (recyclerView.adapter as ProductsListAdapter).setProducts(tempList)
    }

    /**
     * Verify if the two arrays have at least one element in common
     */
    private fun elementCommon(a: List<String>, b: List<String>): Boolean {
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
                // If products are not loaded do nothing
                if (!::tempList.isInitialized) {
                    return false
                }
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

        //zxing-android-embedded Barcode scanner library
        val itemScan = menu.findItem(R.id.menu_scanner)
        itemScan.setOnMenuItemClickListener {
            //Set options for barScanner
            val options = ScanOptions()
            options.setPrompt(getString(R.string.scan_prompt))
            options.setOrientationLocked(false)
            //Use class that extends CaptureActivity
            // and is referenced in Manifest as having screenRotation fullSensor
            options.captureActivity = CaptureBarCodeAct::class.java
            options.setBeepEnabled(false)
            //Launch the barcode Scanner
            barcodeLauncher.launch(options)
            true
        }
    }


    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }


}