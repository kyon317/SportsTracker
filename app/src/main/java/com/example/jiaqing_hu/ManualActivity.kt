package com.example.jiaqing_hu

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.jiaqing_hu.database.*
import java.util.*
import java.util.Calendar.*

/*  Manual Input activity:
*   Allow users to input training data manually.
* */
class ManualActivity : AppCompatActivity() {
    private lateinit var saveBtn:Button
    private lateinit var cancelBtn:Button
    private lateinit var listView: ListView

    private lateinit var arrayList: ArrayList<ExerciseEntry>
    private lateinit var arrayAdapter: HistoryAdapter

    var entry : ExerciseEntry = ExerciseEntry()
    private lateinit var database: ExerciseEntryDatabase
    private lateinit var databaseDao: ExerciseEntryDatabaseDao
    private lateinit var repository: ExerciseEntryRepository
    private lateinit var viewModelFactory: ExerciseViewModelFactory
    private lateinit var commentViewModel: ExerciseEntryViewModel.CommentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual)
        saveBtn = findViewById(R.id.save_btn3)
        cancelBtn = findViewById(R.id.cancel_btn3)
        listView = findViewById(R.id.manual_input_items)

        // set up database connection
        arrayList = ArrayList()
        arrayAdapter = HistoryAdapter(this, arrayList)
        database = ExerciseEntryDatabase.getInstance(this)
        databaseDao = database.exerciseEntryDatabaseDao
        repository = ExerciseEntryRepository(databaseDao)
        viewModelFactory = ExerciseViewModelFactory(repository)
        commentViewModel = ViewModelProvider(this, viewModelFactory).get(
            ExerciseEntryViewModel.CommentViewModel::class.java)

        // update view model
        commentViewModel.allCommentsLiveData.observe(this, Observer { it ->
            arrayAdapter.replace(it)
            arrayAdapter.notifyDataSetChanged()
        })


        // set up listeners
        // buttons
        saveBtn.setOnClickListener(View.OnClickListener { v ->
            saveBtn(v)
        })
        cancelBtn.setOnClickListener(View.OnClickListener { v ->
            cancelBtn(v)
        })
        // listview item clicked
        listView.setOnItemClickListener { parent, view, position, id ->
            switchItems(view,position)
        }

    }

    /* switchItems:
    * pops different dialogs, based on listview positions */
    fun switchItems(view: View,position:Int){
        val Dialog = ManualDialog()
        val bundle = Bundle()
        bundle.putInt(ManualDialog.DIALOG_KEY, position)

        Dialog.arguments = bundle
        Dialog.show(supportFragmentManager, "manual dialog")

    }

    // setters
    fun inputTypeSet(type:Int){
        this.entry.inputType = type
    }
    fun activityTypeSet(type:Int){
        this.entry.activityType = type
    }
    fun dateSet(calendar : Calendar){
        this.entry.dateTime.set(YEAR,calendar.get(YEAR))
        this.entry.dateTime.set(MONTH,calendar.get(MONTH))
        this.entry.dateTime.set(DAY_OF_MONTH,calendar.get(DAY_OF_MONTH))
    }
    fun timeSet(calendar : Calendar){
        this.entry.dateTime.set(HOUR_OF_DAY,calendar.get(HOUR_OF_DAY))
        this.entry.dateTime.set(MINUTE,calendar.get(MINUTE))
    }
    fun durationSet(duration : Double){
        this.entry.duration = duration
    }
    fun distanceSet(distance : Double){
        this.entry.distance = distance
    }
    fun caloriesSet(calories : Double){
        this.entry.calorie = calories
    }
    fun heartRateSet(heartRate : Double){
        this.entry.heartRate = heartRate
    }

    // button behaviors
    fun saveBtn(view: View){
        inputTypeSet(intent.getIntExtra("inputted",0))
        activityTypeSet(intent.getIntExtra("activity",0))
        commentViewModel.insert(this.entry)
        finish()
    }
    fun cancelBtn(view: View){
        finish()
    }

}
