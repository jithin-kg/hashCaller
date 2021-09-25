package com.hashcaller.app.view.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ActivityCreditsActvityBinding

class CreditsActvity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityCreditsActvityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreditsActvityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners()
    }

    private fun initListeners() {
        binding.tvAndroidOpensource.setOnClickListener(this)
        binding.tvAp1.setOnClickListener(this)
        binding.tvAp2.setOnClickListener(this)
        binding.tvAp3.setOnClickListener(this)
        binding.tvAp4.setOnClickListener(this)
        binding.tvAp5.setOnClickListener(this)
        binding.tvAp6.setOnClickListener(this)
        binding.tvAp7.setOnClickListener(this)
        binding.tvAp8.setOnClickListener(this)
        binding.tvAp9.setOnClickListener(this)
        binding.tvAp10.setOnClickListener(this)
        binding.tvAp11.setOnClickListener(this)
        binding.tvAp12.setOnClickListener(this)
        binding.tvAp13.setOnClickListener(this)
        binding.tvAp14.setOnClickListener(this)
        binding.tvAp16.setOnClickListener(this)
        binding.tvMit1.setOnClickListener(this)
        binding.tvAttr1.setOnClickListener(this)
        binding.tvAttr3Lotti.setOnClickListener(this)
//        binding.tvAp15.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.tvAttr3Lotti -> {
                onLicenceClicked("https://lottiefiles.com/45085-padlock")
            }
            R.id.tvAp1 -> {
                onLicenceClicked("https://github.com/square/retrofit")
            }
            R.id.tvAndroidOpensource -> {
                onLicenceClicked("https://source.android.com/")
            }
            R.id.tvAp2 -> {
                onLicenceClicked("https://github.com/klinker41/android-smsmms")
            }
            R.id.tvAp3 -> {
                onLicenceClicked("https://developer.android.com/jetpack")
            }
            R.id.tvAp4 -> {
                onLicenceClicked("https://github.com/google/play-services-plugins")
            }
            R.id.tvAp5 -> {
                onLicenceClicked("https://github.com/google/libphonenumber")
            }
            R.id.tvAp6 -> {
                onLicenceClicked("https://github.com/firebase")
            }
            R.id.tvAp7 -> {
                onLicenceClicked("https://github.com/square/okhttp")
            }
            R.id.tvAp8 -> {
                onLicenceClicked("https://github.com/JetBrains/kotlin")
            }
            R.id.tvAp9 -> {
                onLicenceClicked("https://github.com/bumptech/glide")
            }
            R.id.tvAp10 -> {
                onLicenceClicked("https://github.com/material-components/material-components-android")
            }
            R.id.tvAp11 -> {
                onLicenceClicked("https://github.com/hdodenhof/CircleImageView")
            }
            R.id.tvAp12 -> {
                onLicenceClicked("https://github.com/zetbaitsu/Compressor")
            }
            R.id.tvAp13 -> {
                onLicenceClicked("https://github.com/hbb20/CountryCodePickerProject")
            }
            R.id.tvAp14 -> {
                onLicenceClicked("https://github.com/Kotlin/kotlinx.coroutines")
            }
            R.id.tvAp16 -> {
                onLicenceClicked("https://github.com/Remix-Design/RemixIcon")
            }
            R.id.tvMit1 -> {
                onLicenceClicked("https://github.com/klaxit/hidden-secrets-gradle-plugin")
            }
            R.id.tvAttr1 -> {
                onLicenceClicked("https://storyset.com/work")
            }
            
            
        }
    }

    private fun onLicenceClicked(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }
}