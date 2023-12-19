package com.mquniversity.tcct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.maps.model.DirectionsRoute

class MotorcycleResultFragment(private val motorcycleSize: String): ResultFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        if (!isInitialized) {
            isInitialized = true
            factor = calculationValues.motorcycleValueMap[motorcycleSize]!!
            updateRouteResults()
        }
        return rootScrollView
    }
    fun updateFactor(motorcycleSize: String) {
        factor = calculationValues.motorcycleValueMap[motorcycleSize]!!
        // TODO
    }

    override fun updateRouteResults() {
        // TODO
    }

    override fun insertRouteResult(route: DirectionsRoute, i: Int) {
        TODO("Not yet implemented")
    }
}