package com.nibble.hashcaller.view.ui.sms.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/**
 * Class to handle editText text change events
 */
class TextChangeListener(private val iListener:ITextChangeListener) {
    fun addListener(view:EditText){
        view.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                   iListener.onTextChanged(view.text.toString())
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }
}

interface ITextChangeListener {

    fun onTextChanged(text:String)
    fun afterTextChanged(s:Editable)
}