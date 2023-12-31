package com.mquniversity.tcct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.maps.model.TravelMode

class MotorcycleResultFragment(private val motorcycleSize: String): PrivateVehicleResultFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        if (!isInitialized) {
            isInitialized = true
            travelMode = TravelMode.DRIVING
            iconResId = R.drawable.outline_sports_motorsports_24
            factor = calculationValues.motorcycleValueMap[motorcycleSize]!!
            update()
        }
        return rootScrollView
    }
    fun updateFactor(motorcycleSize: String) {
        factor = calculationValues.motorcycleValueMap[motorcycleSize]!!
        for (i in currRoutes.indices) {
            emissionTexts[i].text = CalculationUtils.calculateByDistance(currRoutes[i].legs[0].distance.inMeters, factor)
            distTexts[i].text = currRoutes[i].legs[0].distance.humanReadable
            durationTexts[i].text = currRoutes[i].legs[0].duration.humanReadable
        }
    }
}