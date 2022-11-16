package com.example.jiaqing_hu

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapActivity  : AppCompatActivity(), OnMapReadyCallback, LocationListener{
    private lateinit var mMap: GoogleMap

    private val PERMISSION_REQUEST_CODE = 0
    private lateinit var locationManager: LocationManager

    private var mapCentered = false
    private lateinit var moveOptions: MarkerOptions
    private lateinit var  markerOptions: MarkerOptions
    private lateinit var  polylineOptions: PolylineOptions
    private lateinit var  polylines: ArrayList<Polyline>
    private lateinit var  dummy: Marker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    fun onSaveBtnClicked(view: View){
        finish()
    }
    fun onCancelBtnClicked(view: View){
        finish()
    }
    fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        else
            initLocationManager()
    }
    override fun onMapReady(googleMap : GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
//        mMap.setOnMapClickListener(this)
//        mMap.setOnMapLongClickListener(this)
        polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLACK)
        polylines = ArrayList()
        markerOptions = MarkerOptions()
        moveOptions = MarkerOptions()
        checkPermission()
    }


    fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            val provider : String? = locationManager.getBestProvider(criteria, true)
            if(provider != null) {
                val location = locationManager.getLastKnownLocation(provider)

                if (location != null){
                      onLocationChanged(location)
//                    val lat = location.latitude
//                    val lng = location.longitude
//                    val latLng = LatLng(lat, lng)
//                    if (!mapCentered) {
//                        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
//                        mMap.animateCamera(cameraUpdate)
//                        markerOptions.position(latLng)
//                        moveOptions.position(latLng)
//                        mMap.addMarker(markerOptions)
//                        dummy = mMap.addMarker(moveOptions)!!
//                        polylineOptions.add(latLng)
//                        mapCentered = true
//                    }
                }

                locationManager.requestLocationUpdates(provider, 0, 0f, this)
            }
        } catch (e: SecurityException) {
        }
    }

    override fun onLocationChanged(location : Location) {
        println("debug: onlocationchanged() $location")
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)
        if (!mapCentered) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
            mMap.animateCamera(cameraUpdate)
            markerOptions.position(latLng)
            moveOptions.position(latLng)
            mMap.addMarker(markerOptions)
            dummy = mMap.addMarker(moveOptions)!!
            polylineOptions.add(latLng)
            mapCentered = true
//            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
//            mMap.animateCamera(cameraUpdate)
//            markerOptions.position(latLng)
//            mMap.addMarker(markerOptions)
////            markerOptions.position(latLng)
//            polylineOptions.add(latLng)
//            mapCentered = true
        }
        dummy.remove()
        moveOptions.position(latLng)
        moveOptions.icon(BitmapDescriptorFactory.defaultMarker(120f))
        dummy = mMap.addMarker(moveOptions)!!
        polylineOptions.add(latLng)
        polylines.add(mMap.addPolyline(polylineOptions))
    }

//    override fun onMapClick(p0 : LatLng) {
//        for (i in polylines.indices) polylines[i].remove()
//        polylineOptions.points.clear()
//    }

//    override fun onMapLongClick(latLng : LatLng) {
//        markerOptions.position(latLng!!)
//        mMap.addMarker(markerOptions)
//        polylineOptions.add(latLng)
//        polylines.add(mMap.addPolyline(polylineOptions))
//    }

    override fun onSaveInstanceState(outState : Bundle, outPersistentState : PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        val bundle = Bundle()
        bundle.putSerializable("polylines",polylines)
        bundle.putParcelable("markeroptions",markerOptions)
        bundle.putParcelable("markeroptions",moveOptions)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(this)
    }
}
