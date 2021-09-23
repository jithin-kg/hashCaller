package com.hashcaller.app.view.ui.getstarted.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.appintro.SlideBackgroundColorHolder
import com.hashcaller.app.R
import com.hashcaller.app.databinding.GettingStartedRespectPrivacyBinding
import com.hashcaller.app.view.ui.contacts.isDarkThemeOn

class GettingStartedRespectPrivacyFragment : Fragment(), SlideBackgroundColorHolder {

    private lateinit var binding: GettingStartedRespectPrivacyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = GettingStartedRespectPrivacyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override val defaultBackgroundColor: Int
        get() =  requireContext().getColor(R.color.gettingstarted_3)
    override fun setBackgroundColor(backgroundColor: Int) {
        binding.container.setBackgroundColor(backgroundColor)

    }
}