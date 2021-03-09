package com.nibble.hashcaller.view.ui.sms.search

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.sms.list.SMSListInjectorUtil
import com.nibble.hashcaller.view.ui.sms.util.ITextChangeListener
import com.nibble.hashcaller.view.ui.sms.util.SMSViewModel
import com.nibble.hashcaller.view.ui.sms.util.TextChangeListener
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity(), ITextChangeListener {
    private lateinit var editTextListener: TextChangeListener
    private lateinit var viewmodel:SMSSearchViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        initListeners()

        initViewModel()
    }



    private fun initViewModel() {
        viewmodel = ViewModelProvider(this, SmsSearchInjectorUtil.provideDialerViewModelFactory(this)).get(
            SMSSearchViewModel::class.java)
    }

    private fun initListeners() {
        editTextListener = TextChangeListener(this)
        editTextListener.addListener(searchVSms)

    }

    override fun onTextChanged(text: String) {
        Log.d(TAG, "onTextChanged: $text")
        viewmodel.search(text)
    }

    override fun afterTextChanged(s: Editable) {
        Log.d(TAG, "afterTextChanged: $s")
    }
    companion object{
        const val TAG = "__SearchActivity"
    }
}