package pt.ipt.dam2022.projetodam.ui.activity

import android.Manifest
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import pt.ipt.dam2022.projetodam.FunctionsUtil.requestPermissionsIfNecessary
import pt.ipt.dam2022.projetodam.R


class MapActivity : AppCompatActivity() {
    private lateinit var map : MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var btnLocation: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setContentView(R.layout.activity_map)
        //Request user Permissions
        requestPermissionsIfNecessary(arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
        ), this, this)

        //Get the button to center the user location
        btnLocation = findViewById(R.id.btnLocation)

        //Set up the map with all the needed overlays
        Configuration.getInstance().userAgentValue = this.packageName
        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.controller.zoomTo(19.0)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
        map.setMultiTouchControls(true)
        val compassOverlay = CompassOverlay(this, map)
        compassOverlay.enableCompass()
        map.overlays.add(compassOverlay)
        val scaleBarOverlay = ScaleBarOverlay(map)
        scaleBarOverlay.setCentred(true)
        scaleBarOverlay.setScaleBarOffset(this.resources.displayMetrics.widthPixels / 2, 10)

        //Get the location Provider to add to the overlay that shows the user location
        val locationProvider = GpsMyLocationProvider(this)
        myLocationOverlay = MyLocationNewOverlay(locationProvider, map)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        map.overlays.add(myLocationOverlay)


        //Map listener that detects when the map is altered, and consequently the follow location is disabled
        val mapListener = object : MapListener {
            override fun onScroll(event: ScrollEvent): Boolean {
                if (!myLocationOverlay.isFollowLocationEnabled) {
                    btnLocation.setImageResource(R.drawable.ic_baseline_location_searching_24)
                }
                return true
            }

            override fun onZoom(event: ZoomEvent): Boolean {
                if (!myLocationOverlay.isFollowLocationEnabled) {
                    btnLocation.setImageResource(R.drawable.ic_baseline_location_searching_24)
                }
                return false
            }
        }

        // Add the MapListener to the MapView
        map.addMapListener(mapListener)



        //Change the button image and change the map actions
        btnLocation.setOnClickListener {
            if (!myLocationOverlay.isFollowLocationEnabled) {
                //If following the location is not enabled start following the user location
                myLocationOverlay.enableFollowLocation()
                btnLocation.setImageResource(R.drawable.ic_baseline_my_location_24)
            } else {
                //
                myLocationOverlay.disableFollowLocation()
                btnLocation.setImageResource(R.drawable.ic_baseline_location_searching_24)
            }
        }


    }



}