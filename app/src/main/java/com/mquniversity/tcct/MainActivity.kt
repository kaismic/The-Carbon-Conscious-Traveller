package com.mquniversity.tcct

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textview.MaterialTextView
import com.google.maps.DirectionsApi
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.mquniversity.tcct.databinding.ActivityMapsBinding
import java.io.IOException


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var geoApiContext: GeoApiContext

    private lateinit var inputOrigin: AutocompleteSupportFragment
    private lateinit var inputDest: AutocompleteSupportFragment
    private var origin: Place? = null
    private var destination: Place? = null

    private lateinit var directionsApiRequest: DirectionsApiRequest

    private lateinit var resultDisplay: TextView
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        inputOrigin = supportFragmentManager.findFragmentById(R.id.input_origin) as AutocompleteSupportFragment
        inputOrigin.setHint(resources.getString(R.string.input_origin_hint))
        inputDest = supportFragmentManager.findFragmentById(R.id.input_dest) as AutocompleteSupportFragment
        inputDest.setHint(resources.getString(R.string.input_dest_hint))

        // Specify the types of place data to return.
        inputOrigin.setPlaceFields(listOf(Place.Field.ADDRESS, Place.Field.NAME))
        inputDest.setPlaceFields(listOf(Place.Field.ADDRESS, Place.Field.NAME))

        // Set up a PlaceSelectionListener to handle the response.
        inputOrigin.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                origin = place
                calculate()
            }
            override fun onError(status: Status) {
                Log.i("Origin Input", "$status")
            }
        })
        inputDest.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                destination = place
                calculate()
            }
            override fun onError(status: Status) {
                Log.i("Destination Input", "$status")
            }
        })

        resultDisplay = findViewById<MaterialTextView>(R.id.result_display)
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet))
    }

    fun calculate() {
        if (origin == null || destination == null) {
            return
        }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        resultDisplay.text = "Calculating..."
        directionsApiRequest = DirectionsApi.getDirections(geoApiContext, origin?.address, destination?.address)
        try {
            val response = directionsApiRequest.await()
            val leg = response.routes[0].legs[0]
            val totalEmission = leg.distance.inMeters * 0.17 / 1000 // kg
            val totalDist = leg.distance.inMeters / 1000f // km
            resultDisplay.text = "This trip would produce " + "%.2f".format(totalEmission) + "kg of CO2 over " + totalDist + "km"
        } catch (e: ApiException) {
            resultDisplay.text = "An error has occurred. Please try again few seconds later."
            Log.e("Calculation", "${e.statusCode}: ${e.status}: ${e.message}")
        } catch (e: InterruptedException) {
            resultDisplay.text = "An error has occurred. Please try again few seconds later."
            Log.e("Calculation", "${e.message}")
        } catch (e: IOException) {
            resultDisplay.text = "An error has occurred. Please try again few seconds later."
            Log.e("Calculation", "${e.message}")
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

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

    }

    override fun onDestroy() {
        super.onDestroy()
        geoApiContext.shutdown()
    }
}