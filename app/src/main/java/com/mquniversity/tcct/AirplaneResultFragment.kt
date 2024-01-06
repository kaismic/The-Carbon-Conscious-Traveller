package com.mquniversity.tcct

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.view.get
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class AirplaneResultFragment(private val requestBody: RequestBody) : DialogFragment()  {
    private lateinit var root: LinearLayout
    private lateinit var resultLayout: LinearLayout
    private lateinit var resultDescLayout: LinearLayout
    private lateinit var resultValueLayout: LinearLayout
    private lateinit var progressBar: ProgressBar

    private val requestURL = "https://travelimpactmodel.googleapis.com/v1/flights:computeFlightEmissions?key="
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = layoutInflater.inflate(
            R.layout.airplane_result_container,
            null,
            false
        ) as LinearLayout

        resultLayout = root.findViewById(R.id.airplane_result_layout)
        resultDescLayout = resultLayout.findViewById(R.id.airplane_result_desc_layout)
        resultValueLayout = resultLayout.findViewById(R.id.airplane_result_value_layout)
        for (i in 0 until 4) {
            val textView = MaterialTextView(requireContext())
            textView.textSize = 24f
            resultDescLayout.addView(textView)
        }
        for (i in 0 until 4) {
            val textView = MaterialTextView(requireContext())
            textView.textSize = 24f
            resultValueLayout.addView(textView)
        }
        (resultDescLayout[0] as MaterialTextView).text = "First"
        (resultDescLayout[1] as MaterialTextView).text = "Business"
        (resultDescLayout[2] as MaterialTextView).text = "Premium Economy"
        (resultDescLayout[3] as MaterialTextView).text = "Economy"

        val calAgainBtn: MaterialButton = root.findViewById(R.id.calculate_again_button)
        calAgainBtn.setOnClickListener {
            showResult()
        }
        progressBar = root.findViewById(R.id.airplane_progress_bar)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        showResult()
        return root
    }

    private fun showResult() {
        CoroutineScope(Dispatchers.IO).launch {
            root.post {
                resultLayout.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            }
            var response: ResponseBody? = null
            val connection = URL(requestURL + (requireActivity() as MainActivity).apiKey).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("charset", "utf-8")
            connection.setRequestProperty("Content-Type", "application/json")
            val json = gson.toJson(requestBody)
            val byteArray = json.toByteArray()
            connection.setRequestProperty("Content-length", byteArray.size.toString())
            val outputStream = DataOutputStream(connection.outputStream)
            outputStream.write(byteArray)
            outputStream.flush()
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = DataInputStream(connection.inputStream)
                val reader = BufferedReader(InputStreamReader(inputStream))
                response = gson.fromJson(reader.readText(), ResponseBody::class.java)
                reader.close()
                inputStream.close()
            } else {
                Log.i("AirplaneQueryFragment getFlightEmission HttpRequest error", connection.responseMessage)
                Log.i("AirplaneQueryFragment getFlightEmission HttpRequest error", connection.responseCode.toString())
            }
            connection.disconnect()

            root.post {
                if (response != null) {
                    if (response.flightEmissions[0].emissionsGramsPerPax != null) {
                        (resultValueLayout[0] as MaterialTextView).text = response.flightEmissions[0].emissionsGramsPerPax?.first.toString()
                        (resultValueLayout[1] as MaterialTextView).text = response.flightEmissions[0].emissionsGramsPerPax?.business.toString()
                        (resultValueLayout[2] as MaterialTextView).text = response.flightEmissions[0].emissionsGramsPerPax?.premiumEconomy.toString()
                        (resultValueLayout[3] as MaterialTextView).text = response.flightEmissions[0].emissionsGramsPerPax?.economy.toString()
                    }
                } else {

                }

                // TODO error and no result handling
                progressBar.visibility = View.GONE
                resultLayout.visibility = View.VISIBLE
            }
        }
    }
}