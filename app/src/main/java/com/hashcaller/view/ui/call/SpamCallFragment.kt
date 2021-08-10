package com.hashcaller.view.ui.call

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hashcaller.R
import com.skydoves.expandablelayout.ExpandableLayout
import com.skydoves.expandablelayout.expandableLayout
import kotlinx.android.synthetic.main.fragment_spam_call.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SpamCallFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SpamCallFragment : Fragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var myExpandableLayout:ExpandableLayout? = null

    private lateinit var adapter:SpamCallAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_spam_call, container, false)
    }

    companion object {
        const val TAG = "__SpamCallFragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SpamCallFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SpamCallFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnDefaulCallScreening.setOnClickListener(this)
         myExpandableLayout = expandableLayout(this!!.requireContext()) {
            setParentLayoutResource(R.layout.expandable_parent)
            setSecondLayoutResource(R.layout.layout_second)
            setShowSpinner(true)
            setSpinnerAnimate(true)
            setSpinnerMargin(12f)
            setSpinnerRotation(90)
            setDuration(200)
            setOnExpandListener { Log.d(TAG, "onViewCreated: clicked") }
        }
    }

    override fun onClick(v: View?) {
        myExpandableLayout!!.expand()
    }



}