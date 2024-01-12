package com.mquniversity.tcct

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.maps.GeoApiContext
import com.google.maps.model.EncodedPolyline
import com.mquniversity.tcct.PermissionUtils.isPermissionGranted
import com.mquniversity.tcct.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.schedule

/**
 * location bias radius for search result suggestion. Value range: [0 - 50000] in meters
 */
@Suppress("unused")
private const val BIAS_RADIUS: Double = 5000.0
private const val DEFAULT_ZOOM = 15f

/**
 * Request code for location permission request.
 *
 * https://github.com/googlemaps-samples/android-samples/blob/8caba9ce7b04ff9385422074e8e21ed1a9174de7/ApiDemos/kotlin/app/src/gms/java/com/example/kotlindemos/MyLocationDemoActivity.kt#L184C63-L184C63
 * https://developers.google.com/maps/documentation/android-sdk/location
 *
 * @see .onRequestPermissionsResult
 */
private const val LOCATION_PERMISSION_REQUEST_CODE = 1

/**
 * Request code for settings check request
 * https://github.com/android/location-samples/blob/1e9d5122894ad0ed100fc6e8f72a5f21c790cd86/LocationSettings/app/src/main/java/com/google/android/gms/location/sample/locationsettings/MainActivity.java#L61
 * https://developers.google.com/android/reference/com/google/android/gms/common/api/ResolvableApiException#public-void-startresolutionforresult-activity-activity,-int-requestcode
 */
private const val REQUEST_CHECK_SETTINGS = 1


class MainActivity : AppCompatActivity(), OnMapReadyCallback, OnRequestPermissionsResultCallback {
    private var permissionDenied = false
    private lateinit var googleMap: GoogleMap
    private lateinit var binding: ActivityMainBinding

    lateinit var geoApiContext: GeoApiContext

    lateinit var apiKey: String

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient

    var origin: Place? = null
    var dest: Place? = null
    private lateinit var originInput: AutocompleteSupportFragment
    private lateinit var destInput: AutocompleteSupportFragment
    private var originMarker: Marker? = null
    private var destMarker: Marker? = null
    private var lastOrigin: Place? = null
    private var lastDest: Place? = null

    lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    lateinit var transportSelection: TransportSelection
    private lateinit var backPressedHandler: OnBackPressedCallback

    lateinit var calculationValues: CalculationValues

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.activityToolbar)
        binding.activityToolbar.setNavigationOnClickListener {
            binding.root.open()
        }
        binding.sidebarNavigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_privacy -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_link))))
                else -> {}
            }
            return@setNavigationItemSelectedListener false
        }

        calculationValues = CalculationValues(this)

        // add TransportSelectionLayout to the transport_mode_selection view (HorizontalScrollView)
        transportSelection = TransportSelection(this)
        binding.transportModeSelection.addView(transportSelection)

        backPressedHandler = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currFrag = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (binding.root.isOpen) {
                    binding.root.close()
                    return
                }
                if (currFrag == null) {
                    finish()
                    return
                }
                when (currFrag) {
                    is CarResultFragment -> {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        supportFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragment_container,
                                supportFragmentManager.findFragmentByTag(getString(R.string.tag_car_query))!!)
                            .commit()
                    }
                    is MotorcycleResultFragment -> {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        supportFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragment_container,
                                supportFragmentManager.findFragmentByTag(getString(R.string.tag_motorcycle_query))!!)
                            .commit()
                    }
                    else -> {
                        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        } else {
                            finish()
                        }
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(
            this,
            backPressedHandler
        )

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        apiKey = packageManager
            .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            .metaData
            .getString("com.google.android.geo.API_KEY")
            .toString()

        geoApiContext = GeoApiContext
            .Builder()
            .apiKey(apiKey)
            .build()

        // initialize Places if not initialized
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey, resources.configuration.locales[0])
        }
        placesClient = Places.createClient(this)

        originInput = supportFragmentManager.findFragmentById(R.id.input_origin) as AutocompleteSupportFragment
        originInput.setHint(getString(R.string.hint_input_origin))
        destInput = supportFragmentManager.findFragmentById(R.id.input_dest) as AutocompleteSupportFragment
        destInput.setHint(getString(R.string.hint_input_dest))

        // Specify the types of place data to return.
        val fields = listOf(Place.Field.NAME, Place.Field.LAT_LNG)
        originInput.setPlaceFields(fields)
        destInput.setPlaceFields(fields)

        // Set up a PlaceSelectionListener to handle the response.
        originInput.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // hide clear button i.e. disable it
                val clearBtn = originInput.view?.findViewById(com.google.android.libraries.places.R.id.places_autocomplete_clear_button) as ImageButton
                Timer().schedule(200) {
                    clearBtn.post {
                        clearBtn.visibility = View.GONE
                    }
                }
                if (dest != null && place.latLng == dest?.latLng) {
                    // delay is needed probably because after fetching the Place
                    // with the API, the AutocompleteSupportFragment sets the text to the place name.
                    // and since this API request is asynchronous and takes time,
                    // originInput.setText(null) is overwritten after the Place is fetched.
                    Timer().schedule(100) {
                        originInput.setText(null)
                    }
                    Snackbar
                        .make(binding.root, getString(R.string.warning_same_location), Snackbar.LENGTH_SHORT)
                        .show()
                    return
                }
                origin = place
                calculate(false)
            }
            override fun onError(status: Status) {
                Log.i("Origin Input", "$status")
            }
        })
        destInput.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // hide clear button i.e. disable it
                val clearBtn = destInput.view?.findViewById(com.google.android.libraries.places.R.id.places_autocomplete_clear_button) as ImageButton
                Timer().schedule(200) {
                    clearBtn.post {
                        clearBtn.visibility = View.GONE
                    }
                }
                if (origin != null && place.latLng == origin?.latLng) {
                    Timer().schedule(100) {
                        destInput.setText(null)
                    }
                    Snackbar
                        .make(binding.root, getString(R.string.warning_same_location), Snackbar.LENGTH_SHORT)
                        .show()
                    return
                }
                dest = place
                calculate(false)
            }
            override fun onError(status: Status) {
                Log.i("Destination Input", "$status")
            }
        })

        // 2 seconds should be enough to originInput and destInput to initialise
        Timer().schedule(2000) {
            for (i in 0 until 2) {
                val input = if (i == 0) originInput else destInput
                val clearBtn = input.view?.findViewById(com.google.android.libraries.places.R.id.places_autocomplete_clear_button) as ImageButton
                clearBtn.post {
                    // disable clear button just in case the user quickly
                    // enters location and clicks on clear button before changing the visibility.
                    // should not happen though
                    clearBtn.isEnabled = false
                }
            }
        }

        binding.locationSwapButton.setOnClickListener {
            if (origin == null && dest == null) {
                return@setOnClickListener
            }
            enableButtons(false)
            val prevOrigin = origin
            val prevDest = dest
            origin = prevDest
            dest = prevOrigin
            originInput.setText(prevDest?.name)
            destInput.setText(prevOrigin?.name)
            calculate(false)
        }

        binding.reloadButton.setOnClickListener {
            enableButtons(false)
            calculate(true)
        }

        binding.currentLocationButton.setOnClickListener {
            enableButtons(false)
            // https:/developers.google.com/maps//documentation/android-sdk/current-place-tutorial
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
                    enableButtons(true)
                    Snackbar.make(
                        binding.root,
                        "Location needs to be enabled to retrieve current location",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                val request = FindCurrentPlaceRequest.newInstance(
                    listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.NAME)
                )
                Snackbar.make(
                    binding.root,
                    "Retrieving current location...",
                    Snackbar.LENGTH_SHORT
                ).show()
                val placeResult = placesClient.findCurrentPlace(request)
                placeResult.addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        val curPlace = task.result.placeLikelihoods[0].place
                        origin = curPlace
                        originInput.setText(curPlace.address)
                        // hide clear button i.e. disable it
                        val clearBtn = originInput.view?.findViewById(com.google.android.libraries.places.R.id.places_autocomplete_clear_button) as ImageButton
                        Timer().schedule(200) {
                            clearBtn.post {
                                clearBtn.visibility = View.GONE
                            }
                        }
                        calculate(false)
                    } else {
                        enableButtons(true)
                    }
                }
            } else {
                enableButtons(true)
            }
        }

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.peekHeight = resources.displayMetrics.heightPixels / 8
    }

    fun calculate(reload: Boolean) {
        if (origin == null || dest == null) {
            enableButtons(true)
            return
        }
        if (origin != lastOrigin || dest != lastDest) {
            transportSelection.resetIconTexts()
        }
        lastOrigin = origin
        lastDest = dest
        enableButtons(false)
        // remove help text at the first call
        if (binding.helperText.isAttachedToWindow) {
            binding.bottomSheet.removeView(binding.helperText)
        }

        moveMarkers()
        moveCameraBetween()

        val currFrag = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currFrag is ResultFragment) {
            currFrag.hidePolylines()
        }

        var frag: Fragment?
        when (transportSelection.currMode) {
            TransportMode.CAR -> {
                frag = supportFragmentManager.findFragmentByTag(getString(R.string.tag_car_result))
                if (frag == null) {
                    frag = supportFragmentManager.findFragmentByTag(getString(R.string.tag_car_query))
                    if (frag == null) {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragment_container, CarQueryFragment(), getString(R.string.tag_car_query))
                            .addToBackStack(getString(R.string.tag_car_query))
                            .commit()
                        enableButtons(true)
                        return
                    }
                }
            }
            TransportMode.MOTORCYCLE -> {
                frag = supportFragmentManager.findFragmentByTag(getString(R.string.tag_motorcycle_result))
                if (frag == null) {
                    frag = supportFragmentManager.findFragmentByTag(getString(R.string.tag_motorcycle_query))
                    if (frag == null) {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragment_container, MotorcycleQueryFragment(), getString(R.string.tag_motorcycle_query))
                            .addToBackStack(getString(R.string.tag_motorcycle_query))
                            .commit()
                        enableButtons(true)
                        return
                    }
                }
            }
            TransportMode.PUBLIC_TRANSPORT -> {
                frag = supportFragmentManager.findFragmentByTag(getString(R.string.tag_public_transport_result))
                if (frag == null) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container, PublicTransportResultFragment(), getString(R.string.tag_public_transport_result))
                        .addToBackStack(getString(R.string.tag_public_transport_result))
                        .commit()
                    enableButtons(true)
                    return
                }
            }
            else -> throw IllegalArgumentException("TransportSelection.currMode is in wrong state")
        }
        if (frag is ResultFragment) {
            if (!isOnline(this)) {
                Snackbar.make(
                    binding.root,
                    "Calculating requires network connection",
                    Snackbar.LENGTH_SHORT
                ).show()
                enableButtons(true)
                return
            }
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            frag.update(reload)
        } else {
            // frag is query fragment
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            enableButtons(true)
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, frag, frag.tag)
            .commit()
    }

    fun insertPolyline(encodedPolyline: EncodedPolyline, color: Int, patterns: List<PatternItem>?): Polyline {
        return googleMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .geodesic(true)
                .width(15f)
                .color(color)
                .pattern(patterns)
                .jointType(JointType.ROUND)
                .startCap(RoundCap())
                .endCap(RoundCap())
                .addAll(
                    encodedPolyline.decodePath().map {
                        latLng -> LatLng(latLng.lat, latLng.lng)
                    }
                )
        )
    }

    private fun moveMarkers() {
        if (originMarker == null) {
            originMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(origin?.latLng!!)
            )
        } else {
            originMarker!!.position = origin?.latLng!!
        }
        if (destMarker == null) {
            destMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(dest?.latLng!!)
            )
        } else {
            destMarker!!.position = dest?.latLng!!
        }
    }

    private fun moveCameraBetween() {
        val bounds = LatLngBounds.Builder()
            .include(origin?.latLng!!)
            .include(dest?.latLng!!)
            .build()
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(bounds, 64)
        )
    }

    fun enableButtons(enable: Boolean) {
        binding.root.post {
            originInput.view?.isEnabled = enable
            destInput.view?.isEnabled = enable
            binding.locationSwapButton.isEnabled = enable
            binding.reloadButton.isEnabled = enable
            binding.currentLocationButton.isEnabled = enable
            transportSelection.enableButtons(enable)
        }
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }
        }
        return false
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        // disable map tilt
        googleMap.uiSettings.isTiltGesturesEnabled = false
        // highlight route on click
        googleMap.setOnPolylineClickListener { polyline ->
            val resultFrag = supportFragmentManager.findFragmentById(R.id.fragment_container) as ResultFragment
            val idx = resultFrag.findRouteIdxWithPolyline(polyline)
            resultFrag.highlightRoute(idx)
            resultFrag.highlightResult(idx)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        enableMyLocation()
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private fun enableMyLocation() {
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
            ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            // Priority.PRIORITY_HIGH_ACCURACY is needed to notify SettingsClient
            // that it needs to notify the user to enable location
            val locationRequest = LocationRequest.create()
            locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY
            // Check if location is turned on
            val lsrBuilder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(false)
            val settingsClient: SettingsClient = LocationServices.getSettingsClient(this)
            val lsrTask: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(lsrBuilder.build())
            // Location is turned on so get current location, set search result location bias and move camera
            lsrTask.addOnSuccessListener { lsr : LocationSettingsResponse? ->
                if (lsr?.locationSettingsStates?.isLocationUsable == true) {
                    val currLocRequest = CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                        .build()
                    val cts = CancellationTokenSource()
                    val locTask = fusedLocationClient.getCurrentLocation(currLocRequest, cts.token)
                    locTask.addOnSuccessListener { location ->
                        if (location != null) {
                            val ll = LatLng(
                                location.latitude,
                                location.longitude
                            )
                            // set location bias for search results
//                            val bound = CircularBounds.newInstance(ll, BIAS_RADIUS)
//                            originInput.setLocationBias(bound)
//                            destInput.setLocationBias(bound)
                            // move camera center to the current location
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, DEFAULT_ZOOM))
                        }
                    }
                    locTask.addOnFailureListener {
                        Snackbar
                            .make(binding.root, "Could not retrieve current location. Please try again later.", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Snackbar
                        .make(binding.root, "Location is not enabled", Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
            // Location is not turned on so ask the user to turn it on
            lsrTask.addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        // The activity's onActivityResult method will be invoked after the user is done.
                        // If the resultCode is Activity.RESULT_OK, the application should try to connect again.
                        e.startResolutionForResult(this@MainActivity, REQUEST_CHECK_SETTINGS)
                    } catch (_: IntentSender.SendIntentException) {}
                }
            }
            googleMap.isMyLocationEnabled = true
            return
        }


        // 2. If a permission rationale dialog should be shown
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            PermissionUtils.RationaleDialog.newInstance(
                LOCATION_PERMISSION_REQUEST_CODE, true
            ).show(supportFragmentManager, "dialog")
            return
        }

        // 3. Otherwise, request permission
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
            return
        }

        if (isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError()
            permissionDenied = false
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private fun showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog(true).show(supportFragmentManager, "dialog")
    }

    override fun onDestroy() {
        CoroutineScope(Dispatchers.IO).launch {
            geoApiContext.shutdown()
        }
        super.onDestroy()
    }
}
