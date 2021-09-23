package com.hashcaller.app.view.ui.getstarted.fragments

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.appintro.SlideBackgroundColorHolder
import com.hashcaller.app.R
import com.hashcaller.app.databinding.GettingStartedRespectPrivacyBinding
import com.hashcaller.app.view.ui.contacts.isDarkThemeOn

class GettingStartedRespectPrivacyFragment : Fragment(), SlideBackgroundColorHolder,
    View.OnClickListener {

    private lateinit var binding: GettingStartedRespectPrivacyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = GettingStartedRespectPrivacyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        binding.btnLearnMore.setOnClickListener(this)
    }

    override val defaultBackgroundColor: Int
        get() =  requireContext().getColor(R.color.colorBackground)
    override fun setBackgroundColor(backgroundColor: Int) {
        binding.container.setBackgroundColor(backgroundColor)

    }

    override fun onClick(v: View?) {
       when(v?.id){
           R.id.btnLearnMore -> {
               val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://hashcaller.com/privacy"))
               startActivity(browserIntent)
           }
       }
    }
}