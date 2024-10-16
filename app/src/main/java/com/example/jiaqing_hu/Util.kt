package com.example.jiaqing_hu

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import java.text.DecimalFormat
import java.util.*
import kotlin.math.abs


// Global variables
val ACTIVITY_TYPES = arrayOf(
    "Running",
    "Walking",
    "Standing",
    "Cycling",
    "Hiking",
    "Downhill Skiing",
    "Cross-Country Skiing",
    "Snowboarding",
    "Skating",
    "Swimming",
    "Mountain Biking",
    "Wheelchair",
    "Elliptical",
    "Other"
)
val AUTO_ACTIVITY_TYPES = arrayOf(
    "Standing",
    "Walking",
    "Running",
    "Others"
)
val INPUT_TYPES = arrayOf("Manual Entry", "GPS", "Automatic")
val dec = DecimalFormat("#.##")
val dec_time = DecimalFormat("##")
const val ACCELEROMETER_BUFFER_CAPACITY = 2048
const val ACCELEROMETER_BLOCK_CAPACITY = 64

const val ACTIVITY_ID_STANDING = 0
const val ACTIVITY_ID_WALKING = 1
const val ACTIVITY_ID_RUNNING = 2
const val ACTIVITY_ID_OTHER = 2

const val SERVICE_TASK_TYPE_COLLECT = 0
const val SERVICE_TASK_TYPE_CLASSIFY = 1

const val ACTION_MOTION_UPDATED = "MYRUNS_MOTION_UPDATED"

const val CLASS_LABEL_KEY = "label"
const val CLASS_LABEL_STANDING = "Standing"
const val CLASS_LABEL_WALKING = "Walking"
const val CLASS_LABEL_RUNNING = "Running"
const val CLASS_LABEL_OTHER = "Others"

const val FEAT_FFT_COEF_LABEL = "fft_coef_"
const val FEAT_MAX_LABEL = "max"
const val FEAT_SET_NAME = "accelerometer_features"

const val FEATURE_FILE_NAME = "features.arff"
const val RAW_DATA_NAME = "raw_data.txt"
const val FEATURE_SET_CAPACITY = 10000


object Util {
    fun checkPermissions(activity: Activity?) {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,Manifest.permission.ACCESS_FINE_LOCATION), 0)
        }
    }

    fun getBitmap(context: Context, imgUri: Uri): Bitmap {
        var bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imgUri))
        val matrix = Matrix()

        matrix.setRotate(90f)
        var ret = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return ret
    }

    // Helper function to parse calender into date format
    fun dateParser(calendar : Calendar):String{
        val year = calendar.get(Calendar.YEAR).toString()
        val month = (1+calendar.get(Calendar.MONTH)).toString()
        val day = calendar.get(Calendar.DAY_OF_MONTH).toString()
        val time = calendar.get(Calendar.HOUR).toString() +":"+ calendar.get(Calendar.MINUTE).toString()+":"+calendar.get(Calendar.SECOND)
        return "$time $month/$day/$year"
    }

    // Helper function to parse speed
    fun speedParser(isMetric:Boolean,speed:Double): String {
        var res = 0.0
        var unit = " km/h"
        if (isMetric){
            res = abs(speed*3.6)
        }
        else{
            res = abs(speed*2.237)
            unit = " mile/h"
        }
        return dec.format(res) + unit
    }

    // Helper function to parse distance
    fun distanceParser(isMetric:Boolean,distance:Double): String {
        var res = 0.0
        var unit = " km"
        if (isMetric){
            res = abs(distance/1000)
        }
        else{
            res = abs(distance/1609)
            unit = " miles"
        }
        return dec.format(res) + unit
    }

    // Helper function to parse calorie
    fun calorieParser(calorie:Double):String{
        return dec.format(calorie)
    }

    // Helper function to parse duration
    fun durationParser(duration : Double) : String {
        val hours = duration / 3600;
        val minutes = ((duration % 3600) / 60);
        val seconds = duration % 60;
        return "${dec_time.format(hours)}:${dec_time.format(minutes)}:${dec_time.format(seconds)}"
    }

    // check unit preferences
    fun isMetric(context : Context) :Boolean{
        val settings : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val options = context.resources.getStringArray(R.array.unit_preferences)
        val selected = settings.getString(context.getString(R.string.unit_preferences),options[0])
        if (selected == "Metric (Kilometers)") return true
        return false
    }
}
