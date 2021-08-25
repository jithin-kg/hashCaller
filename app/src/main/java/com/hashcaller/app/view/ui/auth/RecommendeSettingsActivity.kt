package com.hashcaller.app.view.ui.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hashcaller.app.databinding.ActivityRecommendeSettingsBinding

class RecommendeSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecommendeSettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecommendeSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}