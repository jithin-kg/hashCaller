package com.nibble.hashcaller.view.ui.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.FragmentSearch2Binding
import com.nibble.hashcaller.view.ui.call.CallFragment
import com.nibble.hashcaller.view.ui.call.search.CallLogSearchActivity
import com.nibble.hashcaller.view.utils.IDefaultFragmentSelection


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

        return binding.root
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