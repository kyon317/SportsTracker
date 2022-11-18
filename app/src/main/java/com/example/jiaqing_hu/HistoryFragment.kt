package com.example.jiaqing_hu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.jiaqing_hu.database.*

/* History Fragment - Show activity history with a listview */
class HistoryFragment : Fragment() {
    private lateinit var listView:ListView

    private lateinit var arrayList: ArrayList<ExerciseEntry>
    private lateinit var arrayAdapter: HistoryAdapter

    private lateinit var database: ExerciseEntryDatabase
    private lateinit var databaseDao: ExerciseEntryDatabaseDao
    private lateinit var repository: ExerciseEntryRepository
    private lateinit var viewModelFactory: ExerciseViewModelFactory
    private lateinit var commentViewModel: ExerciseEntryViewModel.CommentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.fragment_history, container, false)
        listView = view.findViewById(R.id.listview)

        // set up database
        arrayList = ArrayList()
        arrayAdapter = HistoryAdapter(requireActivity(), arrayList)
        listView.adapter = arrayAdapter

        database = ExerciseEntryDatabase.getInstance(requireActivity())
        databaseDao = database.exerciseEntryDatabaseDao
        repository = ExerciseEntryRepository(databaseDao)
        viewModelFactory = ExerciseViewModelFactory(repository)
        commentViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ExerciseEntryViewModel.CommentViewModel::class.java]
        // show data using view model
        commentViewModel.allCommentsLiveData.observe(requireActivity(), Observer { it ->
            arrayAdapter.replace(it)
            arrayAdapter.notifyDataSetChanged()
        })
        // set click listener for each item,
        // pass the selected item to showEntryActivity()
        listView.setOnItemClickListener { parent, view, position, id ->
            val element:ExerciseEntry = arrayAdapter.getItem(position) as ExerciseEntry
            showEntryActivity(view,element,position)
        }

        return view
    }

    // update listview when resumed
    override fun onResume() {
        super.onResume()
        commentViewModel.allCommentsLiveData.observe(requireActivity(), Observer { it ->
            arrayAdapter.replace(it)
            arrayAdapter.notifyDataSetChanged()
        })
    }

    // define launch behavior and pass data to Entry Activity
    private fun showEntryActivity(view : View, element:ExerciseEntry,position:Int){
        var activityType = "Unknown"
        if (element.activityType != -1)
            activityType = ACTIVITY_TYPES[element.activityType]
        val inputType = INPUT_TYPES[element.inputType]
        val datetime = Util.dateParser(element.dateTime)
        val duration = element.duration.toString()
        val distance = element.distance.toString()
        val calories = element.calorie.toString()
        val heartRate = element.heartRate.toString()
        var intent = Intent()
        when(element.inputType){
            0->{
                intent = Intent(view.context,EntryActivity::class.java)
            }
            1,2->{
                intent = Intent(view.context,MapActivity::class.java)
                intent.putExtra("history",true)
                intent.putExtra("locationList",element.locationList)
                intent.putExtra("index",position)
            }
        }
        intent.putExtra("id",element.id)
        intent.putExtra("inputType",inputType)
        intent.putExtra("activityType",activityType)
        intent.putExtra("dateTime",datetime)
        intent.putExtra("duration",duration)
        intent.putExtra("distance",distance)
        intent.putExtra("calories",calories)
        intent.putExtra("heartRate",heartRate)
        startActivity(intent)
    }
}