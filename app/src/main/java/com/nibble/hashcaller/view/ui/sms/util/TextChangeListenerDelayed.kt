package com.nibble.hashcaller.view.ui.sms.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TextChangeListenerDelayed(private val iListener:ITextChangeListenerDelayed) {
    fun addListener(view: EditText){
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
                GlobalScope.launch {
                    delay(300L)
                    iListener.onTextChanged(view.text.toString())
                }

            }

            override fun afterTextChanged(s: Editable) {
                iListener.afterTextChanged(s)
            }
        })
    }
}

interface ITextChangeListenerDelayed {

    fun onTextChanged(text:String)
    fun afterTextChanged(s: Editable)
}