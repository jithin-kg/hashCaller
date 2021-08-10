package com.hashcaller.view.ui.call.floating

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hashcaller.R

class SampleActivityStartnotification : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_startnotification)
//        startFloatingService(intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER))
    }
}