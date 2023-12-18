package com.mquniversity.tcct

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView

abstract class QueryFragment: Fragment() {
    protected var isInitialized = false

    protected lateinit var mainLayout: LinearLayout
    protected lateinit var headerText: MaterialTextView
    protected lateinit var calBtn: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!isInitialized) {
            mainLayout = inflater.inflate(R.layout.query_view, container, false) as LinearLayout
            val shapeDivider = GradientDrawable()
            shapeDivider.setSize(0, 32)
            mainLayout.dividerDrawable = shapeDivider
            mainLayout.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE

            headerText = mainLayout.findViewById(R.id.header_text)

            calBtn = MaterialButton(ContextThemeWrapper(context, com.google.android.material.R.style.Widget_Material3_Button))
            calBtn.text = "Calculate"
            calBtn.isEnabled = false
            calBtn.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
            mainLayout.addView(calBtn)
        }

        return mainLayout
    }

    protected fun insertQuery(menu: TextInputLayout, label: String, options: Array<String>) {
        menu.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        menu.hint = label
        val dropDown = MaterialAutoCompleteTextView(ContextThemeWrapper(context, com.google.android.material.R.style.ThemeOverlay_Material3_AutoCompleteTextView_OutlinedBox))
        dropDown.inputType = InputType.TYPE_NULL
        dropDown.setSimpleItems(options)
        menu.addView(dropDown)
        mainLayout.addView(menu, mainLayout.childCount - 1)
    }
}