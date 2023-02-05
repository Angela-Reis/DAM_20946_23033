package pt.ipt.dam2022.projetodam.ui.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam2022.projetodam.R
import pt.ipt.dam2022.projetodam.model.Product
import pt.ipt.dam2022.projetodam.ui.activity.ProductActivity
import java.io.InputStream
import java.net.URL
import java.text.DecimalFormat
import java.util.concurrent.Executors


/**
 * adapter to the data received to form the list
 */
class ProductsListAdapter(
    private var products: MutableMap<String, Product>,
    private val context: Context,
) : RecyclerView.Adapter<ProductsListAdapter.ViewHolder>() {

    lateinit var images: MutableMap<Product, Bitmap?>

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(product: Product, productKey: String, image: Bitmap?) {
            val category: TextView = itemView.findViewById(R.id.product_category)
            val name: TextView = itemView.findViewById(R.id.product_name)
            val price: TextView = itemView.findViewById(R.id.product_price)
            val imageProduct: ImageView = itemView.findViewById(R.id.imageProduct)

            //load the image and set it in imageProduct
            //product.setImageTask(imageProduct)
            if (image != null) {
                val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
                //hide progress bar and show image
                progressBar.visibility = View.INVISIBLE
                imageProduct.visibility = View.VISIBLE
                imageProduct.setImageBitmap(image)
            }

            category.text = product.category
            name.text = product.name
            val pTxt = DecimalFormat("##.00").format(product.price).toString() + "€"
            price.text = pTxt

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ProductActivity::class.java)
                intent.putExtra("Product", product)
                intent.putExtra("ProductKey", productKey)
                itemView.context.startActivity(intent)
            }
        }

    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val productKey = products.keys.toList()[position]
        val p = products[productKey]
        if (images[p] == null) {
            setImageTask(products[products.keys.toList()[position]] as Product, position)
        }
        holder.let {
            if (p != null) {
                it.bindView(p, productKey, images[p])
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.product_item, parent, false)
        //create MutableMap images
        images = mutableMapOf<Product, Bitmap?>()
            .apply {
                for (i in 0 until products.size) {
                    val p = (products[products.keys.toList()[i]]) as Product
                    this[p] = null
                }
            }

        return ViewHolder(view)
    }


    //get images in a asynchronous way and save then to the MutableMap images and alert of change
    //https://developer.android.com/guide/background/asynchronous/java-threads
    fun setImageTask(pr: Product, i: Int) {
        val handler = Handler(Looper.getMainLooper())
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            //Background thread
            var productImage: Bitmap? = null
            try {
                val `in`: InputStream =
                    URL(pr.image).openStream()
                productImage = BitmapFactory.decodeStream(`in`)
            } catch (e: Exception) {
                Log.e(context.getString(R.string.error_message), e.message!!)
                e.printStackTrace()
            }
            handler.post {
                //UI Thread
                //save bitMap
                images[pr] = productImage
                //notify adapter that the image finished loading
                notifyItemChanged(i)
            }
        }

    }

    override fun getItemCount(): Int {
        return products.size
    }

    fun setProducts(newProducts: MutableMap<String, Product>) {
        products = newProducts
    }
}