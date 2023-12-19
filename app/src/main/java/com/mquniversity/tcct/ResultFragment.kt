package com.mquniversity.tcct

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.google.android.libraries.places.api.model.Place
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsRoute

abstract class ResultFragment: Fragment() {
    protected var isInitialized = false

    protected lateinit var rootScrollView: ScrollView
    protected lateinit var mainLayout: LinearLayout
    protected var iconResId: Int? = null

    protected lateinit var mainActivity: MainActivity
    protected lateinit var geoApiContext: GeoApiContext
    protected lateinit var calculationValues: CalculationValues

    protected var currOrigin: Place? = null
    protected var currDest: Place? = null

    protected lateinit var currRoutes: Array<DirectionsRoute>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!isInitialized) {
            rootScrollView = inflater.inflate(R.layout.result_view, container, false) as ScrollView
            mainLayout = LinearLayout(context)
            mainLayout.orientation = LinearLayout.VERTICAL
            mainLayout.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // TODO? gray line divider
            val shapeDivider = GradientDrawable()
            shapeDivider.setSize(0, 32)
            mainLayout.dividerDrawable = shapeDivider
            mainLayout.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE

            rootScrollView.addView(mainLayout)

            mainActivity = requireActivity() as MainActivity
            calculationValues = mainActivity.calculationValues
            geoApiContext = mainActivity.geoApiContext
        }


        return rootScrollView
    }

    protected abstract fun insertRouteResult(route: DirectionsRoute, i: Int)
    open fun updateRouteResults(): Boolean {
        if (currOrigin != null && currDest != null) {
            if (mainActivity.origin?.address == currOrigin?.address
                ||
                mainActivity.destination?.address == currDest?.address) {
                return false
            }
        }
        return true
    }
}