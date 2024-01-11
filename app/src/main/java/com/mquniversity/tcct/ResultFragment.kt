package com.mquniversity.tcct

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.Polyline
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.maps.DirectionsApi
import com.google.maps.errors.ApiException
import com.google.maps.errors.OverDailyLimitException
import com.google.maps.errors.OverQueryLimitException
import com.google.maps.errors.ZeroResultsException
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.LatLng
import com.google.maps.model.TravelMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.UnknownHostException

abstract class ResultFragment: Fragment() {
    protected lateinit var rootScrollView: NestedScrollView
    protected lateinit var mainLayout: LinearLayout

    protected lateinit var mainActivity: MainActivity
    protected lateinit var calculationValues: CalculationValues

    private var currOrigin: Place? = null
    private var currDest: Place? = null

    protected lateinit var currRoutes: Array<DirectionsRoute>

    private var currPolylines: Array<Array<Polyline?>?> = arrayOf()
    private var lastClickedRoutePolylines: Array<Polyline?>? = null

    protected var resultLayouts: Array<LinearLayout?> = arrayOf()
    private var currSelectedResultLayout: LinearLayout? = null

    private var selectionIndicators: Array<View?> = arrayOf()
    private var currSelectedIndicator: View? = null

    protected var travelMode = TravelMode.DRIVING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootScrollView = NestedScrollView(requireContext())
        rootScrollView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        mainLayout = layoutInflater.inflate(
            R.layout.results_container,
            null,
            false
        ) as LinearLayout

        rootScrollView.addView(mainLayout)

        mainActivity = requireActivity() as MainActivity
        calculationValues = mainActivity.calculationValues
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return rootScrollView
    }

    private fun removePolylines() {
        mainLayout.post {
            for (polylines in currPolylines) {
                for (polyline in polylines!!) {
                    polyline?.remove()
                }
            }
        }
    }

    fun showPolylines() {
        mainLayout.post {
            for (polylines in currPolylines) {
                for (polyline in polylines!!) {
                    polyline?.isVisible = true
                }
            }
        }
    }

    fun hidePolylines() {
        mainLayout.post {
            for (polylines in currPolylines) {
                for (polyline in polylines!!) {
                    polyline?.isVisible = false
                }
            }
        }
    }

    fun findRouteIdxWithPolyline(selectedPolyline: Polyline): Int {
        var idx: Int? = null
        for (i in currPolylines.indices) {
            if (currPolylines[i]?.contains(selectedPolyline) == true) {
                idx = i
            }
        }
        return idx!!
    }

    protected open fun insertRouteResult(idx: Int): Float {
        // initialise polylines with unselected style
        for (i in currPolylines[idx]!!.indices) {
            currPolylines[idx]!![i] = mainActivity.insertPolyline(
                currRoutes[idx].legs[0].steps[i].polyline,
                resources.getColor(R.color.polyline_unselected, context?.theme),
                null
            )
        }

        mainLayout.addView(resultLayouts[idx])

        // insert selection indicator
        val selectionIndicator = View(context)
        selectionIndicators[idx] = selectionIndicator
        selectionIndicator.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            0.0625f
        ).apply {
            setMargins(0, 0, 16, 0)
        }
        selectionIndicator.setBackgroundResource(
            com.google.android.material.R.color.design_default_color_primary
        )
        selectionIndicator.visibility = View.INVISIBLE

        resultLayouts[idx]?.addView(selectionIndicator, 0)

        resultLayouts[idx]?.setOnClickListener {
            if (currSelectedResultLayout === resultLayouts[idx]) {
                return@setOnClickListener
            }
            currSelectedResultLayout = resultLayouts[idx]
            highlightRoute(idx)
            highlightResult(idx)
            mainActivity.bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        return 0f
    }

    fun highlightResult(idx: Int) {
        if (currSelectedIndicator != null) {
            currSelectedIndicator?.visibility = View.INVISIBLE
        }
        currSelectedIndicator = selectionIndicators[idx]
        selectionIndicators[idx]?.visibility = View.VISIBLE
    }

    protected fun areLocationsSameAsBefore(): Boolean {
        return (currOrigin != null && currDest != null
                && mainActivity.origin?.address == currOrigin?.address
                && mainActivity.dest?.address == currDest?.address)
    }

    open fun update(reload: Boolean) {
        mainLayout.removeAllViews()

        val progressBar = ProgressBar(context)
        mainLayout.addView(progressBar)

        val request = DirectionsApi.newRequest(mainActivity.geoApiContext)
            .origin(LatLng(mainActivity.origin?.latLng!!.latitude, mainActivity.origin?.latLng!!.longitude))
            .destination(LatLng(mainActivity.dest?.latLng!!.latitude, mainActivity.dest?.latLng!!.longitude))
            .mode(travelMode)
            .alternatives(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = request.await()
                removePolylines()
                currRoutes = response.routes
                if (this@ResultFragment is PublicTransportResultFragment) {
                    this@ResultFragment.fetchTransitIcons()
                }
                currPolylines = arrayOfNulls(currRoutes.size)
                for (i in currRoutes.indices) {
                    currPolylines[i] = arrayOfNulls(currRoutes[i].legs[0].steps.size)
                }
                currOrigin = mainActivity.origin!!
                currDest = mainActivity.dest!!
                resultLayouts = arrayOfNulls(currRoutes.size)
                selectionIndicators = arrayOfNulls(currRoutes.size)
                mainLayout.post {
                    mainLayout.removeAllViews()
                    val routeEmissions = FloatArray(currRoutes.size)
                    for (i in currRoutes.indices) {
                        routeEmissions[i] = insertRouteResult(i)
                    }
                    mainActivity.transportSelection.updateIcons(routeEmissions)
                    highlightRoute(0)
                    highlightResult(0)
                    mainLayout.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
                }
            } catch (e: Exception) {
                if (e !is ApiException && e !is UnknownHostException) {
                    throw e
                }
                val errorText = MaterialTextView(requireContext())
                errorText.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 32, 0, 32)
                    gravity = Gravity.CENTER
                }
                val retryBtn = MaterialButton(requireContext())
                retryBtn.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                }
                retryBtn.text = getString(R.string.try_again)
                retryBtn.setOnClickListener {
                    mainActivity.calculate(true)
                }
                mainLayout.post {
                    mainLayout.showDividers = LinearLayout.SHOW_DIVIDER_NONE
                    mainLayout.removeAllViews()
                    mainLayout.addView(errorText)
                    when (e) {
                        is ApiException -> {
                            if (e !is ZeroResultsException) {
                                mainLayout.addView(retryBtn)
                            }
                            when (e) {
                                is ZeroResultsException -> errorText.text = getString(R.string.error_zero_results)
                                is OverDailyLimitException -> errorText.text = getString(R.string.error_daily_limit_exceeded)
                                is OverQueryLimitException -> errorText.text = getString(R.string.error_query_limit_exceeded)
                                else -> errorText.text = getString(R.string.error_general)
                            }
                        }
                        is UnknownHostException -> {
                            errorText.text =
                                "Could not calculate results due to network error.\n" +
                                "Make sure you have a stable network connection."
                            mainLayout.addView(retryBtn)
                        }
                    }
                }
            }
        }.invokeOnCompletion {
            mainActivity.enableButtons(true)
        }
    }

    fun highlightRoute(idx: Int) {
        val selectedPolylinePolylines = currPolylines[idx]!!
        if (lastClickedRoutePolylines === selectedPolylinePolylines) {
            return
        }
        if (lastClickedRoutePolylines != null) {
            // reset style to unselected
            for (polyline in lastClickedRoutePolylines!!) {
                polyline?.zIndex = 0f
                polyline?.color = resources.getColor(R.color.polyline_unselected, context?.theme)
                polyline?.pattern = null
            }
        }
        lastClickedRoutePolylines = selectedPolylinePolylines
        
        val steps = currRoutes[idx].legs[0].steps
        for (i in steps.indices) {
            selectedPolylinePolylines[i]?.zIndex = 1f
            when (steps[i].travelMode) {
                TravelMode.TRANSIT -> {
                    selectedPolylinePolylines[i]?.color = Color.parseColor(steps[i].transitDetails.line.color)
                    selectedPolylinePolylines[i]?.pattern = null
                }
                TravelMode.DRIVING -> {
                    selectedPolylinePolylines[i]?.color = resources.getColor(
                        R.color.polyline_private_vehicle,
                        context?.theme
                    )
                    selectedPolylinePolylines[i]?.pattern = null
                }
                TravelMode.WALKING, TravelMode.BICYCLING -> {
                    selectedPolylinePolylines[i]?.color = resources.getColor(
                        R.color.polyline_private_vehicle,
                        context?.theme
                    )
                    selectedPolylinePolylines[i]?.pattern = listOf(Dot(), Gap(10f))
                }
                else -> {
                    selectedPolylinePolylines[i]?.color = resources.getColor(
                        R.color.polyline_unselected,
                        context?.theme
                    )
                    selectedPolylinePolylines[i]?.pattern = listOf(Dot(), Gap(10f))
                }
            }
        }
    }
}
