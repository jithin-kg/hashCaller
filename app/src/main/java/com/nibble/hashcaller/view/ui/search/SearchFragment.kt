package com.nibble.hashcaller.view.ui.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.FragmentSearch2Binding
import com.nibble.hashcaller.view.utils.IDefaultFragmentSelection


class SearchFragment : Fragment(), IDefaultFragmentSelection {
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
        return binding.root
    }

    companion object {



    }

    override var isDefaultFgmnt: Boolean
        get() = isDflt
        set(value) {}
}