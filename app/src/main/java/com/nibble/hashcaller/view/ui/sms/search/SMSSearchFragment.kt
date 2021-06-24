package com.nibble.hashcaller.view.ui.sms.search

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.SimpleItemAnimator
import com.nibble.hashcaller.databinding.ContactSearchResultItemBinding
import com.nibble.hashcaller.databinding.FragmentSMSSearchBinding
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.call.dialer.DialerAdapter
import com.nibble.hashcaller.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.nibble.hashcaller.view.ui.contacts.hasReadContactsPermission
import com.nibble.hashcaller.view.ui.search.AllSearchInjectorUtil
import com.nibble.hashcaller.view.ui.search.AllSearchViewmodel
import com.nibble.hashcaller.view.ui.sms.individual.util.beGone
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.ui.sms.util.ITextChangeListenerDelayed
import com.nibble.hashcaller.view.ui.sms.util.TextChangeListenerDelayed
import com.nibble.hashcaller.view.utils.IDefaultFragmentSelection


class SMSSearchFragment : Fragment() , IDefaultFragmentSelection, ITextChangeListenerDelayed {
    private var isDflt = false
    private lateinit var binding: FragmentSMSSearchBinding
    private  var searchAdapter: DialerAdapter? = null
    private  var fullContactAdapter: DialerAdapter? = null
    private lateinit var editTextListener: TextChangeListenerDelayed
    private var queryStr = ""
    private lateinit var allSearchViewmodel: AllSearchViewmodel
    private var isFirstTimeOpening = true



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSMSSearchBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initListeners()

    }


    private fun observeSearchResult() {
    allSearchViewmodel.contactsSearchListLivedata.observe(viewLifecycleOwner, Observer {
        searchAdapter?.setList(it)
        binding.pgBarSMSSearch.beGone()
        if(it.isNotEmpty()) {
            binding.reclrSmsSearchResult.beVisible()
            binding.tvNoResult.beGone()
        }else {
            binding.reclrSmsSearchResult.beGone()
            binding.tvNoResult.beVisible()
        }
    })
    }
    private fun observeContactsList() {
        allSearchViewmodel.contactsListOfLivedata.observe(viewLifecycleOwner, Observer {
              if (isFirstTimeOpening){
                  fullContactAdapter?.setList(it)
                  isFirstTimeOpening = false
                  binding.pgBarSMSSearch.beGone()
                  if(it.isNotEmpty()) {
                      binding.reclrSmsSearchResult.beVisible()
                      binding.tvNoResult.beGone()
                  }else {
                      binding.reclrSmsSearchResult.beGone()
                      binding.tvNoResult.beVisible()
                  }
              }


        })
    }

    private fun initContactsList() {
        allSearchViewmodel.initContactsList()
    }

    private fun initViewModel() {
        allSearchViewmodel = ViewModelProvider(
            this, AllSearchInjectorUtil.provideViewModelFactory(
                requireContext())
        ).get(AllSearchViewmodel::class.java)
    }

    override fun onTextChanged(newText: String) {
        Log.d(TAG, "onTextChanged: ")
        activity?.runOnUiThread {
            queryStr = newText
            if(queryStr.isNotEmpty()) {
                binding.reclrFullContacts.beGone()
                binding.reclrSmsSearchResult.beVisible()
                binding.pgBarSMSSearch.beVisible()
                allSearchViewmodel.onQueryTextChanged(newText.toLowerCase(), true)
            }else {
                binding.reclrFullContacts.beVisible()
                binding.reclrSmsSearchResult.beGone()
                allSearchViewmodel.setFullContactsList()
                binding.pgBarSMSSearch.beInvisible()
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        binding.reclrSmsSearchResult.beGone()
        binding.reclrFullContacts.beVisible()
        if(!hidden && !this::allSearchViewmodel.isInitialized){
            if (requireContext()?.hasReadContactsPermission()) {
                initViewModel()
                initContactsList()
                observeSearchResult()
                observeContactsList()


            }
        }
        else if(!hidden  && this::allSearchViewmodel.isInitialized && !binding.edtTextSMSSearch.text.isNullOrEmpty()){
//            initContactsList()
            binding.edtTextSMSSearch.setText("")
////            allSearchViewmodel.setFullContactsList()
        }

    }



    override fun afterTextChanged(s: Editable) {
    }

    private fun initListeners() {
        editTextListener = TextChangeListenerDelayed(this)
        editTextListener.addListener( binding.edtTextSMSSearch)

    }



    private fun initRecyclerView() {
              searchAdapter =    DialerAdapter(requireContext()) { binding: ContactSearchResultItemBinding, contact: Contact, clickType: Int ->
                onContactItemClicked(
                    binding,
                    contact,
                    clickType
                )



            }
        fullContactAdapter =    DialerAdapter(requireContext()) { binding: ContactSearchResultItemBinding, contact: Contact, clickType: Int ->
            onContactItemClicked(
                binding,
                contact,
                clickType
            )
        }
        binding.reclrSmsSearchResult.layoutManager = CustomLinearLayoutManager(requireContext())
        binding.reclrSmsSearchResult.adapter = this.searchAdapter
        binding.reclrSmsSearchResult.itemAnimator = null

        binding.reclrFullContacts.layoutManager = CustomLinearLayoutManager(requireContext())
        binding.reclrFullContacts.adapter = this.fullContactAdapter
    }
    private fun onContactItemClicked(
        binding: ContactSearchResultItemBinding,
        contactItem: Contact,
        clickType: Int
    ){


    }
    companion object {

        @JvmStatic
        fun newInstance() =
            SMSSearchFragment()

        const val TAG = "__SMSSearchFragment"
    }


    override var isDefaultFgmnt: Boolean
        get() = isDflt
        set(value) {isDflt = value}
}