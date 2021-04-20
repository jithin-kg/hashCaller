package com.nibble.hashcaller.view.ui.call.individualCallLog

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityIndividualCallLogBinding
import com.nibble.hashcaller.view.ui.call.search.CallLogSearchViewModel
import com.nibble.hashcaller.view.ui.call.search.CalllogSearchInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES

class IndividualCallLogActivity : AppCompatActivity() {
    lateinit var binding : ActivityIndividualCallLogBinding
    private lateinit var viewmodel: IndividualCallViewModel
    private var num:String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIndividualCallLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        num  = intent.getStringExtra(CONTACT_ADDRES)
        initViewmodel()
        observeCallLog()

    }

    @SuppressLint("LongLogTag")
    private fun observeCallLog() {
        viewmodel.callLogLiveData.observe(this, Observer {

        })
    }

    private fun initViewmodel() {
        viewmodel = ViewModelProvider(this, IndividualCallLogInjectorUtil.provideDialerViewModelFactory(this)).get(
            IndividualCallViewModel::class.java)

    }
    companion object{
        const val TAG ="__IndividualCallLogActivity"
    }
}