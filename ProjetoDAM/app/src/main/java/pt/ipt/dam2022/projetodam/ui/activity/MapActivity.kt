package pt.ipt.dam2022.projetodam.ui.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import pt.ipt.dam2022.projetodam.FunctionsUtil.requestPermissionsIfNecessary
import pt.ipt.dam2022.projetodam.R

class MapActivity : AppCompatActivity() {
    private lateinit var map : MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setContentView(R.layout.activity_map)
        requestPermissionsIfNecessary(arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
        ), this, this)


        Configuration.getInstance().userAgentValue = this.packageName
        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.controller.zoomTo(19.0)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
        map.setMultiTouchControls(true) // para poder fazer zoom com os dedos
        var compassOverlay = CompassOverlay(this, map)
        compassOverlay.enableCompass()
        map.overlays.add(compassOverlay)

        val locationProvider = GpsMyLocationProvider(this)
        myLocationOverlay = MyLocationNewOverlay(locationProvider, map)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        map.overlays.add(myLocationOverlay)

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // This callback is triggered when the user's location changes.
                // Update the map with the new location

                // Create a new MyLocationNewOverlay with the updated location
                val newMyLocationOverlay = MyLocationNewOverlay(locationProvider, map)
                newMyLocationOverlay.enableMyLocation()
                newMyLocationOverlay.enableFollowLocation()

                // Remove the existing MyLocationNewOverlay
                map.overlays.remove(myLocationOverlay)

                // Add the new MyLocationNewOverlay to the map
                map.overlays.add(newMyLocationOverlay)

                // Update the reference to the new overlay
                myLocationOverlay = newMyLocationOverlay

                val currentLocation = Location("Current Location")
                val myLocation = myLocationOverlay.myLocation
                if (myLocation != null) {
                    currentLocation.latitude = myLocation.latitude
                    currentLocation.longitude = myLocation.longitude
                }
                // Center the map on the current location
                map.controller.animateTo(
                    GeoPoint(
                        currentLocation.latitude,
                        currentLocation.longitude
                    )
                )

                // Refresh the map
                map.invalidate()

            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}
        }


        // Check for both coarse and fine location permissions
        val fineLocationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED && coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // Both permissions are granted, it can request location updates
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                10000,
                100.5f,
                locationListener
            )
        }else{
            requestPermissionsIfNecessary(arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ), this, this)
        }

    }



}