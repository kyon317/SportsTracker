package com.example.jiaqing_hu

import android.content.Context
import android.icu.util.Calendar
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.jiaqing_hu.database.ExerciseEntry
import java.time.Month
import java.time.Year

class HistoryAdapter(private val context: Context, private var entryList: List<ExerciseEntry>):BaseAdapter() {
    override fun getCount() : Int {
        return entryList.size
    }

    override fun getItem(position : Int) : Any {
        return entryList.get(position)
    }

    override fun getItemId(position : Int) : Long {
        return position.toLong()
    }

    override fun getView(position : Int, convertView : View?, parent : ViewGroup?) : View {
        val view:View = View.inflate(context, R.layout.history_adapter,null)
        val inputDesc = view.findViewById<TextView>(R.id.input_type)
        val inputDetail = view.findViewById<TextView>(R.id.input_details)
        val entry = entryList.get(position)
        val activityType = ACTIVITY_TYPES[entry.activityType]
        val inputType = INPUT_TYPES[entry.inputType]
        val datetime = Util.dateParser(entry.dateTime)

        val desc:String = "$inputType: $activityType $datetime"
        val detail:String = entry.distance.toString() + " Miles, " + entry.duration.toString() +"secs"

        inputDesc.text = desc
        inputDetail.text = detail

        return view
    }

    fun replace(newList: List<ExerciseEntry>){
        entryList = newList
    }

}