package com.hashcaller.app.view.ui.getstarted

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.appintro.AppIntro
import com.hashcaller.app.R
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.datastore.PreferencesKeys
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.auth.ActivityPhoneAuth
import com.hashcaller.app.view.ui.getstarted.fragments.GettingStartedFullFeaturedFragment
import com.hashcaller.app.view.ui.getstarted.fragments.GettingStartedPrivacyPolicy
import com.hashcaller.app.view.ui.getstarted.fragments.GettingStartedRespectPrivacyFragment
import com.hashcaller.app.view.ui.getstarted.fragments.GettingStartedSecurelyStoredFragment

class GettingStartedSliderActivity : AppIntro() {
    private lateinit var dataStoreRepository: DataStoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setImmersiveMode()
        isWizardMode = true
        showStatusBar(true)
        setStatusBarColor(getColor(R.color.colorBackground))
        setStatusBarColorRes(R.color.colorBackground)
        addSlide(GettingStartedPrivacyPolicy())
//        addSlide(GettingStartedFullFeaturedFragment())
        addSlide(GettingStartedRespectPrivacyFragment())
        addSlide(GettingStartedSecurelyStoredFragment())
        isColorTransitionsEnabled = true
        isSkipButtonEnabled = false
        setSwipeLock(true)
        isIndicatorEnabled = true

//        isIndicatorEnabled = false
        setIndicatorColor(ContextCompat.getColor(this, R.color.colorPrimary),ContextCompat.getColor(this, R.color.unselectedIndicator))
        setNextArrowColor(ContextCompat.getColor(this, R.color.colorPrimary))
        setBackArrowColor(ContextCompat.getColor(this, R.color.colorPrimary))
        setColorDoneText(ContextCompat.getColor(this, R.color.colorPrimary))
        dataStoreRepository = DataStoreRepository(tokeDataStore)

        setPreferences()
    }

    private fun setPreferences() {
        lifecycleScope.launchWhenStarted {
            dataStoreRepository.setBoolean( true, PreferencesKeys.RCV_NOT_BLK_CALL)
        }
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
//        startPhoneAuthActivity()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        startPhoneAuthActivity()
    }

    override fun onNextPressed(currentFragment: Fragment?) {
        if (currentFragment is GettingStartedPrivacyPolicy) {
//            isSkipButtonEnabled = true
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