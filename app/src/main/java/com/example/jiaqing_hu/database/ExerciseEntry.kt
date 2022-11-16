package com.example.jiaqing_hu.database


import androidx.room.*
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
    var comment : String = ""
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
}