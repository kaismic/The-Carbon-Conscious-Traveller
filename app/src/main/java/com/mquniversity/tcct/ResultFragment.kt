package com.mquniversity.tcct

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.errors.OverDailyLimitException
import com.google.maps.errors.OverQueryLimitException
import com.google.maps.errors.ZeroResultsException
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.TravelMode

abstract class ResultFragment: Fragment() {
    protected var isInitialized = false

    protected lateinit var rootScrollView: ScrollView
    protected lateinit var mainLayout: LinearLayout
    protected var iconResId: Int? = null

    private lateinit var mainActivity: MainActivity
    private lateinit var geoApiContext: GeoApiContext
    protected lateinit var calculationValues: CalculationValues

    private var currOrigin: Place? = null
    private var currDest: Place? = null
    protected lateinit var currRoutes: Array<DirectionsRoute>

    protected val emissionTexts: MutableList<TextView> = mutableListOf()
    protected val distTexts: MutableList<TextView> = mutableListOf()
    protected val durationTexts: MutableList<TextView> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!isInitialized) {
            rootScrollView = ScrollView(context)
            rootScrollView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
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
            mainActivity.bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            calculationValues = mainActivity.calculationValues
            geoApiContext = mainActivity.geoApiContext
        }
        return rootScrollView
    }

    protected abstract fun insertRouteResult(route: DirectionsRoute, i: Int)
    fun updateRouteResults() {
        // return if the current start and end location is same as before
        if (currOrigin != null && currDest != null
            && mainActivity.origin?.address == currOrigin?.address
            && mainActivity.destination?.address == currDest?.address) {
            return
        }
        mainLayout.removeAllViews()
        emissionTexts.clear()
        distTexts.clear()
        durationTexts.clear()

        val progressBar = ProgressBar(context)
        mainLayout.addView(progressBar)

        val errorText = MaterialTextView(requireContext())
        errorText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        val retryBtn = MaterialButton(ContextThemeWrapper(context, com.google.android.material.R.style.Widget_Material3_Button))
        retryBtn.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        retryBtn.text = getString(R.string.try_again)
        retryBtn.setOnClickListener {
            updateRouteResults()
        }
        try {
            val request = DirectionsApi.getDirections(geoApiContext, mainActivity.origin?.address, mainActivity.destination?.address)
                .mode(TravelMode.DRIVING)
                .alternatives(true)
            val response = request.await()

            mainLayout.removeAllViews()

            currRoutes = response.routes
            for (i in currRoutes.indices) {
                insertRouteResult(response.routes[i], i)
            }
            currOrigin = mainActivity.origin!!
            currDest = mainActivity.destination!!
        } catch (e: ZeroResultsException) {
            mainLayout.removeAllViews()
            errorText.text = getString(R.string.error_zero_results)
            mainLayout.addView(errorText)
        } catch (e: OverDailyLimitException) {
            mainLayout.removeAllViews()
            errorText.text = getString(R.string.error_daily_limit_exceeded)
            mainLayout.addView(errorText)
            mainLayout.addView(retryBtn)
        } catch (e: OverQueryLimitException) {
            mainLayout.removeAllViews()
            errorText.text = getString(R.string.error_query_limit_exceeded)
            mainLayout.addView(errorText)
            mainLayout.addView(retryBtn)
        } catch (e: Exception) {
            mainLayout.removeAllViews()
            errorText.text = getString(R.string.error_general)
            mainLayout.addView(errorText)
            mainLayout.addView(retryBtn)
        }
    }
}