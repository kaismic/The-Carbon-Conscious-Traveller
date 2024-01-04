package com.mquniversity.tcct

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Date

class AirplaneQueryFragment : DialogFragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var mainLayout: LinearLayout

    private lateinit var inputLayouts: Array<TextInputLayout>
    private val regexes = arrayOf(
        Regex("^[a-zA-Z]{3}$"),
        Regex("^[a-zA-Z]{3}$"),
        Regex("^(\\d|[a-zA-Z]){2}$"),
        Regex("^\\d{1,4}$")
    )
    private val errorMessages = arrayOf(
        "Airport code must be consisted of 3 letter alphabets",
        "Airport code must be consisted of 3 letter alphabets",
        "Airline/carrier code must be consisted of 2 letter alphanumerics",
        "Flight number must be consisted of 1 to 4 digit numbers"
    )
    private val areInputFieldsFilled = arrayOf(false, false, false, false)
    private var departureDate: Date = Date()
    private lateinit var calBtn: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = (requireActivity() as MainActivity)
        mainActivity.onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    parentFragmentManager.popBackStack()
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(
            R.layout.airplane_query_container,
            container,
            false
        ) as CoordinatorLayout
        mainLayout = root.findViewById(R.id.main_layout)

        inputLayouts = arrayOf(
            mainLayout.findViewById(R.id.origin_airport_input_layout),
            mainLayout.findViewById(R.id.dest_airport_input_layout),
            mainLayout.findViewById(R.id.airline_input_layout),
            mainLayout.findViewById(R.id.flight_number_input_layout),
        )

        for (i in inputLayouts.indices) {
            inputLayouts[i].editText?.doOnTextChanged { text, _, _, _ ->
                if (text!!.matches(regexes[i])) {
                    inputLayouts[i].error = null
                    areInputFieldsFilled[i] = true
                } else {
                    inputLayouts[i].error = errorMessages[i]
                    areInputFieldsFilled[i] = false
                }
                calBtn.isEnabled = areInputFieldsFilled.all { it }
            }
        }

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Departure Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.now())
                    .build()
            )
            .build()

        val departureDateEdittext: TextInputEditText = mainLayout.findViewById(R.id.departure_date_edittext)
        val dateFormat = DateFormat.getDateFormat(context)
        departureDateEdittext.setText(dateFormat.format(departureDate))
        departureDateEdittext.setOnClickListener {
            datePicker.show(parentFragmentManager, null)
        }
        datePicker.addOnPositiveButtonClickListener {
            departureDate.time = datePicker.selection!!
            departureDateEdittext.setText(dateFormat.format(departureDate))
        }

        calBtn = mainLayout.findViewById(R.id.calculate_button)

        return root
    }
}