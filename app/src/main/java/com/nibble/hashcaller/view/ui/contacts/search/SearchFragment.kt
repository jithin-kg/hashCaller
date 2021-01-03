package com.nibble.hashcaller.view.ui.contacts.search

import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.ContacInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.utils.ContactsViewModel
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment(), View.OnClickListener, View.OnFocusChangeListener,
    SearchView.OnQueryTextListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewSearch:View
    private lateinit  var searchViewmodel: SearchViewModel
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
        sharedElementEnterTransition=  TransitionInflater.from(activity).inflateTransition(R.transition.fragment_transition)
//        sharedElementEnterTransition=  TransitionInflater.from(activity).inflateTransition(R.transition.fragment_transition)
        this.searchViewmodel = ViewModelProvider(this, SearchInjectorUtil.provideUserInjectorUtil(
            this.requireContext()
        )).get(SearchViewModel::class.java)

        // Inflate the layout for this fragment
        viewSearch =  inflater.inflate(R.layout.fragment_search, container, false)

        return viewSearch
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
        observerPhoneHashValue()


    }

    private fun observerPhoneHashValue() {
        this.searchViewmodel.hashedPhoneNum.observe(viewLifecycleOwner, Observer {
            no->
            edtTextPhoneSearch.setText(no)
            shimmer_view_container.startShimmer()
        } )
    }

    private fun initListeners() {
        viewSearch.btnSampleTransition.setOnClickListener(this)
        viewSearch.searchViewPhoneNum.setOnQueryTextFocusChangeListener(this)
        searchViewPhoneNum.setOnQueryTextListener(this)


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "__SearchFragment"
    }

    override fun onClick(v: View?) {
       requireActivity().onBackPressed();
//        (activity as MainActivity).removeSearchFragment()
//        (activity as MainActivity).addFragmentsAgain()

    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        Log.d(TAG, "onFocusChange: $")
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
     return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
//        this.viewSearch.edtTextPhoneSearch.setText(newText)
        val secret = Secrets().managecipher(activity?.packageName!!, newText!!)
        this.searchViewmodel.search(secret!!)

        Log.d(TAG, "got secret $secret")
        //public key comes from server, saved in shared preferences
//        phone number is hashed and encoded in and while sending encrypted


        return true
    }


}