package com.nibble.hashcaller.view.ui.BlockConfig

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.adapter.ViewPagerAdapter
import com.nibble.hashcaller.view.ui.tabian.TabianActivit

import kotlinx.android.synthetic.main.block_config_fragment.*

/**
 * Created by Jithin KG on 03,July,2020
 */
//import database.sql.SQLiteDatabaseHandler;
class BlockConfigFragment : Fragment(), View.OnClickListener {
    //    private SQLiteDatabaseHandler db;



    var blockConfigFragment: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        blockConfigFragment = inflater.inflate(R.layout.block_config_fragment, container, false)



//        setSupportActionBar(toolbar);

        return blockConfigFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intialize()
        setupViewPager(viewPagerBlockConfig)
        tabLayoutBlockConfig?.setupWithViewPager(viewPagerBlockConfig)
    }

    private fun intialize() {

        fabBtnAddNewBlock?.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_add))
        fabBtnAddNewBlock?.setOnClickListener(this as View.OnClickListener)
    }

    private fun setupViewPager(viewPager: ViewPager?) {
        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
//        viewPagerAdapter.addFragment(BlkListFragment(), "BlockList")
        viewPagerAdapter.addFragment(BlkListFragment(), "Blk")
//        viewPagerAdapter.addFragment(BlockScheduleFragment(), "Schedule")
        viewPager?.adapter = viewPagerAdapter
    }

    override fun onClick(v: View?) {
        val i = Intent(context, ActivityCreteBlockListPattern::class.java)
//                i.putExtra("PersonID", personID);
        startActivity(i)
    }
}
