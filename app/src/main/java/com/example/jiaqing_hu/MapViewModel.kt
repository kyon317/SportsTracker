package com.example.jiaqing_hu

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jiaqing_hu.TrackingService.Companion.LOCATION_KEY
import com.example.jiaqing_hu.database.ExerciseEntry
import com.google.android.gms.maps.model.LatLng

/* A map viewmodel that handles msg from background services */
class MapViewModel:ViewModel(),ServiceConnection {
    private var myMessageHandler: MessageHandler = MessageHandler(Looper.getMainLooper())
    private var _entry = MutableLiveData<ExerciseEntry>()
    val entry: LiveData<ExerciseEntry>
        get() {
            return _entry
        }

    override fun onServiceConnected(name : ComponentName?, service : IBinder?) {
        val tempBinder = service as TrackingService.MyBinder
        tempBinder.setmsgHandler(myMessageHandler)
        _entry.value = tempBinder.getEntry()
    }

    override fun onServiceDisconnected(name : ComponentName?) {

    }

    inner class MessageHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            if (msg.what == TrackingService.MSG_VALUE) {
                val bundle = msg.data
                _entry.value!!.locationList  = bundle.getSerializable(LOCATION_KEY) as ArrayList<LatLng>
            }
        }
    }
}