package com.example.jiaqing_hu

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instance
import weka.core.Instances
import java.io.File
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.ArrayBlockingQueue

/*Tracking services - provide current location info, parse location to mapviewmodel, make broadcast when location changes*/
class TrackingService:
    Service(),
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener,
    SensorEventListener {
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

    private val mFeatLen = ACCELEROMETER_BLOCK_CAPACITY + 2
    private lateinit var mFeatureFile: File
    private lateinit var mSensorManager: SensorManager
    private lateinit var mAccelerometer: Sensor
    private var mServiceTaskType = 0
    private lateinit var mLabel: String
    private lateinit var mDataset: Instances
    private lateinit var mClassAttribute: Attribute
    private lateinit var mAsyncTask: OnSensorChangedTask
    private lateinit var mAccBuffer: ArrayBlockingQueue<Double>
    private var isAuto = false


    override fun onCreate() {
        super.onCreate()
        exerciseEntry = ExerciseEntry()
        locationList = ArrayList()
        myTask = MyTask()
        timer = Timer()
        timer.scheduleAtFixedRate(myTask, 0, 1000L)
        showNotification()
        myBinder = MyBinder()
        mAccBuffer = ArrayBlockingQueue<Double>(ACCELEROMETER_BUFFER_CAPACITY)
    }

    // start google api client and request location updates
    // reference: https://stackoverflow.com/questions/28535703/best-way-to-get-user-gps-location-in-background-in-android
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST)

        if (intent!=null){
            isAuto = intent.getBooleanExtra("isAuto",false)
            exerciseEntry.activityType = intent.getIntExtra("type",-1)
        }
        if (isAuto) init()
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
        if (isAuto)
            intent.putExtra("activity", exerciseEntry.activityType)
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

    // continually infers the activity type using accelerometer data
    override fun onSensorChanged(event : SensorEvent) {
        if (isAuto){
            if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
                val m = Math.sqrt(
                    (event.values[0] * event.values[0] + event.values[1] * event.values[1] + (event.values[2]
                            * event.values[2])).toDouble()
                )
                // Inserts the specified element into this queue if it is possible
                // to do so immediately without violating capacity restrictions,
                // returning true upon success and throwing an IllegalStateException
                // if no space is currently available. When using a
                // capacity-restricted queue, it is generally preferable to use
                // offer.
                try {
                    mAccBuffer.add(m)
                } catch (e: IllegalStateException) {

                    // Exception happens when reach the capacity.
                    // Doubling the buffer. ListBlockingQueue has no such issue,
                    // But generally has worse performance
                    val newBuf = ArrayBlockingQueue<Double>(mAccBuffer.size * 2)
                    mAccBuffer.drainTo(newBuf)
                    mAccBuffer = newBuf
                    mAccBuffer.add(m)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor : Sensor?, accuracy : Int) {

    }

    inner class OnSensorChangedTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg arg0: Void?): Void? {
            val inst: Instance = DenseInstance(mFeatLen)
            inst.setDataset(mDataset)
            var blockSize = 0
            val fft = FFT(ACCELEROMETER_BLOCK_CAPACITY)
            val accBlock = DoubleArray(ACCELEROMETER_BLOCK_CAPACITY)
            val im = DoubleArray(ACCELEROMETER_BLOCK_CAPACITY)
            var max = Double.MIN_VALUE
            while (true) {
                try {
                    // need to check if the AsyncTask is cancelled or not in the while loop
                    if (isCancelled() == true) {
                        return null
                    }

                    // Dumping buffer
                    accBlock[blockSize++] = mAccBuffer.take().toDouble()
                    if (blockSize == ACCELEROMETER_BLOCK_CAPACITY) {
                        blockSize = 0

                        // time = System.currentTimeMillis();
                        max = .0
                        for (`val` in accBlock) {
                            if (max < `val`) {
                                max = `val`
                            }
                        }
                        fft.fft(accBlock, im)
                        for (i in accBlock.indices) {
                            val mag = Math.sqrt(accBlock[i] * accBlock[i] + im[i]
                                    * im[i])
                            inst.setValue(i, mag)
                            im[i] = .0 // Clear the field
                        }
                        // Append max after frequency component
                        inst.setValue(ACCELEROMETER_BLOCK_CAPACITY, max)
                        inst.setValue(mClassAttribute, mLabel)
                        mDataset.add(inst)
                        // User classifier to decide current activity type
                        val resultValue = WekaClassifier.classify(inst.toDoubleArray().toTypedArray()).toInt()
                        exerciseEntry.activityType = resultValue
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // model initialization
    fun init(){
        mLabel = AUTO_ACTIVITY_TYPES[3]
        when(exerciseEntry.activityType){
            0->{mLabel = AUTO_ACTIVITY_TYPES[2]}
            1->{mLabel = AUTO_ACTIVITY_TYPES[1]}
            2->{mLabel = AUTO_ACTIVITY_TYPES[0]}
        }

        // Create the container for attributes
        val allAttr = ArrayList<Attribute>()

        // Adding FFT coefficient attributes
        val df = DecimalFormat("0000")
        for (i in 0 until ACCELEROMETER_BLOCK_CAPACITY) {
            allAttr.add(Attribute(FEAT_FFT_COEF_LABEL + df.format(i.toLong())))
        }
        // Adding the max feature
        allAttr.add(Attribute(FEAT_MAX_LABEL))

        // Declare a nominal attribute along with its candidate values
        val labelItems = ArrayList<String>(3)
        labelItems.add(CLASS_LABEL_STANDING)
        labelItems.add(CLASS_LABEL_WALKING)
        labelItems.add(CLASS_LABEL_RUNNING)
        labelItems.add(CLASS_LABEL_OTHER)
        mClassAttribute = Attribute(CLASS_LABEL_KEY, labelItems)
        allAttr.add(mClassAttribute)

        // Construct the dataset with the attributes specified as allAttr and
        // capacity 10000
        mDataset = Instances(FEAT_SET_NAME, allAttr, FEATURE_SET_CAPACITY)

        // Set the last column/attribute (standing/walking/running) as the class
        // index for classification
        mDataset.setClassIndex(mDataset.numAttributes() - 1)

        mAsyncTask = OnSensorChangedTask()
        mAsyncTask.execute()
    }
}

