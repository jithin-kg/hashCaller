package com.nibble.hashcaller.view.ui.IncommingCall

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityIncommingCallFeedbackBinding

class ActivityIncommingCallFeedback : AppCompatActivity() {
    private lateinit var binding : ActivityIncommingCallFeedbackBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncommingCallFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}