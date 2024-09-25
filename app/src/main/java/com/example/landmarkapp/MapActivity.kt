package com.example.landmarkapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.ScaleBarOverlay

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var currentLocation: GeoPoint

    // Variables to store the last added marker and route
    private var lastMarker: Marker? = null
    private var lastRoute: Polyline? = null

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

        // Add a scale bar overlay
        val scaleBarOverlay = ScaleBarOverlay(mapView)
        scaleBarOverlay.setCentred(true)  // Centered scale bar
        scaleBarOverlay.setScaleBarOffset(100, 10)  // Adjust the offset
        mapView.overlays.add(scaleBarOverlay)

        // Handle tap gestures for selecting landmarks
        mapView.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                val selectedLocation = mapView.projection.fromPixels(motionEvent.x.toInt(), motionEvent.y.toInt()) as GeoPoint
                addMarker(selectedLocation)
                calculateRouteToLandmark(selectedLocation)
                true
            } else {
                false
            }
        }
    }

    // Enable user location if permissions are granted
    private fun enableUserLocation() {
        myLocationOverlay = MyLocationNewOverlay(mapView)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        mapView.overlays.add(myLocationOverlay)
    }

    // Add a marker to the selected landmark location
    private fun addMarker(location: GeoPoint) {
        // Remove the previous marker if it exists
        lastMarker?.let {
            mapView.overlays.remove(it)
        }

        val marker = Marker(mapView)
        marker.position = location
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Selected Landmark"
        mapView.overlays.add(marker)
        mapView.invalidate() // Refresh the map to display the marker

        lastMarker = marker // Save reference to the last marker
    }

    // Calculate the route from the user's location to the selected landmark and display the estimated time and distance
    private fun calculateRouteToLandmark(landmark: GeoPoint) {
        // Check if the user's current location is available
        val currentLocation = myLocationOverlay.myLocation

        if (currentLocation != null) {
            // Remove previous route if exists
            lastRoute?.let { mapView.overlays.remove(it) }

            // Create the route as a straight line between the user's location and the selected landmark
            val route = Polyline()
            route.addPoint(currentLocation)
            route.addPoint(landmark)
            mapView.overlays.add(route)
            mapView.invalidate() // Refresh the map to display the route

            lastRoute = route // Save reference to the last route

            // Calculate the distance to the landmark (in kilometers)
            val distance = currentLocation.distanceToAsDouble(landmark) / 1000

            // Calculate the estimated time (assuming an average walking speed of 5 km/h)
            val averageWalkingSpeedKmH = 5.0
            val estimatedTimeHours = distance / averageWalkingSpeedKmH
            val estimatedTimeMinutes = (estimatedTimeHours * 60).toInt()

            // Display the distance and estimated time in a Toast message
            Toast.makeText(this, "Distance: %.2f km, Estimated Time: %d min".format(distance, estimatedTimeMinutes), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Current location not available. Please wait for location to be fetched.", Toast.LENGTH_SHORT).show()
        }
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
