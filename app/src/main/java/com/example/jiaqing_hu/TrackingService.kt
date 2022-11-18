package com.example.jiaqing_hu

//import com.google.android.gms.location.LocationListener
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.jiaqing_hu.database.ExerciseEntry
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.util.*

/*Tracking services - provide current location info, parse location to mapviewmodel, make broadcast when location changes*/
class TrackingService:Service(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {
    private lateinit var notificationManager: NotificationManager
    private val NOTIFICATION_ID = 777
    private val CHANNEL_ID = "notification channel"

    private lateinit var locationManager: LocationManager
    private lateinit var locationList : ArrayList<Location>


    private lateinit var  myBinder: MyBinder
    private var msgHandler: Handler? = null
    private lateinit var exerciseEntry : ExerciseEntry
    companion object{
        val LOCATION_KEY = "location key"
        val MSG_VALUE = 0
    }
    /////////
    private var counter = 0
    private lateinit var myTask: MyTask
    private lateinit var timer: Timer
    private lateinit var mLocationClient:GoogleApiClient
    private lateinit var mLocationRequest:LocationRequest
    private val mHandler = Handler()

    override fun onCreate() {
        super.onCreate()
        exerciseEntry = ExerciseEntry()
        locationList = ArrayList()
        myTask = MyTask()
        timer = Timer()
        timer.scheduleAtFixedRate(myTask, 0, 1000L)
        showNotification()
        myBinder = MyBinder()
    }

    // start google api client and request location updates
    // reference: https://stackoverflow.com/questions/28535703/best-way-to-get-user-gps-location-in-background-in-android
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent!=null)
            exerciseEntry.activityType = intent.getIntExtra("type",-1)
        mLocationClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        mLocationRequest = LocationRequest()
        mLocationRequest.setInterval(10000) //10 secs
        mLocationRequest.setFastestInterval(5000) //5 secs
        val priority = LocationRequest.PRIORITY_HIGH_ACCURACY //by default
        //PRIORITY_BALANCED_POWER_ACCURACY, PRIORITY_LOW_POWER, PRIORITY_NO_POWER are the other priority modes
        mLocationRequest.priority = priority
        mLocationClient.connect()

        return START_STICKY
    }

    // bind & unbind behaviors
    override fun onBind(intent: Intent?): IBinder? {
        println("debug: Service onBind() called")
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun setmsgHandler(msgHandler: Handler) {
            this@TrackingService.msgHandler = msgHandler
        }
        fun getEntry() : ExerciseEntry {
            return exerciseEntry
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        println("debug: Service onUnBind() called~~~")
        msgHandler = null
        return true
    }

    // clean running tasks
    override fun onDestroy() {
        super.onDestroy()
        cleanupTasks()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        println("debug: app removed from the application list")
        cleanupTasks()
        stopSelf()
    }

    // stop notification, timer, and google api client
    private fun cleanupTasks(){
        notificationManager.cancel(NOTIFICATION_ID)
        timer.cancel()
        counter = 0
        mLocationClient.disconnect();
    }

    // show notification
    private fun showNotification() {
        val intent = Intent(this, MapActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground)
        notificationBuilder.setContentTitle("Recording your path")
        notificationBuilder.setContentText("Tap me to go back")
        notificationBuilder.setContentIntent(pendingIntent)
        val notification = notificationBuilder.build()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "channel name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    // timer task
    inner class MyTask : TimerTask() {
        override fun run() {
            try {
                mHandler.post(Runnable { updateLocation() })
                counter += 1
                sendUpdate()

                if(msgHandler != null){
                    val bundle = Bundle()
                    bundle.putSerializable(LOCATION_KEY,exerciseEntry.locationList)
                    val message = msgHandler!!.obtainMessage()
                    message.data = bundle
                    message.what = MSG_VALUE
                    msgHandler!!.sendMessage(message)
                }
            } catch (t: Throwable) { // ultimately catch all exceptions in timer tasks.
                println("debug: Tracking services Failed. $t")
            }
        }
    }

    // update exerciseEntry when location changes
    override fun onLocationChanged(location : Location) {
        println("debug: onlocationchanged() $location")
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)

        var prevLoc = location
        if (locationList.size > 0)
            prevLoc = locationList[locationList.size-1]
        locationList.add(location)
        exerciseEntry.locationList.add(latLng)
        exerciseEntry.distance += location.distanceTo(prevLoc).toDouble()
        exerciseEntry.duration = counter.toDouble()
        exerciseEntry.avgSpeed = exerciseEntry.distance/exerciseEntry.duration
        exerciseEntry.avgPace = location.distanceTo(prevLoc).toDouble()
        exerciseEntry.climb = location.altitude - locationList[0].altitude
        exerciseEntry.calorie += exerciseEntry.avgSpeed * 0.05
        sendUpdate()
    }

    // update current location
    @SuppressLint("MissingPermission")
    private fun updateLocation(){
        mLocationClient.connect()
    }


    // send update to broadcast receiver
    private fun sendUpdate() {
        val intent = Intent(MapActivity.UpdateReceiver::class.java.name)
        intent.putExtra("update", true)
        sendBroadcast(intent)
    }



    // implement members for google api client connection
    @SuppressLint("MissingPermission")
    override fun onConnected(p0 : Bundle?) {
        Log.d(TAG, "Connected");
        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest,this)
    }

    override fun onConnectionSuspended(p0 : Int) {
        Log.d(TAG, "Connection suspended");
    }

    override fun onConnectionFailed(p0 : ConnectionResult) {
        Log.d(TAG, "Fail to Connect");
    }


}

