package com.mquniversity.tcct

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
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
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.mquniversity.tcct.PermissionUtils.isPermissionGranted
import com.mquniversity.tcct.databinding.ActivityMapsBinding
import java.util.Timer
import kotlin.concurrent.schedule

/**
 * location bias radius for search result suggestion. Value range: [0 - 50000] in meters
 */
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


class MainActivity : AppCompatActivity(), OnMyLocationButtonClickListener,
    OnMapReadyCallback, OnRequestPermissionsResultCallback {
    private var permissionDenied = false
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var geoApiContext: GeoApiContext

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var originInput: AutocompleteSupportFragment
    private lateinit var destInput: AutocompleteSupportFragment
    private var origin: Place? = null
    private var destination: Place? = null

    private lateinit var directionsApiRequest: DirectionsApiRequest

    private lateinit var bottomSheet: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var transportSelection: TransportSelection
    private lateinit var backPressedHandler: OnBackPressedCallback

    private lateinit var calculationValues: CalculationValues

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        calculationValues = CalculationValues(this)

        // add TransportSelectionLayout to the transport_mode_selection view (HorizontalScrollView)
        val transportSelectionView: HorizontalScrollView = findViewById(R.id.transport_mode_selection)
        transportSelection = TransportSelection(this)
        transportSelection.setOnCheckedChangeListener { _, _ ->
            calculate()
        }
        transportSelectionView.addView(transportSelection)

        backPressedHandler = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                val visibleFrag = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (visibleFrag != null) {
                    when (visibleFrag) {
                        is CarQueryFragment, is MotorcycleQueryFragment -> {
                            finish()
                            return
                        }
                    }
                } else {
                    isEnabled = false
                }
                supportFragmentManager.popBackStack()
                if (supportFragmentManager.backStackEntryCount <= 1) {
                    isEnabled = false
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

        val apiKey = packageManager
            .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            .metaData
            .getString("com.google.android.geo.API_KEY")

        geoApiContext = GeoApiContext
            .Builder()
            .apiKey(apiKey)
            .build()

        // initialize Places if not initialized
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey.toString(), resources.configuration.locales[0])
        }

        originInput = supportFragmentManager.findFragmentById(R.id.input_origin) as AutocompleteSupportFragment
        originInput.setHint(resources.getString(R.string.input_origin_hint))
        destInput = supportFragmentManager.findFragmentById(R.id.input_dest) as AutocompleteSupportFragment
        destInput.setHint(resources.getString(R.string.input_dest_hint))

        // Specify the types of place data to return.
        originInput.setPlaceFields(listOf(Place.Field.ADDRESS, Place.Field.NAME))
        destInput.setPlaceFields(listOf(Place.Field.ADDRESS, Place.Field.NAME))

        // Set up a PlaceSelectionListener to handle the response.
        originInput.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                if (destination != null && place.address == destination?.address) {
                    // delay is needed probably because after fetching the Place
                    // with the API, the AutocompleteSupportFragment sets the text to the place name.
                    // and since this API request is asynchronous and takes time,
                    // originInput.setText(null) is overwritten after the Place is fetched.
                    Timer().schedule(100) {
                        originInput.setText(null)
                    }
                    val snack = Snackbar.make(binding.root, resources.getString(R.string.same_location_warning), Snackbar.LENGTH_SHORT)
                    snack.show()
                    return
                }
                origin = place
                calculate()
            }
            override fun onError(status: Status) {
                Log.i("Origin Input", "$status")
            }
        })
        destInput.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                if (origin != null && place.address == origin?.address) {
                    Timer().schedule(100) {
                        destInput.setText(null)
                    }
                    val snack = Snackbar.make(binding.root, resources.getString(R.string.same_location_warning), Snackbar.LENGTH_SHORT)
                    snack.show()
                    return
                }
                destination = place
                calculate()
            }
            override fun onError(status: Status) {
                Log.i("Destination Input", "$status")
            }
        })

        bottomSheet = findViewById(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = resources.displayMetrics.heightPixels / 4
    }

    fun calculate() {
        if (origin == null || destination == null) {
            return
        }

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        showVehicleDetailQuery()
//        resultDisplay.text = "Calculating..."
//        directionsApiRequest = DirectionsApi.getDirections(geoApiContext, origin?.address, destination?.address)
//        try {
//            val response = directionsApiRequest.await()
//            val leg = response.routes[0].legs[0]
//            val totalEmission = leg.distance.inMeters * 0.17 / 1000 // kg
//            val totalDist = leg.distance.inMeters / 1000f // km
//            resultDisplay.text = "This trip would produce " + "%.2f".format(totalEmission) + "kg of CO2 over " + totalDist + "km"
//        } catch (e: ApiException) {
//            resultDisplay.text = "An error has occurred. Please try again few seconds later."
//            Log.e("Calculation", "${e.statusCode}: ${e.status}: ${e.message}")
//        } catch (e: InterruptedException) {
//            resultDisplay.text = "An error has occurred. Please try again few seconds later."
//            Log.e("Calculation", "${e.message}")
//        } catch (e: IOException) {
//            resultDisplay.text = "An error has occurred. Please try again few seconds later."
//            Log.e("Calculation", "${e.message}")
//        }
    }

    private fun showVehicleDetailQuery() {
        val tag = when (transportSelection.currMode) {
            TravelModes.CAR.ordinal -> resources.getString(R.string.fragment_tag_car_info)
            TravelModes.MOTORCYCLE.ordinal -> resources.getString(R.string.fragment_tag_motorcycle_info)
            else -> return
        }
        var frag = supportFragmentManager.findFragmentByTag(tag)
        val transaction = supportFragmentManager.beginTransaction()
        if (frag == null) {
            frag = when (transportSelection.currMode) {
                TravelModes.CAR.ordinal -> CarQueryFragment()
                TravelModes.MOTORCYCLE.ordinal -> MotorcycleQueryFragment()
                else -> return
            }
            val bundle = Bundle()
            when (transportSelection.currMode) {
                TravelModes.CAR.ordinal -> {
                    bundle.putStringArray("carSizes", calculationValues.carTypes.toTypedArray())
                    bundle.putStringArray("carFuelTypes", calculationValues.carFuelTypes)
                    bundle.putSerializable("carValues", calculationValues.carValuesMatrix.toTypedArray())
                }
                TravelModes.MOTORCYCLE.ordinal -> {
                    bundle.putStringArray("motorcycleSizes", calculationValues.simpleTransportTypes[MOTORCYCLE]?.toTypedArray())
                }
                else -> return
            }
            frag.arguments = bundle
            transaction
                .replace(R.id.fragment_container, frag, tag)
                .addToBackStack(tag)
                .commit()
            backPressedHandler.isEnabled = true
        } else {
            transaction
                .replace(R.id.fragment_container, frag, tag)
                .commit()
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // disable map tilt
        googleMap.uiSettings.isTiltGesturesEnabled = false

        googleMap.setOnMyLocationButtonClickListener(this)
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
            // create a periodic locationRequest
            val locationRequest = LocationRequest.create()
            locationRequest.interval = 10 * 1000
            locationRequest.fastestInterval = 5 * 1000
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
                            // TODO figure out why this throws this error: java.lang.AssertionError: Unknown LocationBias type.
                            // but it seems like I'm the first one who encountered this error... no similar search results on google
//                            val bound = CircularBounds.newInstance(ll, BIAS_RADIUS)
//                            inputOrigin.setLocationBias(bound)
//                            inputDest.setLocationBias(bound)
                            // move camera center to the current location
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, DEFAULT_ZOOM))
                        }
                    }
                    locTask.addOnFailureListener {
                        Toast.makeText(this, "Could not retrieve current location. Please try again later.", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(this, "Location is not enabled", Toast.LENGTH_SHORT)
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
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(this, "Location is not enabled", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            mMap.isMyLocationEnabled = true
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

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT)
            .show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == RESULT_OK) {
            enableMyLocation()
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
        super.onDestroy()
        geoApiContext.shutdown()
    }
}