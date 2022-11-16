package com.example.jiaqing_hu

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

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
val INPUT_TYPES = arrayOf("Manual Entry", "GPS", "Automatic")
object Util {
    fun checkPermissions(activity: Activity?) {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 0)
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


}
