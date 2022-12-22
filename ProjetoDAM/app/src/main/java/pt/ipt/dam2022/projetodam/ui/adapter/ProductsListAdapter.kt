package pt.ipt.dam2022.projetodam.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam2022.projetodam.R
import pt.ipt.dam2022.projetodam.model.Product

/**
 * adapter to the data received to form the list
 */
class ProductsListAdapter(
    private val products: Map<String, Product>,
    private val context: Context,
) : RecyclerView.Adapter<ProductsListAdapter.ViewHolder>() {
    private val keys: List<String> = products.keys.toList()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(product: Product) {
            val category: TextView = itemView.findViewById(R.id.product_category)
            val name: TextView = itemView.findViewById(R.id.product_name)
            val price: TextView = itemView.findViewById(R.id.product_price)

            category.text = product.category
            name.text = product.name
            price.text = product.price.toString()
        }

    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = products[keys[position]]
        holder?.let {
            if (p != null) {
                it.bindView(p)
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