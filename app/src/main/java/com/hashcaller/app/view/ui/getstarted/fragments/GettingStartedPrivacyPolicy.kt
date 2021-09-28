package com.hashcaller.app.view.ui.getstarted.fragments

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.appintro.SlideBackgroundColorHolder
import com.hashcaller.app.R
import com.hashcaller.app.databinding.FragmentGettingStartedPrivacyPolicyBinding
import com.hashcaller.app.view.ui.auth.ActivityPhoneAuth
import com.hashcaller.app.view.ui.contacts.isDarkThemeOn


class GettingStartedPrivacyPolicy : Fragment(), SlideBackgroundColorHolder {


    private lateinit var binding: FragmentGettingStartedPrivacyPolicyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGettingStartedPrivacyPolicyBinding.inflate(inflater, container, false)
//        binding.tvTermsAgree.setOnClickListener{startPrivacyIntent()}
        return binding.root
    }

    override val defaultBackgroundColor: Int
        get() = requireContext().getColor(R.color.colorBackground)

    override fun setBackgroundColor(backgroundColor: Int) {
//        binding.container.setBackgroundColor(backgroundColor)
    }


    private fun startPrivacyIntent() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://hashcaller.com/privacy"))
        startActivity(browserIntent)
    }


}