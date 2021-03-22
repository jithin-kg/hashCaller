package com.nibble.hashcaller.view.ui.contacts.search

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.utils.crypto.KeyManager
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.IndividualCotactViewActivity
import com.nibble.hashcaller.view.ui.contacts.generateCircleView
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchViewModel
import com.nibble.hashcaller.view.ui.sms.individual.util.CONTACT_ID
import com.nibble.hashcaller.view.ui.sms.individual.util.beGone
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.ui.sms.util.ITextChangeListener
import com.nibble.hashcaller.view.ui.sms.util.TextChangeListener
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.activity_search_phone.*
import kotlinx.android.synthetic.main.activity_search_phone.edtTextPhoneSearch
import kotlinx.android.synthetic.main.activity_search_phone.layoutSearchResult
import kotlinx.android.synthetic.main.activity_search_phone.shimmer_view_container
import kotlinx.android.synthetic.main.activity_search_phone.tvSearchIndicator
import kotlinx.android.synthetic.main.contact_list.textVContactName
import kotlinx.android.synthetic.main.search_result_item.*
import kotlinx.android.synthetic.main.search_result_item.tvSearchResultLocation
import kotlinx.android.synthetic.main.search_result_item1.*
import kotlinx.android.synthetic.main.search_result_item2.*
import kotlinx.android.synthetic.main.search_result_item3.*


class ActivitySearchPhone : AppCompatActivity(), View.OnClickListener , SearchView.OnQueryTextListener,
    ITextChangeListener,
    View.OnFocusChangeListener {
//    private lateinit var searchViewModel: SearchViewModel
//    private lateinit var contactsSearchAdapter: SearchAdapter
    private lateinit var contactsSearchAdapterLocal: SearchAdapterLocal
//    private lateinit var owner:ActivitySearchPhone
    private lateinit var  recyclerView:RecyclerView
//    private lateinit var  recyclerViewLocal:RecyclerView
    private lateinit var editTextListener: TextChangeListener


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
//        initAdapter()
        observerPhoneHashValue()
        observeSearchResults()
        observeLocalSearchResults()

    }

    private fun initAdapter() {
//        recyclerView =
//            findViewById<View>(R.id.recyclrViewLocal) as RecyclerView

         this.contactsSearchAdapterLocal = SearchAdapterLocal(applicationContext){id:Long -> onContactitemClicked(id) }

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = contactsSearchAdapterLocal

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = contactsSearchAdapterLocal
    }

    private fun onContactitemClicked(id: Long) {
        Log.d(TAG, "onContactItemClicked: $id")
        val intent = Intent(this@ActivitySearchPhone, IndividualCotactViewActivity::class.java )
        intent.putExtra(CONTACT_ID, id)
        startActivity(intent)

    }


    /**
     * initialises view elements on OncreateView otherwise kotlin synthetic binding causing nullPointer exceptions
     */
    private fun initLayoutItems() {

    }

    private fun showKeyboardOnLoad() {


//        searchViewPhoneNum2.isIconifiedByDefault = true;
        searchViewPhoneNum2.isFocusable = true;
//        searchViewPhoneNum2.isIconified = false;
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
//        searchViewPhoneNum2.setOnQueryTextFocusChangeListener(this)
//        searchViewPhoneNum2.setOnQueryTextListener(this)
        editTextListener = TextChangeListener(this)
        editTextListener.addListener(searchViewPhoneNum2)

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
//        tvSearchIndicator.text = ""
//        if(!newText.isNullOrEmpty()){
//            layoutSearchResult.visibility = View.GONE
//            this.searchViewmodel.search(newText!!, key, packageName)
//            this.searchViewmodel.searchContactsInDb(newText.toString()).observe(this, Observer {
//                Log.d(TAG, "onQueryTextChange: result from contact cprovider $it")
//                if(it!=null){
//                    setAdapterLocal(it!!)
//                }
//            })
//
//        }else{
//            layoutSearchResult1.beGone()
//            layoutSearchResult2.beGone()
//            layoutSearchResult3.beGone()
//            layoutSearchResult.beGone()
//            edtTextPhoneSearch.setText("")
//            tvContactsResults.beGone()
//            tvResultFromServer.beGone()
//
//        }
        return true
    }
    private fun observeLocalSearchResults() {
        this.searchViewmodel.mt.observe(this, Observer {
            it.let {
                if(it!=null){

                    Log.d(TAG, "onCreate: ${it}")
                    Log.d(TAG, "get contacts from local db  ")
//                    setAdapterLocal(it)
//                    Log.d(TAG, "onCreate: ${it?.get(1)}")
                }
//                for (item in cntcs){
//                    Log.d(TAG, "onCreate: $cntcs")
//                }
                Log.d(TAG, "onCreate: ${it?.size}")

//                Log.d(TAG, "onCreate: ${it?.get(0)}")
            }
        })
    }

    private fun setAdapterLocal(it: List<Contact>) {

//        contactsSearchAdapterLocal.setContactList(it)
       if(it.size>2){
           tvContactsResults.beVisible()

           layoutSearchResult1.beVisible()
           textVContactName1.text = it[0].nameSpann
           textVContactNum1.text = it[0].phoneSpann
           tvSearchResultNameFirstLetter1.text  = it[0].name[0].toString()
           tvSearchResultNameFirstLetter1.background = it[0].drawable

           layoutSearchResult2.beVisible()
           textVContactName2.text = it[1].nameSpann
           textVContactNum2.text = it[1].phoneSpann
//           tvSearchResultLocatio2.text = "location"
           tvSearchResultNameFirstLetter2.text  = it[1].name[0].toString()
           tvSearchResultNameFirstLetter2.background = it[1].drawable

       }else if(it.size > 1){
           tvContactsResults.beVisible()

           layoutSearchResult1.beVisible()
           textVContactName1.text = it[0].nameSpann
           textVContactNum1.text = it[0].phoneSpann
           tvSearchResultNameFirstLetter1.text  = it[0].name[0].toString()
           tvSearchResultNameFirstLetter1.background = it[0].drawable

           layoutSearchResult2.beVisible()
           textVContactName2.text = it[1].nameSpann
           textVContactNum2.text = it[1].phoneSpann
           tvSearchResultNameFirstLetter2.text  = it[0].name[1].toString()
           tvSearchResultNameFirstLetter2.background = it[1].drawable

       }else if(it.size>0){
           tvContactsResults.beVisible()

           layoutSearchResult1.beVisible()
           textVContactName1.text = it[0].nameSpann
           textVContactNum1.text = it[0].phoneSpann
           tvSearchResultNameFirstLetter1.text  = it[0].name[0].toString()
           tvSearchResultNameFirstLetter1.background = it[0].drawable


       }

    }

    private fun observeSearchResults() {
        this.searchViewmodel.searchResultLiveData.observe(this, Observer {
            if(it!=null){
                layoutSearchResult.visibility = View.VISIBLE
                textVContactName.text = "it.name"
                tvSearchResultLocation.text = "kerala"
                Log.d(TAG, "observeSearchResults: location is ${it.location}")
                tvSearchResultNameFirstLetter.text  = it.name[0].toString()
                tvSearchResultNameFirstLetter.background = generateCircleView()


            }

        })
    }

    override fun onTextChanged(newText: String) {
        tvSearchIndicator.text = ""
        if(!newText.isNullOrEmpty()){
            layoutSearchResult.visibility = View.GONE
            this.searchViewmodel.search(newText!!, key, packageName)
            searchInLocal(newText)

        }else{
            layoutSearchResult1.beGone()
            layoutSearchResult2.beGone()
            layoutSearchResult.beGone()
            edtTextPhoneSearch.setText("")
            tvContactsResults.beGone()
            tvResultFromServer.beGone()

        }
    }

    private fun searchInLocal(newText: String) {
        this.searchViewmodel.searchContactsInDb(newText.toString()).observe(this, Observer {
            Log.d(TAG, "onQueryTextChange: result from contact cprovider $it")
            if(it!=null){
                setAdapterLocal(it!!)
            }
        })
    }

    override fun afterTextChanged(s: Editable) {
        Log.d(TAG, "afterTextChanged: ")
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




}