package com.nibble.hashcaller.view.ui.call

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nibble.hashcaller.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CallHistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CallHistoryFragment : Fragment() {
    private lateinit var callHistoryFragment: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        callHistoryFragment = inflater.inflate(R.layout.fragment_call_history, container, false)
        return callHistoryFragment;
    }

    companion object {

    }
}