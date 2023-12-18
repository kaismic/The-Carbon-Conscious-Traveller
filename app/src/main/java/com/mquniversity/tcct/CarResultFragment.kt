package com.mquniversity.tcct

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textview.MaterialTextView
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.TravelMode

class CarResultFragment(
    private var carSize: String,
    private var carFuelType: String
): Fragment(R.layout.result_view) {
    private var isInitialized = false

    private lateinit var rootScrollView: ScrollView
    private lateinit var mainLayout: LinearLayout

    private lateinit var mainActivity: MainActivity
    private lateinit var geoApiContext: GeoApiContext
    private lateinit var calculationValues: CalculationValues
    private var factor = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!isInitialized) {
            isInitialized = true
            rootScrollView = inflater.inflate(R.layout.result_view, container, false) as ScrollView
            mainLayout = LinearLayout(context)
            mainLayout.orientation = LinearLayout.VERTICAL
            mainLayout.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val shapeDivider = GradientDrawable()
            shapeDivider.setSize(0, 32)
            mainLayout.dividerDrawable = shapeDivider
            mainLayout.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
            rootScrollView.addView(mainLayout)

            mainActivity = requireActivity() as MainActivity
            calculationValues = mainActivity.calculationValues
            geoApiContext = mainActivity.geoApiContext
            factor = calculationValues.carValuesMatrix[calculationValues.carSizes.indexOf(carSize)][calculationValues.carFuelTypes.indexOf(carFuelType)]

            val request = DirectionsApi.getDirections(geoApiContext, mainActivity.origin?.address, mainActivity.destination?.address)
                .mode(TravelMode.DRIVING)
                .alternatives(true)
            val response = request.await()
            for (i in response.routes.indices) {
                insertRouteResult(response.routes[i])
            }
        }
        return rootScrollView
    }

    private fun insertRouteResult(route: DirectionsRoute) {
        val resultLayout = LinearLayout(context)
        resultLayout.orientation = LinearLayout.HORIZONTAL
        resultLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val iconLayout = LinearLayout(context)
        iconLayout.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1f
        )
        iconLayout.setPadding(0, 16, 0, 0)
        val carIcon = ImageView(context)
        carIcon.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        carIcon.setImageDrawable(
            ResourcesCompat.getDrawable(
            resources,
            R.drawable.outline_directions_car_24,
            context?.theme
        ))
        iconLayout.addView(carIcon)

        val descLayout = LinearLayout(context)
        descLayout.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            4f
        )
        descLayout.orientation = LinearLayout.VERTICAL
        val summaryText = MaterialTextView(requireContext())
        summaryText.text = "via ${route.summary}"
        summaryText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
        descLayout.addView(summaryText)
        val tapForMoreText = MaterialTextView(requireContext())
        tapForMoreText.text = "Tap for more details"
        tapForMoreText.setTextColor(resources.getColor(com.google.android.libraries.places.R.color.quantum_googblue, context?.theme))
        tapForMoreText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        (tapForMoreText.layoutParams as LinearLayout.LayoutParams).apply {
            gravity = Gravity.BOTTOM
        }
        descLayout.addView(tapForMoreText)

        val valuesLayout = LinearLayout(context)
        valuesLayout.orientation = LinearLayout.VERTICAL
        valuesLayout.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1f
        )
        val emissionText = MaterialTextView(requireContext())
        emissionText.text = calculationValues.calculateByDistance(route.legs[0].distance.inMeters, factor)
        emissionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
        val distText = MaterialTextView(requireContext())
        distText.text = route.legs[0].distance.humanReadable
        distText.alpha = 0.5f
        val durationText = MaterialTextView(requireContext())
        durationText.text = route.legs[0].duration.humanReadable
        durationText.alpha = 0.5f
        valuesLayout.addView(emissionText)
        valuesLayout.addView(distText)
        valuesLayout.addView(durationText)

        resultLayout.addView(iconLayout)
        resultLayout.addView(descLayout)
        resultLayout.addView(valuesLayout)

        mainLayout.addView(resultLayout)
    }
}