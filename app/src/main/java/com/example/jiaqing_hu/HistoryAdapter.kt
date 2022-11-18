package com.example.jiaqing_hu

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.jiaqing_hu.database.ExerciseEntry

class HistoryAdapter(private val context: Context, private var entryList: List<ExerciseEntry>):BaseAdapter() {
    override fun getCount() : Int {
        return entryList.size
    }

    override fun getItem(position : Int) : Any {
        return entryList[position]
    }

    override fun getItemId(position : Int) : Long {
        return position.toLong()
    }

    override fun getView(position : Int, convertView : View?, parent : ViewGroup?) : View {
        val view:View = View.inflate(context, R.layout.history_adapter,null)
        val isMetric = Util.isMetric(context)
        val inputDesc = view.findViewById<TextView>(R.id.input_type)
        val inputDetail = view.findViewById<TextView>(R.id.input_details)
        val entry = entryList.get(position)
        var activityType = "Unknown"
        if (entry.activityType != -1){
            activityType = ACTIVITY_TYPES[entry.activityType]
        }
        val inputType = INPUT_TYPES[entry.inputType]
        val datetime = Util.dateParser(entry.dateTime)

        val desc:String = "$inputType: $activityType $datetime"
        val detail:String = Util.distanceParser(isMetric,entry.distance) +" "+ Util.durationParser(entry.duration)

        inputDesc.text = desc
        inputDetail.text = detail

        return view
    }

    fun replace(newList: List<ExerciseEntry>){
        entryList = newList
    }


}