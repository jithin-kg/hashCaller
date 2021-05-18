package com.nibble.hashcaller.view.ui.contacts.search

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivitySearchContactBinding
import com.nibble.hashcaller.databinding.ContactListBinding
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.utils.crypto.KeyManager
import com.nibble.hashcaller.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.nibble.hashcaller.view.ui.contacts.ContactAdapter
import com.nibble.hashcaller.view.ui.contacts.individualContacts.IndividualContactViewActivity
import com.nibble.hashcaller.view.ui.contacts.search.utils.KeyboardUtils
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ID
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.ui.sms.util.ITextChangeListener
import com.nibble.hashcaller.view.ui.sms.util.TextChangeListener
import java.util.*


class ActivitySerchContacts : AppCompatActivity(), View.OnClickListener ,
    ITextChangeListener,
    View.OnFocusChangeListener {
//    private lateinit var searchViewModel: SearchViewModel
//    private lateinit var contactsSearchAdapter: SearchAdapter
    private lateinit var binding: ActivitySearchContactBinding
    private lateinit var contactsSearchAdapterLocal: ContactAdapter
//    private lateinit var owner:ActivitySearchPhone
//    private lateinit var  recyclerViewLocal:RecyclerView
    private lateinit var editTextListener: TextChangeListener
    private var queryStr = ""

    private lateinit  var searchViewmodel: SearchViewModel
    private var key:String? = null



    companion object{
        private const val TAG = "__ActivitySearchPhone"
        var num1 = 0
        var num2 = 0
        var num3 = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val enterAnimation = TransitionInflater.from(this).inflateTransition(R.transition.explode)

//        enterAnimation.setDuration(1000)
//        window.enterTransition = enterAnimation
        binding = ActivitySearchContactBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewmodel()

        initLayoutItems()
        binding.progressBar.beInvisible()
        requestPreventSoftInputMovingLayoutItems()
        showKeyboardOnLoad()
//        setHasOptionsMenu(true) //
        key = KeyManager.getKey(this)

        initListeners()
//        initAdapter()
        observerPhoneHashValue()
        observeSearchResults()
//        observeLocalSearchResults()
        initAdapter()
    }

    private fun initViewmodel() {
        this.searchViewmodel = ViewModelProvider(
            this, SearchInjectorUtil.provideUserInjectorUtil(
                applicationContext,
                null,
            )).get(SearchViewModel::class.java)
    }

    private fun initAdapter() {

         this.contactsSearchAdapterLocal = ContactAdapter(applicationContext) { binding: ContactListBinding, contact: Contact ->onContactItemClicked(binding, contact)}

        binding.reclrVResultFull.setHasFixedSize(true)
        binding.reclrVResultFull.layoutManager = CustomLinearLayoutManager(this)
        binding.reclrVResultFull.adapter = contactsSearchAdapterLocal

        binding.reclrVResultFull.setHasFixedSize(true)
        binding.reclrVResultFull.layoutManager = LinearLayoutManager(this)
        binding.reclrVResultFull.adapter = contactsSearchAdapterLocal
    }

    private fun onContactItemClicked(listBinding: ContactListBinding, contact: Contact) {

        val intent = Intent(this, IndividualContactViewActivity::class.java )
        intent.putExtra(CONTACT_ID, contact.phoneNumber)
        intent.putExtra("name", contact.name )
//        intent.putExtra("id", contactItem.id)
        intent.putExtra("photo", contact.photoURI)
        intent.putExtra("color", contact.drawable)
        Log.d(TAG, "onContactItemClicked: ${contact.photoURI}")
        val pairList = ArrayList<android.util.Pair<View, String>>()
//        val p1 = android.util.Pair(imgViewCntct as View,"contactImageTransition")
        var pair:android.util.Pair<View, String>? = null
        if(contact.photoURI.isEmpty()){
            pair = android.util.Pair(listBinding.textViewcontactCrclr as View, "firstLetterTransition")
        }else{
            pair = android.util.Pair(listBinding.imgViewCntct as View,"contactImageTransition")
        }
        pairList.add(pair)
        val options = ActivityOptions.makeSceneTransitionAnimation(this,pairList[0])
        startActivity(intent, options.toBundle())

    }


    /**
     * initialises view elements on OncreateView otherwise kotlin synthetic binding causing nullPointer exceptions
     */
    private fun initLayoutItems() {

    }

    private fun showKeyboardOnLoad() {


//        searchViewPhoneNum2.isIconifiedByDefault = true;
        binding.searchVCallSearch.isFocusable = true;
//        searchViewPhoneNum2.isIconified = false;
        binding.searchVCallSearch.requestFocusFromTouch();

    }



//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        Log.d(SearchFragment.TAG, "onCreateOptionsMenu: ")
//        inflater.inflate(R.menu.search_menu, menu)
//        super.onCreateOptionsMenu(menu)
//    }

    private fun requestPreventSoftInputMovingLayoutItems() {
//        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    private fun observerPhoneHashValue() {
        this.searchViewmodel.hashedPhoneNum.observe(this, Observer {
                no->
            binding.searchVCallSearch.setText(no)
            binding.tvQueryItem.text = "Searching for"
        } )
    }

    private fun initListeners() {

        editTextListener = TextChangeListener(this)
        editTextListener.addListener(binding.searchVCallSearch)

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.imgBtnSearchBack ->{
                closeSearchFragment()
            }
        }
    }

    private fun onBackButtonPressed() {
        super.onBackPressed()

    }



    private fun closeSearchFragment() {
        KeyboardUtils.hideKeyboard(this, binding.searchVCallSearch.windowToken)
        onBackPressed()
    }

    /**
     * shows keyboard
     */
    private fun showSoftInput() {
//        val imgr =
//            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imgr.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.RESULT_SHOWN);
    }



    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        Log.d(TAG, "onFocusChange: $")
    }

//    override fun onQueryTextSubmit(query: String?): Boolean {
//        return true
//    }

//    override fun onQueryTextChange(newText: String?): Boolean {
////        this.viewSearch.edtTextPhoneSearch.setText(newText)
////        tvSearchIndicator.text = ""
////        if(!newText.isNullOrEmpty()){
////            layoutSearchResult.visibility = View.GONE
////            this.searchViewmodel.search(newText!!, key, packageName)
////            this.searchViewmodel.searchContactsInDb(newText.toString()).observe(this, Observer {
////                Log.d(TAG, "onQueryTextChange: result from contact cprovider $it")
////                if(it!=null){
////                    setAdapterLocal(it!!)
////                }
////            })
////
////        }else{
////
////
////        }
//        return true
//    }
    private fun observeLocalSearchResults() {
//        this.searchViewmodel.searchResultsLivedata.observe(this, Observer {
//            it.let {
//                if(it!=null){
//
//                    Log.d(TAG, "onCreate: ${it}")
//                    Log.d(TAG, "get contacts from local db  ")
////                    setAdapterLocal(it)
////                    Log.d(TAG, "onCreate: ${it?.get(1)}")
//                }
////                for (item in cntcs){
////                    Log.d(TAG, "onCreate: $cntcs")
////                }
//                Log.d(TAG, "onCreate: ${it?.size}")
//
////                Log.d(TAG, "onCreate: ${it?.get(0)}")
//            }
//        })
    }

    private fun setAdapterLocal(it: List<Contact>) {

        contactsSearchAdapterLocal.setContactList(it)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun observeSearchResults() {
        searchViewmodel.searchResultsLivedata.observe(this, Observer {
            contactsSearchAdapterLocal.setContactList(it)
        })
    }

    override fun onTextChanged(newText: String) {
        binding.tvQueryItem.text = ""

        if(!newText.isNullOrEmpty()){
            queryStr = newText

//            this.searchViewmodel.search(newText!!, key, packageName)

            binding.progressBar.beVisible()

            this.searchViewmodel.searchContactsInDb(newText)

        }

    }

    private fun searchInLocal(newText: String) {


    }

    override fun afterTextChanged(s: Editable) {
        Log.d(TAG, "afterTextChanged: ")
    }






}