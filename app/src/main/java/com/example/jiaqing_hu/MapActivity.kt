package com.example.jiaqing_hu

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.jiaqing_hu.database.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlin.properties.Delegates

/* Map Activity - Activity to display a google map
*  Mode 1: Recording path and updating UI dynamically, allow users to save or discard current exercise;
*  Mode 2: History mode, allow users to check previously saved path.
* */
class MapActivity  : AppCompatActivity(), OnMapReadyCallback{
    private lateinit var mMap: GoogleMap

    private val PERMISSION_REQUEST_CODE = 0
    private var isHistory = false

    private lateinit var moveOptions: MarkerOptions
    private lateinit var  markerOptions: MarkerOptions
    private lateinit var  polylineOptions: PolylineOptions
    private lateinit var  polylines: ArrayList<Polyline>
    private lateinit var  polyline:Polyline
    private lateinit var  originMarker: Marker
    private lateinit var  endMarker: Marker

    private lateinit var trackingIntent : Intent
    private var isBind:Boolean = false
    private var isAuto:Boolean = false
    private lateinit var mapViewModel : MapViewModel

    private var inputType by Delegates.notNull<Int>()
    private lateinit var activityType_tv:TextView
    private lateinit var avgSpeed_tv:TextView
    private lateinit var currSpeed_tv:TextView
    private lateinit var climb_tv:TextView
    private lateinit var calorie_tv:TextView
    private lateinit var distance_tv:TextView
    private lateinit var saveBtn:Button
    private lateinit var cancelBtn:Button
    private var isMetric = false
    private var activityType:String = "Unknown"

    private var entry : ExerciseEntry = ExerciseEntry()
    private lateinit var updateReceiver : UpdateReceiver

    private lateinit var arrayList: java.util.ArrayList<ExerciseEntry>
    private lateinit var arrayAdapter: HistoryAdapter

    private lateinit var database: ExerciseEntryDatabase
    private lateinit var databaseDao: ExerciseEntryDatabaseDao
    private lateinit var repository: ExerciseEntryRepository
    private lateinit var viewModelFactory: ExerciseViewModelFactory
    private lateinit var commentViewModel: ExerciseEntryViewModel.CommentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // set up database connection
        arrayList = java.util.ArrayList()
        arrayAdapter = HistoryAdapter(this, arrayList)
        database = ExerciseEntryDatabase.getInstance(this)
        databaseDao = database.exerciseEntryDatabaseDao
        repository = ExerciseEntryRepository(databaseDao)
        viewModelFactory = ExerciseViewModelFactory(repository)
        commentViewModel = ViewModelProvider(this, viewModelFactory)[ExerciseEntryViewModel.CommentViewModel::class.java]

        // update view model
        commentViewModel.allCommentsLiveData.observe(this, Observer { it ->
            arrayAdapter.replace(it)
            arrayAdapter.notifyDataSetChanged()
        })

        // check activity type, set run mode
        inputType = intent.getIntExtra("inputted", -1)
        if (inputType == 2) isAuto = true
        isHistory = intent.getBooleanExtra("history",false)
        val activity = intent.getIntExtra("activity",-1)
        if (activity != -1){
            activityType = ACTIVITY_TYPES[activity]
        }

        // initialize map ui
        init()
        isMetric = Util.isMetric(this.applicationContext)
        if (savedInstanceState != null){
            isBind = savedInstanceState.getBoolean("BIND_STATUS")
        }
        mapViewModel = ViewModelProvider(this)[MapViewModel::class.java]

        // check if it is history mode
        if (isHistory){
            saveBtn.isVisible= false
            cancelBtn.isVisible = false
            val id = intent.getLongExtra("id",-1L)
            if (id != -1L){
                entry.id = id
                var index = intent.getIntExtra("index",0)
                commentViewModel.allCommentsLiveData.observe(this) {
                    if (it.isNotEmpty()){
                        if (index >= it.size) index = it.size-1
                        setEntry(it[index])
                        setPath()
                        updateInfo()
                    }
                }
            }
        }else{
            trackingIntent = Intent(this.applicationContext,TrackingService::class.java)
            trackingIntent.putExtra("type",activity)
            trackingIntent.putExtra("isAuto",isAuto)
            // Register updateReceiver
            updateReceiver = UpdateReceiver()
            registerReceiver(updateReceiver, IntentFilter(updateReceiver::class.java.name))
            mapViewModel.entry.observe(this, Observer {
                setEntry(it as ExerciseEntry)
            })
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }
    // define menu button
    // reference:https://developer.android.com/develop/ui/views/components/menus
    override fun onCreateOptionsMenu(menu : Menu?) : Boolean {
        if (isHistory){
            val deletebtn : MenuItem = menu!!.add(0, 0, 0, "Delete")
            deletebtn.setShowAsAction(2)
        }
        return super.onCreateOptionsMenu(menu)
    }

    // define menu button behavior
    // reference:https://developer.android.com/develop/ui/views/components/menus
    override fun onOptionsItemSelected(item : MenuItem) : Boolean {
        if (item.itemId == 0) {
            repository.delete(entry.id)
            commentViewModel.allCommentsLiveData.observe(this, Observer { it ->
                arrayAdapter.replace(it)
                arrayAdapter.notifyDataSetChanged()
            })
            finish()
        }
        else finish()
        return super.onOptionsItemSelected(item)
    }

    // define back press behavior
    // should unbind and finish
    override fun onBackPressed() {
        trackingIntent = Intent(this.applicationContext,TrackingService::class.java)
        stopService(trackingIntent)
        unbindService()
        finish()
//        unregisterReceiver(updateReceiver)
        super.onBackPressed()
    }

    // initialize ui components
    private fun init(){
        activityType_tv = findViewById(R.id.ActivityType_tv)
        avgSpeed_tv = findViewById(R.id.AvgSpeed_tv)
        currSpeed_tv = findViewById(R.id.CurrSpeed_tv)
        climb_tv = findViewById(R.id.Climb_tv)
        calorie_tv = findViewById(R.id.Calorie_tv)
        distance_tv = findViewById(R.id.Distance_tv)
        saveBtn = findViewById(R.id.map_save_btn)
        cancelBtn = findViewById(R.id.map_cancel_btn)
    }



    // set up entry
    private fun setEntry(entry : ExerciseEntry){
        if (entry!=this.entry){
            this.entry = entry
        }

    }

    // save current exercise to database
    fun onSaveBtnClicked(view: View){
        entry.inputType = inputType
        commentViewModel.insert(this.entry)
        this.stopService(trackingIntent)
        unbindService()
        finish()
    }

    // discard current exercise
    fun onCancelBtnClicked(view: View){
        this.stopService(trackingIntent)
        unbindService()
        finish()
    }


    // check permissions
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        else
            initMap()
    }


    // initialize map
    private fun initMap(){
        originMarker = mMap.addMarker(MarkerOptions().position(LatLng(0.0, 0.0)))!!
        endMarker = mMap.addMarker(MarkerOptions().position(LatLng(0.0, 0.0)))!!
        if (!isHistory){
            this.startService(trackingIntent)
            bindService()
        }
        polyline = mMap.addPolyline(PolylineOptions())
    }

    // draw a path from point list
    fun setPath() {
        if (entry != null && entry.locationList.size !== 0) {
            val path = entry.locationList
            val origin = path[0]
            val end = path[path.size-1]
            markerOptions.position(origin)
            originMarker= mMap.addMarker(markerOptions)!!

            polyline.points = path
            if (endMarker != null) {
                endMarker.position = end
            } else {
                moveOptions.position(end)
                endMarker = mMap.addMarker(moveOptions)!!
            }
            reCenter(end)
        }
    }

    // recenter camera to current location
    private fun reCenter(location : LatLng) {
        var needRecenter = false
        val vr = mMap.projection.visibleRegion
        if (!vr.latLngBounds.contains(location)) needRecenter = true
        if (needRecenter) {
            val update = CameraUpdateFactory.newLatLngZoom(location, 17.0f)
            mMap.animateCamera(update)
        }
    }

    // update exercise info
    fun updateInfo() {
        if (entry!= null) {
            var type = "Type: " + "Unknown"
            if (isAuto and !isHistory) {
                val dummy = intent.getIntExtra("activity",-1)
                type = if (dummy != -1) "Type: " + AUTO_ACTIVITY_TYPES[entry.activityType]
                else "Type: " + "Unknown"
            }else if (entry.activityType != -1){
                type = if (isAuto) "Type: " + AUTO_ACTIVITY_TYPES[entry.activityType]
                else "Type: " +ACTIVITY_TYPES[entry.activityType]
            }
            val avg_speed = "Avg speed: " + Util.speedParser(isMetric, entry.avgSpeed)
            val cur_speed = "Cur speed: " + Util.speedParser(isMetric, entry.avgPace)
            val climb = "Climb: " + Util.distanceParser(isMetric,entry.climb)
            val calorie = "Calorie: " + Util.calorieParser(entry.calorie)
            val distance = "Distance: " + Util.distanceParser(isMetric,entry.distance)
            activityType_tv.text = type
            avgSpeed_tv.text = avg_speed
            currSpeed_tv.text = cur_speed
            climb_tv.text = climb
            calorie_tv.text = calorie
            distance_tv.text = distance
        }
    }

    // initialize markers and polylines
    override fun onMapReady(googleMap : GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLACK)
        polylines = ArrayList()
        markerOptions = MarkerOptions()
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(120f))
        moveOptions = MarkerOptions()
        checkPermission()
    }

    // save binding status
    override fun onSaveInstanceState(outState : Bundle, outPersistentState : PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putBoolean("BIND_STATUS", isBind)
    }


    override fun onResume() {
        super.onResume()
        mapViewModel.entry.observe(this){
        }
    }

    // bind & unbind services
    private fun bindService(){
        if (!isBind){
            this.applicationContext.bindService(trackingIntent, mapViewModel, Context.BIND_AUTO_CREATE)
            isBind = true
        }
    }
    private fun unbindService(){
        if (isBind){
            this.applicationContext.unbindService(mapViewModel)
            isBind = false
            unregisterReceiver(updateReceiver)
        }
    }

    // a broadcast receiver to update markers and paths
    // reference: https://developer.android.com/reference/android/content/BroadcastReceiver
    inner class UpdateReceiver : BroadcastReceiver() {
        override fun onReceive(context : Context, intent : Intent) {
            this@MapActivity.setPath()
            this@MapActivity.updateInfo()
        }
    }

}
