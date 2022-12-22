package pt.ipt.dam2022.projetodam.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import pt.ipt.dam2022.projetodam.R

private const val SEARCH_GENERIC_TEXT_PT = "Pesquisar"

class HeaderFragment : Fragment(){

    private var searchGenericText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            searchGenericText = it.getString(SEARCH_GENERIC_TEXT_PT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view:View = inflater.inflate(R.layout.header_fragment_layout, container, false)

        var btnMenu:ImageButton = view.findViewById(R.id.btnMenu)
        // CREATE LISTENER HERE

        var txt:TextView = view.findViewById(R.id.searchBar)
        // CREATE LISTENER HERE

        var btnSearch: ImageButton = view.findViewById(R.id.searchBtn)
        // CREATE LISTENER HERE

        var btnFilter:Button = view.findViewById(R.id.btnFilter)
        // CREATE LISTENER HERE

        return view
    }
}