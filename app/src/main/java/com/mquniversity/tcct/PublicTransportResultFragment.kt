package com.mquniversity.tcct

import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.flexbox.FlexboxLayout
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.TravelMode
import com.google.maps.model.VehicleType
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.URL

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

        val stepsIconContainer: FlexboxLayout = resultLayout.findViewById(R.id.steps_icon_container)

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
            val nextIcon = ImageView(context).apply {
                setImageResource(R.drawable.outline_navigate_next_24)
            }
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
                        VehicleType.TROLLEYBUS -> calculationValues.trolleybusValue
                        VehicleType.CABLE_CAR -> calculationValues.cableCarValue
                        else -> 0f
                    }
                    totalEmissionInGram += step.distance.inMeters * factor

                    val transitIconsContainer = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = FlexboxLayout.LayoutParams(
                            FlexboxLayout.LayoutParams.WRAP_CONTENT,
                            FlexboxLayout.LayoutParams.WRAP_CONTENT
                        )
                    }
                    // TODO use ConstraintLayout to make localIcon fit to shortName TextView
                    try {
                        var iconURL = step.transitDetails.line.vehicle.localIcon
                        if (iconURL.isNullOrEmpty()) {
                            iconURL = step.transitDetails.line.vehicle.icon
                        }
                        if (!iconURL.isNullOrEmpty()) {
                            val completeURL = "https:$iconURL"
                            val localIcon = ImageView(context)
                            val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                                throwable.printStackTrace()
                            }
                            CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                                val bitmap = BitmapFactory.decodeStream(
                                    URL(completeURL).openStream()
                                )
                                localIcon.post {
                                    localIcon.setImageBitmap(bitmap)
                                }
                            }
                            transitIconsContainer.addView(localIcon)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val shortNameText = step.transitDetails.line.shortName
                    if (!shortNameText.isNullOrEmpty()) {
                        val shortName = TextView(context).apply {
                            background = ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.transport_short_name_shape,
                                context.theme
                            )
                            setLines(1)
                            text = shortNameText
                            setTextColor(Color.parseColor(step.transitDetails.line.textColor))
                            backgroundTintList = ColorStateList.valueOf(Color.parseColor(step.transitDetails.line.color))
                        }
                        transitIconsContainer.addView(shortName)
                    }
                    stepsIconContainer.addView(transitIconsContainer)
                }
                TravelMode.WALKING -> {
                    val walkingIcon = ImageView(context).apply {
                        setImageResource(R.drawable.outline_directions_walk_24)
                    }
                    stepsIconContainer.addView(walkingIcon)
                }
                else -> {}
            }
            stepsIconContainer.addView(nextIcon)
        }
        stepsIconContainer.removeViewAt(stepsIconContainer.childCount - 1)
        emissionText.text = CalculationUtils.formatEmission(totalEmissionInGram)

        mainLayout.addView(resultLayout)
    }
}