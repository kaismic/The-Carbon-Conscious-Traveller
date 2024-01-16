package com.mquniversity.tcct

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.color.MaterialColors
import com.google.maps.model.TravelMode
import com.google.maps.model.VehicleType
import java.io.IOException
import java.net.URL
import java.time.format.DateTimeFormatter

class PublicTransportResultFragment: ResultFragment() {
    private val timePattern = "h:mm a"
    private val iconsMap: MutableMap<String, Bitmap> = mutableMapOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        travelMode = TravelMode.TRANSIT
        update(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return rootScrollView
    }

    fun fetchTransitIcons() {
        for (route in currRoutes) {
            val steps = route.legs[0].steps
            for (step in steps) {
                if (step.travelMode == TravelMode.TRANSIT) {
                    for (i in 0 until 2) {
                        val iconURL = if (i == 0) {
                            step.transitDetails.line.vehicle.localIcon
                        } else {
                            step.transitDetails.line.vehicle.icon
                        }
                        if (!iconURL.isNullOrEmpty() && !iconsMap.containsKey(iconURL)) {
                            val completeURL = "https:$iconURL"
                            try {
                                val bitmap = BitmapFactory.decodeStream(
                                    URL(completeURL).openStream()
                                )
                                if (bitmap != null) {
                                    iconsMap[iconURL] = bitmap
                                }
                            } catch (_: IOException) {}
                        }
                    }
                }
            }
        }
    }

    override fun update(reload: Boolean) {
        // return if the current start and end location is same as before
        if (!reload && areLocationsSameAsBefore()) {
            showPolylines()
            mainActivity.enableButtons(true)
            return
        }
        super.update(reload)
    }

    override fun insertRouteResult(idx: Int): Float {
        resultLayouts[idx] = layoutInflater.inflate(
            R.layout.public_transport_result_item,
            mainLayout,
            false) as LinearLayout

        super.insertRouteResult(idx)

        val resultLayout = resultLayouts[idx]!!
        val route = currRoutes[idx]

        val emissionText: TextView = resultLayout.findViewById(R.id.emission_text)
        val distText: TextView = resultLayout.findViewById(R.id.distance_text)
        distText.text = route.legs[0].distance.humanReadable
        val durationText: TextView = resultLayout.findViewById(R.id.duration_text)
        durationText.text = route.legs[0].duration.humanReadable

        val timeTextView: TextView = resultLayout.findViewById(R.id.departure_arrival_time)
        val depTime = route.legs[0].departureTime
        val arrivalTime = route.legs[0].arrivalTime
        if (depTime != null && arrivalTime != null) {
            val departureTimeText = depTime.format(DateTimeFormatter.ofPattern(timePattern))
            val arrivalTimeText = arrivalTime.format(DateTimeFormatter.ofPattern(timePattern))
            timeTextView.text = getString(R.string.departure_arrival_time, departureTimeText, arrivalTimeText)
        }

        val stepsIconContainer: FlexboxLayout = resultLayout.findViewById(R.id.steps_icon_container)
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
                        VehicleType.TROLLEYBUS -> calculationValues.trolleybusValue
                        VehicleType.CABLE_CAR -> calculationValues.cableCarValue
                        else -> 0f
                    }
                    val stepEmission = step.distance.inMeters * factor
                    totalEmissionInGram += stepEmission

                    val transitIconsContainer: ConstraintLayout = layoutInflater.inflate(
                        R.layout.transit_icons_container,
                        stepsIconContainer,
                        false
                    ) as ConstraintLayout
                    transitIconsContainer.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    try {
                        var iconURL = step.transitDetails.line.vehicle.localIcon
                        if (iconURL.isNullOrEmpty()) {
                            iconURL = step.transitDetails.line.vehicle.icon
                        }
                        if (!iconURL.isNullOrEmpty()) {
                            val bitmap = iconsMap[iconURL]
                            if (bitmap != null) {
                                transitIconsContainer.findViewById<ImageView>(R.id.transit_icon).setImageBitmap(bitmap)
                            }

                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val shortNameText = step.transitDetails.line.shortName ?: step.transitDetails.line.name
                    if (!shortNameText.isNullOrEmpty()) {
                        val shortName: TextView = transitIconsContainer.findViewById(R.id.transit_short_name)
                        shortName.apply {
                            text = shortNameText
                            if (step.transitDetails.line.textColor != null) {
                                setTextColor(Color.parseColor(step.transitDetails.line.textColor))
                            } else {
                                setTextColor(
                                    MaterialColors.getColor(
                                        requireContext(),
                                        android.R.attr.textColorPrimary,
                                        "android.R.attr.textColorPrimary error"
                                    )
                                )
                            }
                            backgroundTintList = if (step.transitDetails.line.color != null) {
                                ColorStateList.valueOf(Color.parseColor(step.transitDetails.line.color))
                            } else {
                                ColorStateList.valueOf(
                                    MaterialColors.getColor(
                                        requireContext(),
                                        android.R.attr.textColorPrimaryInverse,
                                        "android.R.attr.textColorPrimaryInverse error"
                                    )
                                )
                            }
                        }
                    }
                    val stepEmissionText: TextView = transitIconsContainer.findViewById(R.id.transit_step_emission)
                    stepEmissionText.text = CalculationUtils.formatEmission(stepEmission)
                    stepsIconContainer.addView(transitIconsContainer)
                }
                TravelMode.WALKING -> {
                    // walking icon
                    stepsIconContainer.addView(
                        ImageView(context).apply {
                            setImageResource(R.drawable.outline_directions_walk_24)
                            setColorFilter(
                                MaterialColors.getColor(
                                    context,
                                    android.R.attr.textColorSecondary,
                                    "android.R.attr.textColorSecondary error"
                                )
                            )
                        }
                    )
                }
                else -> {}
            }

            // next icon
            stepsIconContainer.addView(
                ImageView(context).apply {
                    setImageResource(R.drawable.outline_navigate_next_24)
                    setColorFilter(
                        MaterialColors.getColor(
                            context,
                            android.R.attr.textColorSecondary,
                            "android.R.attr.textColorSecondary error"
                        )
                    )
                }
            )
        }
        stepsIconContainer.removeViewAt(stepsIconContainer.childCount - 1)
        emissionText.text = CalculationUtils.formatEmission(totalEmissionInGram)
        return totalEmissionInGram
    }
}
