package com.example.jiaqing_hu.database


import androidx.room.*
import com.google.android.gms.maps.model.LatLng
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.util.*

/* ExerciseEntry - A data class for exercise entry
id: Long //Primary Key
inputType: Int  // Manual, GPS or automatic
activityType: Int    // Running, cycling etc.
dateTime: Calendar    // When does this entry happen
duration: Double       // Exercise duration in seconds
distance: Double      // Distance traveled. Either in meters or feet.
avgPace: Double       // Average pace
avgSpeed: Double     // Average speed
calorie: Double        // Calories burnt
climb: Double         // Climb. Either in meters or feet.
heartRate: Double       // Heart rate
comment: String       // Comments
locationList: ArrayList<LatLng>  // Location list
*/
@Entity(tableName = "exercise_table")
@TypeConverters(Converters::class)
data class ExerciseEntry(
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0L,

    @ColumnInfo(name = "inputType")
    var inputType : Int = 0,

    @ColumnInfo(name = "activityType")
    var activityType : Int = 0,

    @ColumnInfo(name = "dateTime")
    var dateTime : Calendar = Calendar.getInstance(),

    @ColumnInfo(name = "duration")
    var duration : Double = 0.0,

    @ColumnInfo(name = "distance")
    var distance : Double = 0.0,

    @ColumnInfo(name = "avgPace")
    var avgPace : Double = 0.0,

    @ColumnInfo(name = "avgSpeed")
    var avgSpeed : Double = 0.0,

    @ColumnInfo(name = "calorie")
    var calorie : Double = 0.0,

    @ColumnInfo(name = "climb")
    var climb : Double = 0.0,

    @ColumnInfo(name = "heartRate")
    var heartRate : Double = 0.0,

    @ColumnInfo(name = "comments")
    var comment : String = "",

    @ColumnInfo(name = "locationList")
    var locationList : ArrayList<LatLng> = ArrayList()


)



class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Calendar? {
        val calendar = Calendar.getInstance()
        if (value != null) {
            calendar.timeInMillis = value
        }
        return calendar
    }

    @TypeConverter
    fun dateToTimestamp(date: Calendar?): Long {
        if (date != null) {
            return date.timeInMillis
        }
        return 0L
    }

    @TypeConverter
    fun locationToByteArray(locationList:ArrayList<LatLng>) : ByteArray? {
        val intArray = IntArray(locationList.size * 2)
        for (i in 0 until locationList.size) {
            intArray[i * 2] = (locationList[i].latitude * 1000000.0).toInt()
            intArray[i * 2 + 1] = (locationList[i].longitude * 1000000.0).toInt()
        }
        val byteBuffer : ByteBuffer = ByteBuffer.allocate(intArray.size * 32)
        val intBuffer : IntBuffer = byteBuffer.asIntBuffer()
        intBuffer.put(intArray)
        return byteBuffer.array()
    }

    @TypeConverter
    fun byteArrayToLocation(bytePointArray : ByteArray): ArrayList<LatLng>{
        val locationList = ArrayList<LatLng>()
        val byteBuffer : ByteBuffer = ByteBuffer.wrap(bytePointArray)
        val intBuffer : IntBuffer = byteBuffer.asIntBuffer()
        val intArray = IntArray(bytePointArray.size / 32)
        intBuffer.get(intArray)
        val locationNum = intArray.size / 2
        for (i in 0 until locationNum) {
            val latLng = LatLng(intArray[i * 2] / 1000000.0, intArray[i * 2 + 1] / 1000000.0)
            locationList.add(latLng)
        }
        return locationList
    }

}