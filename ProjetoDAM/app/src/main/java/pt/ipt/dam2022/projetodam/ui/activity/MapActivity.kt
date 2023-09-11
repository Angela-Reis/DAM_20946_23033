package pt.ipt.dam2022.projetodam.ui.activity

import android.Manifest
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.routing.OSRMRoadManager
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
import pt.ipt.dam2022.projetodam.FunctionsUtil.requestPermissionsIfNecessary
import pt.ipt.dam2022.projetodam.R
import pt.ipt.dam2022.projetodam.model.overpass.OverpassElement
import pt.ipt.dam2022.projetodam.model.overpass.OverpassResponse
import pt.ipt.dam2022.projetodam.retrofit.RetrofitOverpass
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapActivity : AppCompatActivity() {
    private var locationRoute: GeoPoint? = null
    private lateinit var map: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var btnLocation: ImageButton
    private lateinit var store: String
    private lateinit var roadManager: RoadManager
    private var roadOverlay: Polyline? = null
    private var waypoints: ArrayList<GeoPoint>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_map)

        // Create a roadManager instance using the OSRMRoadManager
        roadManager = OSRMRoadManager(applicationContext)

        //Define the store name to search
        store = "Continente"

        //Request user Permissions
        requestPermissionsIfNecessary(
            arrayOf(
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), this, this
        )

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

        //Create the myLocationOverLay and add a LocationListener to it
        myLocationOverlay =
            object : MyLocationNewOverlay(locationProvider, map) {
                override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
                    super.onLocationChanged(location, source)
                    waypoints?.get(1)?.let { roadRoute(location, it) }
                }
            }
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

        //Find all locations of the store, make request to api
        findLocations()
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
                call: Call<OverpassResponse>,
                response: Response<OverpassResponse>
            ) {
                if (response.isSuccessful) {

                    val locationResponse = response.body()
                    // Check if the response is not null and contains elements
                    if (locationResponse != null && !locationResponse.elements.isNullOrEmpty()) {
                        setMarkers(locationResponse)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "No store locations found",
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
                        "Error searching for store locations",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<OverpassResponse>, t: Throwable) {
                t.message?.let { Log.e("Network error while searching for store locations", it) }

                Toast.makeText(
                    applicationContext,
                    "Network error while searching for store locations",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    /**
     * Add markers to the map for store locations retrieved from the Overpass API response.
     */
    private fun setMarkers(locationResponse: OverpassResponse) {
        // Iterate through the elements and add markers for each one
        for (element in locationResponse.elements) {
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
            //println(location + element.lat +" ," + element.lon)
            map.overlays.add(marker)
        }

        routeToNearest(locationResponse)

        // Refresh the map to display the markers
        map.invalidate()
        myLocationOverlay
    }

    /**
     * Set up display of route to nearest location
     */
    private fun routeToNearest(locationResponse: OverpassResponse) {
        val currentLocation = Location("Current Location")
        val myLocation = myLocationOverlay.myLocation
        if (myLocation != null) {
            currentLocation.latitude = myLocation.latitude
            currentLocation.longitude = myLocation.longitude
        }

        val nearestStore = findNearestStore(
            currentLocation,
            locationResponse.elements
        )

        if (nearestStore != null) {
            roadRoute(currentLocation, nearestStore)
        }
    }


    /**
     * Display route to a location
     */
    private fun roadRoute(myLocation: Location?, location: GeoPoint) {

        var getRoute = true
        //Verify if the user is getting far way from the route
        if(roadOverlay!=null){
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
            // Perform network request on a background thread
            GlobalScope.launch(Dispatchers.IO) {
                // Create a Road for the route using RoadManager
                // Calculate the route
                val roadResult = roadManager.getRoad(waypoints)

                // Handle the road result, possibly on the main thread
                launch(Dispatchers.Main) {
                    locationRoute = location
                    //Remove old route overlay
                    if (roadOverlay != null) {
                        map.overlays.remove(roadOverlay)
                    }
                    // Display the new route on the map
                    roadOverlay = RoadManager.buildRoadOverlay(roadResult)
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
        currentLocation: Location,
        elements: List<OverpassElement>
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
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadius = 6371 // Earth's radius in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c
    }

    fun distanceToLine(point: GeoPoint, lineStart: GeoPoint, lineEnd: GeoPoint): Double {
        val d1 = point.distanceToAsDouble(lineStart)
        val d2 = point.distanceToAsDouble(lineEnd)
        val d3 = lineStart.distanceToAsDouble(lineEnd)

        if (d1 >= d2 + d3 || d2 >= d1 + d3) {
            // The point is closest to one of the line endpoints
            return Math.min(d1, d2)
        }

        // Use the formula to calculate the distance to the line
        val s = (d1 + d2 + d3) / 2.0
        val area = Math.sqrt(s * (s - d1) * (s - d2) * (s - d3))
        return 2.0 * area / d3
    }

}