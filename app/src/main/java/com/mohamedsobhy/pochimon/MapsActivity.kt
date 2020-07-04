package com.mohamedsobhy.pochimon

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.Exception

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        loadPockimon()
        checkPermissions()
    }


    val accessLocation = 123

    private fun checkPermissions() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), accessLocation)

                return
            }
        }
        getUserLocation()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            accessLocation -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getUserLocation()
                } else {
                    Toast.makeText(this, "location access is deny", Toast.LENGTH_SHORT).show()
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun getUserLocation() {
        Toast.makeText(this, "location access now", Toast.LENGTH_SHORT).show()

        val myLocation = MyLocationListener()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3, 3f, myLocation)

        val myThread = MyThread()
        myThread.start()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


    }

    var myLocation: Location? = null

    inner class MyLocationListener : LocationListener {

        constructor() {
            myLocation = Location("me")
            myLocation!!.longitude = 0.0
            myLocation!!.latitude = 0.0

        }

        override fun onLocationChanged(location: Location?) {
            myLocation = location
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String?) {
        }

        override fun onProviderDisabled(provider: String?) {
        }
    }

    var oldLocation:Location?=null

    inner class MyThread : Thread {

        constructor() : super() {
            oldLocation = Location("me")
            oldLocation!!.longitude = 0.0
            oldLocation!!.latitude = 0.0
        }

        override fun run() {

            while (true) {

                try {

                    if (oldLocation!!.distanceTo(myLocation) == 0f){
                        continue
                    }

                    oldLocation = myLocation
                    runOnUiThread {
                        mMap!!.clear()
                        // Add a marker in Sydney and move the camera
                        val sydney = LatLng(myLocation!!.latitude , myLocation!!.longitude )
                        mMap.addMarker(MarkerOptions().position(sydney).title("Me")
                                .snippet("here is my location")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mario)))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14f))

                        // show pockimon
                        for (i in 0 until listOfPockimon.size){
                            var newPockimon = listOfPockimon[i]

                            if (newPockimon.isCatch == false){
                                val pockLocation = LatLng(newPockimon.location!!.latitude , newPockimon.location!!.longitude )
                                mMap.addMarker(MarkerOptions().position(pockLocation).title(newPockimon.name)
                                    .snippet(newPockimon.description + ", "+newPockimon.power)
                                    .icon(BitmapDescriptorFactory.fromResource(newPockimon.image!!)))

                                if (myLocation!!.distanceTo(newPockimon.location) <2){
                                    myPower += newPockimon.power!!
                                    newPockimon.isCatch = true
                                    listOfPockimon[i] = newPockimon
                                    Toast.makeText(applicationContext,
                                        "You catch new pockimon , your new power is $myPower", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    Thread.sleep(1000)
                } catch (e : Exception){ }

            }
        }

    }

    var myPower:Double = 0.0

    var listOfPockimon=ArrayList<Pockimon>()

    private fun loadPockimon(){
        listOfPockimon.add(Pockimon(R.drawable.charmander , "charmander" , "charmander living in japan" ,
            55.0 ,30.033519 , 31.210554))

        listOfPockimon.add(Pockimon(R.drawable.bulbasaur, "bulbasaur" , "bulbasaur living in usa" ,
            90.5 ,30.049346, 31.204568))

        listOfPockimon.add(Pockimon(R.drawable.squirtle, "squirtle" , "squirtle living in iraq" ,
            33.5 ,30.040761, 31.205260))

    }
}