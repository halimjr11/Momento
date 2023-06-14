package com.nurhaqhalim.momento.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.nurhaqhalim.momento.R

class MoEditText : TextInputEditText {

    private lateinit var backgroundField: Drawable

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialize()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        background = backgroundField
        setTextColor(ContextCompat.getColor(context, R.color.edit_text_color))
        textSize = if (inputType-1 == InputType.TYPE_TEXT_FLAG_MULTI_LINE) 16f else 12f
        textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        gravity = if (inputType-1 == InputType.TYPE_TEXT_FLAG_MULTI_LINE) Gravity.TOP else Gravity.CENTER_VERTICAL
    }

    private fun initialize() {
        backgroundField = ContextCompat.getDrawable(context, R.drawable.background_edit_text)!!

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Do nothing.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    when (inputType - 1) {
                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS -> {
                            if (!Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches()) {
                                val error = resources.getString(R.string.validation_email_text)
                                showError(error)
                            }
                        }

                        InputType.TYPE_TEXT_VARIATION_PASSWORD -> {
                            if (s.toString().length < 8) {
                                val error = resources.getString(R.string.validation_password_text)
                                showError(error)
                            }
                        }
                    }
                } else {
                    val error = resources.getString(R.string.validation_required_text)
                    showError(error)
                }
            }

            override fun afterTextChanged(s: Editable) {
                // Do nothing.
            }
        })
    }

    private fun showError(string: String) {
        error = string
    }
}