package com.example.jiaqing_hu


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment


class StartFragment : Fragment() {
    private lateinit var input_spinner:Spinner
    private lateinit var input_adapter:ArrayAdapter<String>
    private lateinit var type_spinner:Spinner
    private lateinit var type_adapter:ArrayAdapter<String>
    private lateinit var startbtn:Button

    /* Instantiate items in both spinners,
    * using arrays in string.xml */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_start, container, false)
        startbtn = view.findViewById(R.id.start_btn)
        input_spinner = view.findViewById(R.id.input_spinner)
        type_spinner= view.findViewById(R.id.activity_spinner)

        // array adapters
        input_adapter = ArrayAdapter(view.context,R.layout.item,resources.getStringArray(R.array.input_type))
        input_adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        type_adapter = ArrayAdapter(view.context,R.layout.item,resources.getStringArray(R.array.activity_type))
        type_adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        input_spinner.adapter = input_adapter
        type_spinner.adapter = type_adapter

        // set up listener
        startbtn.setOnClickListener(View.OnClickListener { v ->
            onStartBtnClicked(v)
        })

        return view
    }

    /* Start different activities based on current selected item in input_spinner
    * 1. Manual Entry - ManualActivity
    * 2. GPS - MapActivity
    * 3. Automatic - MapActivity
    * */
    fun onStartBtnClicked(view: View) {
        var intent:Intent = Intent(view.context,ManualActivity::class.java)
        val tmp = input_spinner.selectedItem.toString()
        when(tmp){
            "Manual Entry"->{
                intent = Intent(view.context,ManualActivity::class.java)
                intent.putExtra("inputted",0)
                intent.putExtra("activity",type_spinner.selectedItemId.toInt())
            }
            "GPS"->{
                intent = Intent(view.context,MapActivity::class.java)
                intent.putExtra("inputted",1)
                intent.putExtra("activity",type_spinner.selectedItemId.toInt())
            }
            "Automatic"->{
                intent = Intent(view.context,MapActivity::class.java)
                intent.putExtra("inputted",2)
                intent.putExtra("activity",-1)
            }
        }
        startActivity(intent)
    }
}