package com.mquniversity.tcct

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
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
    private lateinit var resultTable: ConstraintLayout
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
        resultTable = root.findViewById(R.id.airplane_result_table)
        for (textView in resultTable.children) {
            (textView as MaterialTextView).textSize = 24f
        }

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
                    val first: MaterialTextView = resultTable.findViewById(R.id.airplane_r1c2)
                    val business: MaterialTextView = resultTable.findViewById(R.id.airplane_r2c2)
                    val premiumEconomy: MaterialTextView = resultTable.findViewById(R.id.airplane_r3c2)
                    val economy: MaterialTextView = resultTable.findViewById(R.id.airplane_r4c2)
                    first.text = response.flightEmissions[0].emissionsGramsPerPax.first.toString()
                    business.text = response.flightEmissions[0].emissionsGramsPerPax.business.toString()
                    premiumEconomy.text = response.flightEmissions[0].emissionsGramsPerPax.premiumEconomy.toString()
                    economy.text = response.flightEmissions[0].emissionsGramsPerPax.economy.toString()
                } else {

                }

                // TODO
                progressBar.visibility = View.GONE
                resultLayout.visibility = View.VISIBLE
            }
        }
    }
}