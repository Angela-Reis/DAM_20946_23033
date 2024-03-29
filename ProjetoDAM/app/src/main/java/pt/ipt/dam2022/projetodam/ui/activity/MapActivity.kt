package pt.ipt.dam2022.projetodam.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.*
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import pt.ipt.dam2022.projetodam.R
import pt.ipt.dam2022.projetodam.model.overpass.OverpassElement
import pt.ipt.dam2022.projetodam.model.overpass.OverpassResponse
import pt.ipt.dam2022.projetodam.retrofit.RetrofitOverpass
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class MapActivity : AppCompatActivity() {
    private lateinit var routeLayout: LinearLayout
    private lateinit var infoRoad: TextView
    private var showDirections = false
    private lateinit var permissions: Array<String>
    private var locationRoute: GeoPoint? = null
    private lateinit var map: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var btnLocation: ImageButton
    private lateinit var store: String
    private lateinit var roadManager: RoadManager
    private var roadOverlay: Polyline? = null
    private var waypoints: ArrayList<GeoPoint>? = null
    private var findNearest: Boolean = true
    private var locationResponse: OverpassResponse? = null
    private lateinit var instructionsMarkers: ArrayList<Marker>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instructionsMarkers = ArrayList()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_map)

        //Get the routeLayout that is going to show if there is a route
        routeLayout = findViewById(R.id.routeLayout)

        //Get the textView that will show the length of the route
        infoRoad = findViewById(R.id.infoRoad)
        val directionsBtn = findViewById<Button>(R.id.btnDirections)
        directionsBtn.setOnClickListener {
            showDirections = !showDirections
            if (showDirections) {
                directionsBtn.text = resources.getString(R.string.stop_show_route)
                //If it's not following the path click the user and start following
                if (!myLocationOverlay.isFollowLocationEnabled) {
                    btnLocation.performClick()
                }
            } else {
                directionsBtn.text = resources.getString(R.string.show_route)
                //Close all the opened route info windows
                for (marker in instructionsMarkers) {
                    if (marker.isInfoWindowShown) {
                        marker.closeInfoWindow()
                    }
                }
            }
        }

        // Create a roadManager instance using the OSRMRoadManager
        roadManager = OSRMRoadManager(applicationContext)
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


        //Define the store name to search
        val intent = intent // Get the Intent that started this activity
        // Retrieve the value associated with the key "Store"
        store = intent.getStringExtra("Store").toString()

        permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        //Request user Permissions
        requestPermissionsIfNecessary(
            permissions
        )

    }

    /**
     * function to collect user permission
     */
    private fun requestPermissionsIfNecessary(permissions: Array<out String>) {
        val permissionsToRequest = java.util.ArrayList<String>()
        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(
                    this, permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this, permissionsToRequest.toArray(arrayOf<String>()), 1
            )
        } else {
            //If there are no Permissions Requested
            setUpMap()
        }
    }

    /**
     * Function that is triggered when the user gives a result to the request for permission
     * This function detects if the user did not give the location permission and closes the map activity
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            for (i in permissions.indices) {
                val grantResult = grantResults[i]
                //If the user did not give permission finish the activity
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    finish()
                }
            }
            setUpMap()
        }
    }


    private fun setUpMap() {

        //Find all locations of the store, make request to api
        findLocations()


        //Get the location Provider to add to the overlay that shows the user location
        val locationProvider = GpsMyLocationProvider(this)
        //Create the myLocationOverLay and add a LocationListener to it
        myLocationOverlay = object : MyLocationNewOverlay(locationProvider, map) {
            override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
                super.onLocationChanged(location, source)
                if (findNearest) {
                    routeToNearest()
                } else {
                    waypoints?.get(1)?.let { roadRoute(location, it) }
                }
                if (instructionsMarkers.size > 0) {
                    if (showDirections) {
                        openClosestInfoWindow(location)
                    }
                }
            }
        }
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        map.overlays.add(myLocationOverlay)

        //Get the button to center the user location
        btnLocation = findViewById(R.id.btnLocation)

        //Change the button image and change the map actions
        btnLocation.setOnClickListener {
            if (!myLocationOverlay.isFollowLocationEnabled) {
                myLocationOverlay.enableMyLocation()
                //If following the location is not enabled start following the user location
                myLocationOverlay.enableFollowLocation()
                btnLocation.setImageResource(R.drawable.ic_baseline_my_location_24)
            } else {
                //
                myLocationOverlay.disableFollowLocation()
                btnLocation.setImageResource(R.drawable.ic_baseline_location_searching_24)
            }
        }

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
    }


    /**
     * Set up display of route to nearest location
     */
    private fun routeToNearest() {
        val currentLocation = Location("Current Location")
        val myLocation = myLocationOverlay.myLocation
        if (myLocation != null) {
            currentLocation.latitude = myLocation.latitude
            currentLocation.longitude = myLocation.longitude
        }

        if (locationResponse == null) {
            //Find all locations of the store, make request to api
            findLocations()
        }
        val nearestStore = locationResponse?.let {
            findNearestStore(
                currentLocation, it.elements
            )
        }

        if (nearestStore != null) {
            roadRoute(currentLocation, nearestStore)
        }
    }

    /**
     * access api with the call specified in queryOverpass
     */
    private fun findLocations() {
        //Query to search the location using the overpass API
        val query = """
                [out:json];
                area[name="Portugal"]->.searchArea;
                (node["name"="$store"](area.searchArea);
                way["name"="$store"](area.searchArea););
                out center;
            """.trimIndent()

        //
        val call = RetrofitOverpass(this).overpassService().queryOverpass(query)
        processLocations(call)
    }

    /**
     * add the Locations to the interface
     */
    private fun processLocations(call: Call<OverpassResponse>) {
        call.enqueue(object : Callback<OverpassResponse> {
            override fun onResponse(
                call: Call<OverpassResponse>, response: Response<OverpassResponse>
            ) {
                if (response.isSuccessful) {

                    // Check if the response is not null and contains elements
                    if (response.body() != null && response.body()!!.elements.isNotEmpty()) {
                        locationResponse = response.body()!!
                        setMarkers()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            applicationContext.getString(R.string.no_store_locations),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e(
                        "OverpassAPI",
                        "Request failed with code: ${response.code()}, message: ${response.message()}"
                    )
                    Toast.makeText(
                        applicationContext,
                        applicationContext.getString(R.string.error_store_locations),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<OverpassResponse>, t: Throwable) {
                t.message?.let { Log.e("Network error while searching for store locations", it) }
                Toast.makeText(
                    applicationContext,
                    applicationContext.getString(R.string.network_error_store_locations),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    /**
     * Add markers to the map for store locations retrieved from the Overpass API response.
     */
    private fun setMarkers() {
        if (locationResponse == null) {
            //Find all locations of the store, make request to api
            findLocations()
        }
        // Iterate through the elements and add markers for each one
        if (locationResponse != null && locationResponse!!.elements.isNotEmpty()) {
            for (element in locationResponse!!.elements) {
                val marker = Marker(map)
                val lat: Double
                val lon: Double

                // Check if 'center' exists and use its values if present, otherwise use top-level values
                if (element.center != null) {
                    lat = element.center.lat
                    lon = element.center.lon
                } else {
                    lat = element.lat!!
                    lon = element.lon!!
                }

                // Set the marker position using GeoPoint with the determined lat and lon
                marker.position = GeoPoint(lat, lon)
                marker.title = store

                // Set an OnClickListener for the marker
                marker.setOnMarkerClickListener { mkr, _ ->
                    findNearest = false
                    val location = Location("UserLocation")
                    //Change route destination to the marker selected
                    location.latitude = myLocationOverlay.myLocation.latitude
                    location.longitude = myLocationOverlay.myLocation.longitude
                    roadRoute(location, GeoPoint(mkr.position))
                    true // Indicates that the click event has been consumed
                }

                //println(location + element.lat +" ," + element.lon)
                map.overlays.add(marker)
            }
        }

        routeToNearest()

        // Refresh the map to display the markers
        map.invalidate()
        myLocationOverlay
    }


    /**
     * Display route to a location
     */
    private fun roadRoute(myLocation: Location?, location: GeoPoint) {

        var getRoute = true
        //Verify if the user is getting far way from the route
        if (roadOverlay != null) {
            getRoute = !(roadOverlay!!.isCloseTo(GeoPoint(myLocation), 50.0, map))
        }

        //If the user is too far away or if the roadOverlay is null get the route
        if (getRoute) {
            // Create a list of waypoints for the route
            waypoints = ArrayList()
            waypoints!!.add(
                GeoPoint(myLocation)
            )
            waypoints!!.add(
                location
            )
            // Create a coroutine scope
            val coroutineScope = CoroutineScope(Dispatchers.IO)

            // Launch a coroutine within the scope
            coroutineScope.launch {
                // Perform network request on a background thread
                val road = roadManager.getRoad(waypoints)

                // Use withContext to switch to the main thread for UI updates
                withContext(Dispatchers.Main) {
                    //Check road status
                    if (road.mStatus != Road.STATUS_OK) {
                        var text = applicationContext.getString(R.string.error_route)
                        if (road.mStatus == Road.STATUS_INVALID) {
                            text += " " + applicationContext.getString(R.string.error_route_invalid)
                        } else if (road.mStatus == Road.STATUS_TECHNICAL_ISSUE) {
                            text += " " + applicationContext.getString(R.string.error_route_technical_issues)
                        }
                        Toast.makeText(
                            applicationContext, text, Toast.LENGTH_SHORT
                        ).show()
                        //Hide the route info
                        routeLayout.visibility = View.GONE
                    } else {
                        routeLayout.visibility = View.VISIBLE
                        road.mBoundingBox
                        road.mLength
                        // Calculate duration in h:m:s format
                        val durationSeconds = road.mDuration.toDouble() // Duration in seconds
                        val hours = durationSeconds.toInt() / 3600
                        val minutes = (durationSeconds.toInt() % 3600) / 60
                        val seconds = (durationSeconds.toInt() % 3600) % 60
                        // Format the duration as h:m:s
                        val formattedDuration =
                            String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        infoRoad.text = "${formattedDuration}\n${"%.2f".format(road.mLength)} km"


                    }
                    //Remove the overlays
                    for (i in 0 until instructionsMarkers.size) {
                        map.overlays.remove(instructionsMarkers[i])
                    }
                    //Remove the instructionsMarkers from the array by replacing it with a new one
                    instructionsMarkers = ArrayList()
                    val nodeIcon = ResourcesCompat.getDrawable(
                        resources, R.drawable.ic_baseline_circle_24, null
                    )
                    //Get instructions for the route
                    for (i in 0 until road.mNodes.size) {
                        val node = road.mNodes[i]
                        val instructMarker = Marker(map)
                        instructMarker.position = node.mLocation
                        instructMarker.icon = nodeIcon
                        instructMarker.title = "$i"
                        instructMarker.subDescription = Road.getLengthDurationText(
                            applicationContext, node.mLength, node.mDuration
                        )
                        instructMarker.snippet = node.mInstructions
                        instructionsMarkers.add(instructMarker)

                        val icon = getManeuverIcon(node.mManeuverType)
                        instructMarker.image = icon
                        map.overlays.add(instructMarker)
                    }
                    locationRoute = location
                    // Remove old route overlay
                    if (roadOverlay != null) {
                        map.overlays.remove(roadOverlay)
                    }
                    // Display the new route on the map
                    roadOverlay = RoadManager.buildRoadOverlay(road)
                    map.overlays.add(roadOverlay)
                    map.invalidate()
                }
            }
        }
    }

    /**
     * Find nearest store according to it's latitude and longitude
     */
    private fun findNearestStore(
        currentLocation: Location, elements: List<OverpassElement>
    ): GeoPoint? {
        var nearestStore: GeoPoint? = null
        var shortestDistance = Double.MAX_VALUE

        for (element in elements) {
            val lat: Double
            val lon: Double

            // Check if 'center' exists and use its values if present, otherwise use top-level values
            if (element.center != null) {
                lat = element.center.lat
                lon = element.center.lon
            } else {
                lat = element.lat!!
                lon = element.lon!!
            }

            val storeLocation = GeoPoint(lat, lon)
            val storeDistance = calculateDistance(
                currentLocation.latitude,
                currentLocation.longitude,
                storeLocation.latitude,
                storeLocation.longitude
            )

            if (storeDistance < shortestDistance) {
                shortestDistance = storeDistance
                nearestStore = storeLocation
            }
        }

        return nearestStore
    }

    /**
     * Calculate distance using Haversine formula
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double, lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371 // Earth's radius in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1)) * cos(
            Math.toRadians(lat2)
        ) * sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return false
    }


    /**
     * Get the maneuver icon
     */
    private fun getManeuverIcon(value: Int): Drawable? {
        return when (value) {
            1 -> ContextCompat.getDrawable(applicationContext, R.drawable.ic_continue) // Continue
            6 -> ContextCompat.getDrawable(
                applicationContext, R.drawable.ic_slight_right
            ) // Slight right
            7 -> ContextCompat.getDrawable(applicationContext, R.drawable.ic_turn_right) // Right
            8 -> ContextCompat.getDrawable(
                applicationContext, R.drawable.ic_sharp_right
            ) // Sharp right
            12 -> ContextCompat.getDrawable(applicationContext, R.drawable.ic_u_turn) // U-turn
            5 -> ContextCompat.getDrawable(
                applicationContext, R.drawable.ic_sharp_left
            ) // Sharp left
            4 -> ContextCompat.getDrawable(applicationContext, R.drawable.ic_turn_left) // Left
            3 -> ContextCompat.getDrawable(
                applicationContext, R.drawable.ic_slight_left
            ) // Slight left
            24 -> ContextCompat.getDrawable(
                applicationContext, R.drawable.ic_arrived
            ) // Arrived (at waypoint)
            27 -> ContextCompat.getDrawable(
                applicationContext, R.drawable.ic_roundabout
            ) // Round-about, 1st exit
            28 -> ContextCompat.getDrawable(
                applicationContext, R.drawable.ic_roundabout
            ) // 2nd exit, etc ...
            29 -> ContextCompat.getDrawable(applicationContext, R.drawable.ic_roundabout)
            30 -> ContextCompat.getDrawable(applicationContext, R.drawable.ic_roundabout)
            31 -> ContextCompat.getDrawable(applicationContext, R.drawable.ic_roundabout)
            32 -> ContextCompat.getDrawable(applicationContext, R.drawable.ic_roundabout)
            33 -> ContextCompat.getDrawable(applicationContext, R.drawable.ic_roundabout)
            34 -> ContextCompat.getDrawable(
                applicationContext, R.drawable.ic_roundabout
            )
            else -> ContextCompat.getDrawable(
                applicationContext, R.drawable.ic_empty
            )
        }
    }

    /**
     * Open the information window closest to the user location
     */
    private fun openClosestInfoWindow(location: Location?) {
        if (location == null) {
            return
        }

        var shorterDistance = Double.MAX_VALUE
        var openedWindow: Marker? = null

        for (i in 0 until instructionsMarkers.size) {
            val item = instructionsMarkers[i]
            val markerLocation = item.position

            // Calculate the distance between the user's location and the marker
            val distance = calculateDistance(
                location.latitude,
                location.longitude,
                markerLocation.latitude,
                markerLocation.longitude
            )

            // Check if the marker is close enough to open the bubble
            if (distance <= 0.5 && distance <= shorterDistance) {
                openedWindow = item
                shorterDistance = distance
            } else {
                if (item.isInfoWindowOpen) {
                    item.closeInfoWindow()
                }
            }
        }
        if (openedWindow != null) {
            if (!openedWindow.isInfoWindowOpen) {
                openedWindow.showInfoWindow()
            }
        }

    }
}