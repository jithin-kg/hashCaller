package com.hashcaller.app.view.ui.getstarted

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.hashcaller.app.R
import com.hashcaller.app.view.ui.auth.ActivityPhoneAuth
import com.hashcaller.app.view.ui.getstarted.fragments.GettingStartedFullFeaturedFragment
import com.hashcaller.app.view.ui.getstarted.fragments.GettingStartedPrivacyPolicy
import com.hashcaller.app.view.ui.getstarted.fragments.GettingStartedRespectPrivacyFragment
import com.hashcaller.app.view.ui.getstarted.fragments.GettingStartedSecurelyStoredFragment

class GettingStartedSliderActivity : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        addSlide(GettingStartedPrivacyPolicy())
        addSlide(GettingStartedFullFeaturedFragment())
        addSlide(GettingStartedRespectPrivacyFragment())
        addSlide(GettingStartedSecurelyStoredFragment())

        isColorTransitionsEnabled = true


        isSkipButtonEnabled = false
        setSwipeLock(true)
        isIndicatorEnabled = false


    }

    /***
     *
     * lottie icons used
     *

    https://lottiefiles.com/23706-phone-call - phone icon
    https://lottiefiles.com/45085-padlock
    https://lottiefiles.com/67171-data-center- server-icon

     */

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        startPhoneAuthActivity()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        startPhoneAuthActivity()
    }

    override fun onNextPressed(currentFragment: Fragment?) {
        if (currentFragment is GettingStartedPrivacyPolicy) {
            isSkipButtonEnabled = true
            setSwipeLock(false)
            isIndicatorEnabled = true
        }
    }


    private fun startPhoneAuthActivity() {
        val i = Intent(this, ActivityPhoneAuth::class.java)
        startActivity(i)

        overridePendingTransition(
            R.anim.in_anim,
            R.anim.out_anim
        );
        finish()

    }

}