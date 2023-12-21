package com.mquniversity.tcct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.TravelMode
import com.google.maps.model.VehicleType

class PublicTransportResultFragment: ResultFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        if (!isInitialized) {
            isInitialized = true
            travelMode = TravelMode.TRANSIT
            iconResId = R.drawable.outline_directions_subway_24
            updateRouteResults()
        }
        return rootScrollView
    }

    override fun insertRouteResult(route: DirectionsRoute, i: Int) {
        val resultLayout = layoutInflater.inflate(
            R.layout.public_transport_result_item,
            mainLayout,
            false) as LinearLayout

        val emissionText: TextView = resultLayout.findViewById(R.id.emission_text)
        val distText: TextView = resultLayout.findViewById(R.id.distance_text)
        distText.text = route.legs[0].distance.humanReadable
        val durationText: TextView = resultLayout.findViewById(R.id.duration_text)
        durationText.text = route.legs[0].duration.humanReadable

        emissionTexts.add(emissionText)
        distTexts.add(distText)
        durationTexts.add(durationText)

        val steps = route.legs[0].steps
        var totalEmissionInGram = 0f
        for (step in steps) {
            when (step.travelMode) {
                TravelMode.TRANSIT -> {
                    val factor: Float = when (step.transitDetails.line.vehicle.type) {
                        VehicleType.BUS -> calculationValues.busValueMap["Average local bus"]!!
                        VehicleType.INTERCITY_BUS -> calculationValues.busValueMap["Coach"]!!
                        VehicleType.HEAVY_RAIL,
                        VehicleType.HIGH_SPEED_TRAIN,
                        VehicleType.LONG_DISTANCE_TRAIN -> calculationValues.railValueMap["National rail"]!!
                        VehicleType.COMMUTER_TRAIN,
                        VehicleType.METRO_RAIL,
                        VehicleType.MONORAIL,
                        VehicleType.RAIL,
                        VehicleType.TRAM -> calculationValues.railValueMap["Light rail and tram"]!!
                        VehicleType.SUBWAY -> calculationValues.railValueMap["London Underground"]!!
                        VehicleType.FERRY -> calculationValues.ferryValueMap["Foot passenger"]!!
                        else -> 0f
                    }
                    totalEmissionInGram += step.distance.inMeters * factor
                }
                else -> {}
            }
        }
        emissionText.text = CalculationUtils.formatEmission(totalEmissionInGram)

        mainLayout.addView(resultLayout)
    }

    /*
        TODO measure icon widths and
         if current on exceeds LinearLayout width replace previous one with "..." TextView
     */
}