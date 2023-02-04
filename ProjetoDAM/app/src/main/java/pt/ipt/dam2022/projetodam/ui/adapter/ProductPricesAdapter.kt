package pt.ipt.dam2022.projetodam.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam2022.projetodam.R
import pt.ipt.dam2022.projetodam.model.StorePrice
import java.text.DecimalFormat


class ProductPricesAdapter(
    private val storePrices: ArrayList<StorePrice>,
    private val context: Context,
) : RecyclerView.Adapter<ProductPricesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(product: StorePrice) {
            val name: TextView = itemView.findViewById(R.id.price_store_name)
            val price: TextView = itemView.findViewById(R.id.price_store)

            name.text = product.storeName
            val pTxt = DecimalFormat("##.00").format(product.price).toString() + "€"
            price.text = pTxt
        }

    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = storePrices[position]
        holder.let {
            if (p != null) {
                it.bindView(p)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.store_price, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return storePrices.size
    }
}