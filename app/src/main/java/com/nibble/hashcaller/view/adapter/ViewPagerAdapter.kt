package com.nibble.hashcaller.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import java.util.*

/**
 * Created by Jithin KG on 03,July,2020
 */
class ViewPagerAdapter(fm: FragmentManager?) :
    FragmentPagerAdapter(fm!!) {
    private val fragmentList = ArrayList<Fragment>()
    private val fragmentTitle = ArrayList<String>()
    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        fragmentTitle.add(title)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentTitle[position]
    }
}
