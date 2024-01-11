package com.mquniversity.tcct

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar

class AirplaneQueryFragment : DialogFragment() {
    private lateinit var backPressedCallback: OnBackPressedCallback

    private lateinit var root: LinearLayout
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

    private val departureDate = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            backPressedCallback
        )

        root = layoutInflater.inflate(
            R.layout.airplane_query_container,
            null,
            false
        ) as LinearLayout
        inputLayouts = arrayOf(
            root.findViewById(R.id.origin_airport_input_layout),
            root.findViewById(R.id.dest_airport_input_layout),
            root.findViewById(R.id.airline_input_layout),
            root.findViewById(R.id.flight_number_input_layout),
            root.findViewById(R.id.departure_date_layout),
        )

        val calBtn: MaterialButton = root.findViewById(R.id.calculate_button)
        calBtn.setOnClickListener {
            // prevent accidental double click
            calBtn.isEnabled = false
            // disable all inputLayouts to remove focus in case if user was editing text
            inputLayouts.map { it.isEnabled = false }
            AirplaneResultFragment(getRequestBody()).show(parentFragmentManager, null)
            inputLayouts.map { it.isEnabled = true }
            calBtn.isEnabled = true
        }

        for (i in 0..<inputLayouts.size - 1) {
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

        val departureDateEdittext = inputLayouts[4].editText!!
        val dateFormat = DateFormat.getDateFormat(context)
        departureDateEdittext.setText(dateFormat.format(departureDate.time))
        departureDateEdittext.setOnClickListener {
            datePicker.show(parentFragmentManager, null)
        }
        datePicker.addOnPositiveButtonClickListener {
            departureDate.time = java.util.Date(datePicker.selection!!)
            departureDateEdittext.setText(dateFormat.format(datePicker.selection!!))
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return root
    }

    private fun getRequestBody(): RequestBody {
        return RequestBody(
            arrayOf(
                Flight(
                    inputLayouts[0].editText?.text.toString(),
                    inputLayouts[1].editText?.text.toString(),
                    inputLayouts[2].editText?.text.toString(),
                    inputLayouts[3].editText?.text.toString().toInt(),
                    Date(
                        departureDate.get(Calendar.YEAR),
                        departureDate.get(Calendar.MONTH) + 1, // inconsistency cause why not lol
                        departureDate.get(Calendar.DAY_OF_MONTH)
                    )
                )
            )
        )
    }

    override fun onDestroy() {
        backPressedCallback.remove()
        super.onDestroy()
    }

}