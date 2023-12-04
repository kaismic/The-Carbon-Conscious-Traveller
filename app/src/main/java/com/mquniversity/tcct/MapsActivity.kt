package com.mquniversity.tcct

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.Unit
import com.mquniversity.tcct.databinding.ActivityMapsBinding


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var geoApiContext: GeoApiContext

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        geoApiContext = GeoApiContext
            .Builder()
            .apiKey(appInfo.metaData.getString("com.google.android.geo.API_KEY"))
            .build()

        val originEditText = findViewById<TextInputEditText>(R.id.input_origin)
        val destEditText = findViewById<TextInputEditText>(R.id.input_destination)
        val calButton = findViewById<MaterialButton>(R.id.calculate_button)
        val resultDisplay = findViewById<MaterialTextView>(R.id.result_display)

        val inputChecker = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                calButton.isEnabled = originEditText.text?.isBlank() == false && destEditText.text?.isBlank() == false
            }
        }

        val behavior = BottomSheetBehavior.from(findViewById<LinearLayout>(R.id.bottom_sheet))

        originEditText.addTextChangedListener(inputChecker)
        destEditText.addTextChangedListener(inputChecker)

        calButton.setOnClickListener {
            calButton.isEnabled = false
            originEditText.isEnabled = false
            destEditText.isEnabled = false
            behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            resultDisplay.text = "Calculating..."
            val request = DirectionsApi.getDirections(geoApiContext, originEditText.text.toString(), destEditText.text.toString())
            request.units(Unit.METRIC)
            val response = request.await()
            val leg = response.routes[0].legs[0]
            originEditText.setText(leg.startAddress)
            destEditText.setText(leg.endAddress)
            // Quicke, A., 2022. Fuelling efficiency.
            // Assuming 170g CO2 produced per km
            val totalEmission = leg.distance.inMeters * 0.17 / 1000 // kg
            val totalDist = leg.distance.inMeters / 1000f // km
            resultDisplay.text = "This trip would produce " + "%.2f".format(totalEmission) + "kg of CO2 over " + totalDist + "km"
            calButton.isEnabled = true
            originEditText.isEnabled = true
            destEditText.isEnabled = true
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