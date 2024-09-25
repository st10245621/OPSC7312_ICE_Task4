package com.example.landmarkapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.ScaleBarOverlay

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var currentLocation: GeoPoint

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // OSMDroid Configuration
        Configuration.getInstance().load(this, applicationContext.getSharedPreferences("osmdroid", MODE_PRIVATE))

        // Initialize MapView
        mapView = findViewById(R.id.map)
        mapView.setMultiTouchControls(true)

        // Set a default location if permissions are not granted
        val defaultLocation = GeoPoint(51.5074, -0.1278) // Default to London
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(defaultLocation)

        // Request location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            enableUserLocation()
        }

        // Add a compass overlay
        val compassOverlay = CompassOverlay(this, mapView)
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)

        // Add a scale bar overlay (make sure it's properly imported)
        val scaleBarOverlay = ScaleBarOverlay(mapView)
        scaleBarOverlay.setCentred(true)  // Centered scale bar
        scaleBarOverlay.setScaleBarOffset(100, 10)  // Adjust the offset
        mapView.overlays.add(scaleBarOverlay)

        // Add logic to handle map clicks for selecting landmarks
        mapView.setOnLongClickListener {
            val selectedLocation = mapView.projection.fromPixels(it.x.toInt(), it.y.toInt()) as GeoPoint
            calculateRouteToLandmark(selectedLocation)
            true
        }
    }

    // Enable user location if permissions are granted
    private fun enableUserLocation() {
        myLocationOverlay = MyLocationNewOverlay(mapView)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        mapView.overlays.add(myLocationOverlay)
    }

    // Calculate the route from the user's location to the selected landmark
    private fun calculateRouteToLandmark(landmark: GeoPoint) {
        // For this demonstration, we'll use a straight line as the "route"
        // Later, this can be replaced with a real routing algorithm using a web service
        val route = Polyline()
        route.addPoint(myLocationOverlay.myLocation)
        route.addPoint(landmark)
        mapView.overlays.add(route)

        // For now, display a simple distance estimate (in km)
        val distance = myLocationOverlay.myLocation.distanceToAsDouble(landmark) / 1000
        // Update this section later to also show time estimates

        // You can show this information to the user with a Toast or a UI update
        Toast.makeText(this, "Distance to destination: $distance km", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume() // Needed for osmdroid to resume
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause() // Needed for osmdroid to pause
    }
}
