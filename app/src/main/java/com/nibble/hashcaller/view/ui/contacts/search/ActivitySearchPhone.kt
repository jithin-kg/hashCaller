package com.nibble.hashcaller.view.ui.contacts.search

import android.app.Activity
import android.os.Bundle
import android.transition.Transition
import android.transition.TransitionInflater
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.network.search.model.SerachRes
import com.nibble.hashcaller.network.user.Status
import com.nibble.hashcaller.utils.crypto.KeyManager
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchViewModel
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_search_phone.*
import kotlinx.android.synthetic.main.activity_search_phone.edtTextPhoneSearch
import kotlinx.android.synthetic.main.activity_search_phone.layoutSearchResult
import kotlinx.android.synthetic.main.activity_search_phone.shimmer_view_container
import kotlinx.android.synthetic.main.activity_search_phone.tvSearchIndicator
import kotlinx.android.synthetic.main.contact_list.*
import kotlinx.android.synthetic.main.contact_list.textVContactName
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.android.synthetic.main.search_result_item.*



class ActivitySearchPhone : AppCompatActivity(), View.OnClickListener , SearchView.OnQueryTextListener,
    View.OnFocusChangeListener {
//    private lateinit var searchViewModel: SearchViewModel
//    private lateinit var contactsSearchAdapter: SearchAdapter
//    private lateinit var contactsSearchAdapterLocal: SearchAdapterLocal
//    private lateinit var owner:ActivitySearchPhone
//    private lateinit var  recyclerView:RecyclerView
//    private lateinit var  recyclerViewLocal:RecyclerView


    private lateinit  var searchViewmodel: SearchViewModel
    private var key:String? = null



    companion object{
        private const val TAG = "__ActivitySearchPhone"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val enterAnimation = TransitionInflater.from(this).inflateTransition(R.transition.explode)

//        enterAnimation.setDuration(1000)
//        window.enterTransition = enterAnimation
        setContentView(R.layout.activity_search_phone)

        this.searchViewmodel = ViewModelProvider(
            this, SearchInjectorUtil.provideUserInjectorUtil(
                this
            )
        ).get(SearchViewModel::class.java)

        initLayoutItems()
        requestPreventSoftInputMovingLayoutItems()
        showKeyboardOnLoad()
//        setHasOptionsMenu(true) //
        key = KeyManager.getKey(this)

        initListeners()
        observerPhoneHashValue()
        observeSearchResults()

    }

    /**
     * initialises view elements on OncreateView otherwise kotlin synthetic binding causing nullPointer exceptions
     */
    private fun initLayoutItems() {

    }

    private fun showKeyboardOnLoad() {


        searchViewPhoneNum2.isIconifiedByDefault = true;
        searchViewPhoneNum2.isFocusable = true;
        searchViewPhoneNum2.isIconified = false;
        searchViewPhoneNum2.requestFocusFromTouch();
        showSoftInput()
    }



//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        Log.d(SearchFragment.TAG, "onCreateOptionsMenu: ")
//        inflater.inflate(R.menu.search_menu, menu)
//        super.onCreateOptionsMenu(menu)
//    }

    private fun requestPreventSoftInputMovingLayoutItems() {
//        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    private fun observeSearchResults() {
        this.searchViewmodel.searchResultLiveData.observe(this, Observer {
            if(it!=null){
                Log.d(TAG, "observeSearchResults: ")
                layoutSearchResult.visibility = View.VISIBLE
                textVContactName.text = it.name
                tvSearchResultLocation.text = it.location
                tvSearchResultNameFirstLetter.text  = it.name[0].toString()


            }

        })
    }

    private fun observerPhoneHashValue() {
        this.searchViewmodel.hashedPhoneNum.observe(this, Observer {
                no->
            edtTextPhoneSearch.setText(no)
            tvSearchIndicator.text = "Searching for"
            shimmer_view_container.startShimmer()
        } )
    }

    private fun initListeners() {
//        btnSampleTransition.setOnClickListener(this)
        searchViewPhoneNum2.setOnQueryTextFocusChangeListener(this)
        searchViewPhoneNum2.setOnQueryTextListener(this)
        imgBtnSearchBack2.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.imgBtnSearchBack ->{
                closeSearchFragment()
            }
        }
    }

    private fun closeSearchFragment() {
        hideSoftInput()
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

    private fun hideSoftInput() {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        Log.d(TAG, "onFocusChange: $")

    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
//        this.viewSearch.edtTextPhoneSearch.setText(newText)
        tvSearchIndicator.text = ""
        if(!newText.isNullOrEmpty()){
            layoutSearchResult.visibility = View.GONE
            this.searchViewmodel.search(newText!!, key, packageName)
        }else{
            edtTextPhoneSearch.setText("")
            tvSearchIndicator.text = ""
        }



        //public key comes from server, saved in shared preferences
//        phone number is hashed and encoded in and while sending encrypted


        return true
    }




//        owner = this


//        searchViewModel = ViewModelProvider(this, SearchInjectorUtil.provideUserInjectorUtil(this)).get(
//            SearchViewModel::class.java)


//         recyclerView =
//            findViewById<View>(R.id.recyclerViewSearchResults) as RecyclerView
//        recyclerViewLocal =
//            findViewById<View>(R.id.recyclerViewLocalSearch) as RecyclerView

//        val adapter = SearchAdapter(applicationContext){id:Long -> onContactitemClicked(id) }
//
//        recyclerView.setHasFixedSize(true)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        recyclerView.adapter = adapter
//
//        recyclerViewLocal.setHasFixedSize(true)
//        recyclerViewLocal.layoutManager = LinearLayoutManager(this)
//        recyclerViewLocal.adapter = adapter
//        prepareSearchView()
//        initAdapter()

//
//        searchViewModel.mt.observe(owner, Observer {
//            it.let {
//                if(it!=null){
//
//                    Log.d(TAG, "onCreate: ${it}")
//                    Log.d(TAG, "get contacts from local db  ")
//                    setAdapterLocal(it)
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
//        searchViewSearchPhone.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//         override fun onQueryTextChange(newText: String?): Boolean {
//             // your text view here
//             Log.d(TAG, "onQueryTextChange: $newText")
//             searchViewModel.getContactsFromDb(newText.toString())
//            searchContactInServer(newText);
//
//
//             return true
//         }
//
//         override fun onQueryTextSubmit(query: String?): Boolean {
//             Log.d(TAG, "onQueryTextSubmit: $query")
//             return true
//         }
//     })
//
//    }
//
//    private fun searchContactInServer(newText: String?) {
//        searchViewModel.search(newText!!).observe(owner, Observer {
//            it.let {
//                    resource ->
//                when(resource.status){
//                    Status.SUCCESS->{
//                        pgBarSearch.visibility = View.GONE
//                        recyclerView.visibility = View.VISIBLE
//                        Log.d(TAG, "onQueryTextChange is mhan: $it")
//                        resource.data?.let {
//                                searchResult->
//                            setAdapter(searchResult)
//                        }
//                    }
//                    Status.LOADING->{
//                        //show loading
//                        pgBarSearch.visibility = View.VISIBLE
//                        recyclerView.visibility = View.GONE
//                        Log.d(TAG, "onQueryTextChange: Loading....")
//                    }
//                    else ->{
//                        Log.d(TAG, "onQueryTextChange: Error ${resource}")
//                        recyclerView.visibility = View.VISIBLE
//                        pgBarSearch.visibility = View.GONE
//                        Toast.makeText(owner, it.message, Toast.LENGTH_LONG).show()
//                    }
//                }
//            }
//
//        })
//    }
//
//    private fun initAdapter() {
//
//        recyclerViewSearchResults.layoutManager = LinearLayoutManager(this@ActivitySearchPhone)
//        val topSpacingDecorator =
//            TopSpacingItemDecoration(
//                30
//            )
//        recyclerViewSearchResults.addItemDecoration(topSpacingDecorator)
//
//        contactsSearchAdapter = SearchAdapter(this@ActivitySearchPhone){id:Long -> onContactitemClicked(id) }
//        recyclerViewSearchResults.adapter = contactsSearchAdapter
//
//        //local search adapter
//
//        recyclerViewLocalSearch.layoutManager = LinearLayoutManager(this@ActivitySearchPhone)
//        val topSpacingDecorator2 =
//            TopSpacingItemDecoration(
//                30
//            )
//        recyclerViewLocalSearch.addItemDecoration(topSpacingDecorator2)
//
//        contactsSearchAdapterLocal = SearchAdapterLocal(this@ActivitySearchPhone){ id:Long -> onContactitemClicked(id) }
//        recyclerViewLocalSearch.adapter = contactsSearchAdapterLocal
//    }
//
//    private fun onContactitemClicked(id: Long) {
//        Log.d(TAG, "onContactItemClicked: $id")
////        val intent = Intent(this@ActivitySearchPhone, IndividualCotactViewActivity::class.java )
////        intent.putExtra(CONTACT_ID, id)
////        startActivity(intent)
//
//    }
//
//    /**
//     * Set searchview focused by default
//     */
//    private fun prepareSearchView() {
//        searchViewSearchPhone.isIconifiedByDefault = true;
//        searchViewSearchPhone.isFocusable = true;
//        searchViewSearchPhone.isIconified = false;
//        searchViewSearchPhone.requestFocusFromTouch();
//    }
//    private fun setAdapter(searchResult: SerachRes) {
//        contactsSearchAdapter.apply {
//            setContactList(searchResult)
//        }
//    }
//
//    private fun setAdapterLocal(it: List<ContactTable>) {
//        contactsSearchAdapterLocal.apply {
//            setContactList(it)
//        }
//    }



}