package com.nibble.hashcaller.view.ui.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivitySearchMainBinding

class SearchActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySearchMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}