package com.hashcaller.view.ui.IncommingCall

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hashcaller.databinding.ActivityIncommingCallFeedbackBinding

class ActivityIncommingCallFeedback : AppCompatActivity() {
    private lateinit var binding : ActivityIncommingCallFeedbackBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncommingCallFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}