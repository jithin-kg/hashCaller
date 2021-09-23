package com.hashcaller.app.view.ui.getstarted.fragments

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.appintro.SlideBackgroundColorHolder
import com.github.appintro.SlidePolicy
import com.hashcaller.app.R
import com.hashcaller.app.databinding.GettingStartedSecurelyStoredyBinding
import com.hashcaller.app.view.ui.sms.individual.util.toast

class GettingStartedSecurelyStoredFragment :Fragment(), SlideBackgroundColorHolder , SlidePolicy {

    private lateinit var binding : GettingStartedSecurelyStoredyBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding =  GettingStartedSecurelyStoredyBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvAgree.setOnClickListener { startPrivacyIntent() }
        binding.tvPrivacyMore.setOnClickListener { startPrivacyIntent() }
    }

    private fun startPrivacyIntent() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://hashcaller.com/privacy"))
        startActivity(browserIntent)
    }

    override val defaultBackgroundColor: Int
        get() = requireContext().getColor(R.color.colorBackground)

    override fun setBackgroundColor(backgroundColor: Int) {
//        binding.container.setBackgroundColor(backgroundColor)
    }

    override val isPolicyRespected: Boolean

        get() = isPolicyCompleted()


    fun isPolicyCompleted():Boolean{

        return binding.cbAggree.isChecked
    }
    override fun onUserIllegallyRequestedNextPage() {
        requireContext().toast("Please click agree to continue")
        Log.d(TAG, "onUserIllegallyRequestedNextPage: ")
    }

    companion object {
        const val TAG = "__GettingStartedSecurelyStoredFragment"
    }

}