package com.example.jiaqing_hu


import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.jiaqing_hu.database.*

/* Entry Activity - Show details of a specific activity,
*  Allows user to delete the selected activity.
* */
class EntryActivity : AppCompatActivity() {
    private var entryID:Long = 0
    private lateinit var arrayList: ArrayList<ExerciseEntry>
    private lateinit var arrayAdapter: HistoryAdapter

    private lateinit var database: ExerciseEntryDatabase
    private lateinit var databaseDao: ExerciseEntryDatabaseDao
    private lateinit var repository: ExerciseEntryRepository
    private lateinit var viewModelFactory: ExerciseViewModelFactory
    private lateinit var commentViewModel: ExerciseEntryViewModel.CommentViewModel

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        // load database
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

        // fetch data from intent
        val extras = intent.extras
        if (extras != null) {
            entryID = extras.getLong("id")
            (findViewById<View>(R.id.input_type) as EditText).setText(extras.getString("inputType"))
            (findViewById<View>(R.id.activity_type_hist) as EditText).setText(extras.getString("activityType"))
            (findViewById<View>(R.id.date_time_hist) as EditText).setText(extras.getString("dateTime"))
            (findViewById<View>(R.id.duration_hist) as EditText).setText(extras.getString("duration"))
            (findViewById<View>(R.id.distance_hist) as EditText).setText(extras.getString("distance"))
            (findViewById<View>(R.id.calories_hist) as EditText).setText(extras.getString("calories"))
            (findViewById<View>(R.id.heart_rate_hist) as EditText).setText(extras.getString("heartRate"))
        }
    }

    // define menu button
    // reference:https://developer.android.com/develop/ui/views/components/menus
    override fun onCreateOptionsMenu(menu : Menu?) : Boolean {
        val deletebtn : MenuItem = menu!!.add(0, 0, 0, "Delete")
        deletebtn.setShowAsAction(2)
        return super.onCreateOptionsMenu(menu)
    }

    // define menu button behavior
    // reference:https://developer.android.com/develop/ui/views/components/menus
    override fun onOptionsItemSelected(item : MenuItem) : Boolean {
        if (item.itemId == 0) {
            repository.delete(entryID)
            finish()
        }
        else finish()
        return super.onOptionsItemSelected(item)
    }
}