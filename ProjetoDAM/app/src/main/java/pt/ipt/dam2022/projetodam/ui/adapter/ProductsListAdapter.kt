package pt.ipt.dam2022.projetodam.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam2022.projetodam.R
import pt.ipt.dam2022.projetodam.model.Product
import pt.ipt.dam2022.projetodam.ui.activity.ProductActivity


/**
 * adapter to the data received to form the list
 */
class ProductsListAdapter(
    private val products: Map<String, Product>,
    private val context: Context,
) : RecyclerView.Adapter<ProductsListAdapter.ViewHolder>() {
    private val keys: List<String> = products.keys.toList()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(product: Product, productKey: String) {
            val category: TextView = itemView.findViewById(R.id.product_category)
            val name: TextView = itemView.findViewById(R.id.product_name)
            val price: TextView = itemView.findViewById(R.id.product_price)

            category.text = product.category
            name.text = product.name
            price.text = product.price.toString()
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ProductActivity::class.java)
                intent.putExtra("Product", product)
                intent.putExtra("ProductKey", productKey)
                itemView.context.startActivity(intent)
            }
        }

    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val productKey = keys[position]
        val p = products[productKey]
        holder.let {
            if (p != null) {
                it.bindView(p, productKey)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.product_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return products.size
    }
}