package com.mquniversity.tcct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class CarResultFragment(
    private val carSize: String,
    private val carFuelType: String
): PrivateVehicleResultFragment() {
    private val emissionTexts: MutableList<TextView> = mutableListOf()
    private val distTexts: MutableList<TextView> = mutableListOf()
    private val durationTexts: MutableList<TextView> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        if (!isInitialized) {
            isInitialized = true
            iconResId = R.drawable.outline_directions_car_24
            factor = calculationValues.carValuesMatrix[calculationValues.carSizes.indexOf(carSize)][calculationValues.carFuelTypes.indexOf(carFuelType)]
            updateRouteResults()
        }
        return rootScrollView
    }

    fun updateFactor(carSize: String, carFuelType: String) {
        factor = calculationValues.carValuesMatrix[calculationValues.carSizes.indexOf(carSize)][calculationValues.carFuelTypes.indexOf(carFuelType)]
        for (i in currRoutes.indices) {
            emissionTexts[i].text = CalculationUtils.calculateByDistance(currRoutes[i].legs[0].distance.inMeters, factor)
            distTexts[i].text = currRoutes[i].legs[0].distance.humanReadable
            durationTexts[i].text = currRoutes[i].legs[0].duration.humanReadable
        }
    }
}