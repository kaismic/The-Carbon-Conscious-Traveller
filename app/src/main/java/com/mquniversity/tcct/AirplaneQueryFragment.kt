package com.mquniversity.tcct

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView

class AirplaneQueryFragment : DialogFragment() {
    private lateinit var mainLayout: LinearLayout
    private lateinit var headerText: MaterialTextView
    private lateinit var calBtn: MaterialButton

    // TODO CoordinatorLayout for nested scrolling

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainLayout = inflater.inflate(
            R.layout.query_container,
            container,
            false
        ) as LinearLayout

        mainLayout.dividerDrawable = GradientDrawable().apply {
            setSize(0, 32)
        }
        mainLayout.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE

        headerText = mainLayout.findViewById(R.id.header_text)
        headerText.text = getString(R.string.airplane)
        calBtn = mainLayout.findViewById(R.id.calculate_button)

        val originInput = layoutInflater.inflate(R.layout.text_input_template, mainLayout, false) as TextInputLayout
        originInput.hint = "3-letter alphabetic airport code, e.g. LHR."
        mainLayout.addView(originInput, mainLayout.childCount - 1)
        val destInput = layoutInflater.inflate(R.layout.text_input_template, mainLayout, false) as TextInputLayout
        destInput.hint = "3-letter alphabetic airport code, e.g. JFK."
        mainLayout.addView(destInput, mainLayout.childCount - 1)
        val airlineInput = layoutInflater.inflate(R.layout.text_input_template, mainLayout, false) as TextInputLayout
        airlineInput.hint = "2-letter alphanumeric airline code, e.g. 4Y."
        mainLayout.addView(airlineInput, mainLayout.childCount - 1)
        val flightNumberInput = layoutInflater.inflate(R.layout.text_input_template, mainLayout, false) as TextInputLayout
        flightNumberInput.hint = "Flight Number"
        mainLayout.addView(flightNumberInput, mainLayout.childCount - 1)
        val departureDateInput = layoutInflater.inflate(R.layout.text_input_template, mainLayout, false) as TextInputLayout
        departureDateInput.hint = "Departure Date"
        mainLayout.addView(departureDateInput, mainLayout.childCount - 1)

        return mainLayout
    }
}