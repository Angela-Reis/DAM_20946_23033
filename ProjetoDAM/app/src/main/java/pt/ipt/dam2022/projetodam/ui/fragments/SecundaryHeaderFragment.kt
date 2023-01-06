package pt.ipt.dam2022.projetodam.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import pt.ipt.dam2022.projetodam.R


class SecundaryHeaderFragment : Fragment(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view:View = inflater.inflate(R.layout.secundary_header_fragment_layout, container, false)

        var toolBar:androidx.appcompat.widget.Toolbar = view.findViewById(R.id.secundary_tool_bar)
        // CREATE LISTENER HERE

        var backArrow:ImageButton = view.findViewById(R.id.back_arrow_btn)
        // CREATE LISTENER HERE

        var txt:TextView = view.findViewById(R.id.about_us_txt)
        // CREATE LISTENER HERE

        return view
    }

    fun changeHeaderTitle (){

    }
}