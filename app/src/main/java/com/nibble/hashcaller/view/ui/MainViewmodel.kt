package com.nibble.hashcaller.view.ui

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel

class MainViewmodel(application: Application):AndroidViewModel(application) {
    fun getActiveFragment(): Fragment? {
        return activeFragment
    }

    fun setActiveFragment(fragment: Fragment) {
        activeFragment = fragment
    }

    private var activeFragment : Fragment? = null
}