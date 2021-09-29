package com.hashcaller.app.view.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hashcaller.app.databinding.ActivityImmediateUpdateBinding

class ImmediateUpdateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImmediateUpdateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImmediateUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners()
    }

    private fun initListeners() {
        binding.btnUpdateApp.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.hashcaller.app"))
            startActivity(browserIntent)
            finish()
        }
    }
}