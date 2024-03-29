package com.hashcaller.app.view.ui.search

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hashcaller.app.R
import com.hashcaller.app.databinding.FragmentSearch2Binding
import com.hashcaller.app.view.ui.contacts.isDarkThemeOn
import com.hashcaller.app.view.utils.IDefaultFragmentSelection


class SearchFragment : Fragment(), IDefaultFragmentSelection, View.OnClickListener {
    private var _binding:FragmentSearch2Binding? = null
    private val binding get() = _binding!!
    private var isDflt = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSearch2Binding.inflate(layoutInflater, container, false)
        initListeners()
        configureAnimImage()
        return binding.root
    }

    private fun configureAnimImage() {
        if(context?.isDarkThemeOn() == true){
//            binding.gifImageView.setImageResource(R.drawable.lantern_anim_black)
        }else{
//            binding.gifImageView.setImageResource(R.drawable.lantern_anim)

        }
    }

    private fun initListeners() {
        binding.searchViewMain.setOnClickListener(this)

    }


    companion object {



    }

    override var isDefaultFgmnt: Boolean
        get() = isDflt
        set(value) {}

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.searchViewMain -> {
                startSearchActivity()
            }
        }
    }

    private fun startSearchActivity() {
        val intent = Intent(activity, SearchActivity::class.java)
        startActivity(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}