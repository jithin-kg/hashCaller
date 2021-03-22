package com.nibble.hashcaller.view.ui.contacts.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.utils.crypto.KeyManager
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.IndividualCotactViewActivity
import com.nibble.hashcaller.view.ui.contacts.generateCircleView
import com.nibble.hashcaller.view.ui.contacts.isVisible
import com.nibble.hashcaller.view.ui.contacts.search.utils.KeyboardUtils
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchViewModel
import com.nibble.hashcaller.view.ui.sms.individual.util.CONTACT_ID
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.ui.sms.util.ITextChangeListener
import com.nibble.hashcaller.view.ui.sms.util.TextChangeListener
import kotlinx.android.synthetic.main.activity_search_phone.*
import kotlinx.android.synthetic.main.contact_list.textVContactName
import kotlinx.android.synthetic.main.search_result_item.*
import kotlinx.android.synthetic.main.search_result_item.tvSearchResultLocation
import kotlinx.android.synthetic.main.search_result_item1.*
import kotlinx.android.synthetic.main.search_result_item2.*
import java.util.*


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
        initAdapter()

    }

    private fun initAdapter() {
        recyclerView =
            findViewById<View>(R.id.recyclrViewLocal) as RecyclerView

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
        btnLocalMoreSearchResults.setOnClickListener(this)

        imgBtnSearchBack2.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.imgBtnSearchBack ->{
                closeSearchFragment()
            }
            R.id.btnLocalMoreSearchResults->{
                showRecyclerView()
            }
            R.id.imgBtnSearchBack2->{
                onBackButtonPressed()

            }
        }
    }

    private fun onBackButtonPressed() {
        if(isVisible(recyclrViewLocal)){
            hideRecyclerView()
        }else{
            super.onBackPressed()
        }
    }

    private fun showRecyclerView() {
        tvSearchIndicator.beInvisible()
        KeyboardUtils.hideKeyboard(this, searchViewPhoneNum2.windowToken)
        imgViewLock.beInvisible()
        seachViewLayout.beInvisible()
        searchViewPhoneNum2.beInvisible()
        shimmer_view_container.beInvisible()
        edtTextPhoneSearch.beInvisible()
        tvResultFromServer.beInvisible()
        btnLocalMoreSearchResults.beInvisible()
        layoutSearchResult.beInvisible()
        recyclrViewLocal.beVisible()
        tvResultsFor.text = "Search results for "
        tvQueryStr.text = "\"$queryStr\""
        tvResultsFor.beVisible()
        tvQueryStr.beVisible()
        layoutSearchResult1.beInvisible()
        layoutSearchResult2.beInvisible()

        KeyboardUtils.hideKeyboard(this, searchViewPhoneNum2.windowToken)


    }

    private fun hideRecyclerView() {
        layoutSearchResult1.beVisible()
        layoutSearchResult2.beVisible()
        recyclrViewLocal.beInvisible()
        tvResultsFor.beInvisible()
        tvQueryStr.beInvisible()
        imgViewLock.beVisible()
        seachViewLayout.beVisible()
        searchViewPhoneNum2.beVisible()
        shimmer_view_container.beVisible()
        edtTextPhoneSearch.beVisible()
        tvSearchIndicator.beVisible()
        tvContactsResults.beVisible()
        tvResultFromServer.beVisible()

        btnLocalMoreSearchResults.beVisible()
        layoutSearchResult.beVisible()


    }

    private fun closeSearchFragment() {
        KeyboardUtils.hideKeyboard(this, searchViewPhoneNum2.windowToken)
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
//
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

        contactsSearchAdapterLocal.setContactList(it)
        Log.d(TAG, "setAdapterLocal: size is ${it.size}")
       if(it.size>1){
           btnLocalMoreSearchResults.beVisible()
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

       }else if(it.isNotEmpty()){
           tvContactsResults.beVisible()
           btnLocalMoreSearchResults.beInvisible()
           layoutSearchResult1.beVisible()
           textVContactName1.text = it[0].nameSpann
           textVContactNum1.text = it[0].phoneSpann
           tvSearchResultNameFirstLetter1.text  = it[0].name[0].toString()
           tvSearchResultNameFirstLetter1.background = it[0].drawable

           layoutSearchResult2.beInvisible()

       }
    }

    override fun onBackPressed() {
        if(isVisible(recyclrViewLocal)){
            hideRecyclerView()
        }else{
            super.onBackPressed()

        }
    }

    private fun observeSearchResults() {
        this.searchViewmodel.searchResultLiveData.observe(this, Observer {
            if(it!=null){
                layoutSearchResult.beVisible()
                tvResultFromServer.beVisible()
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
            queryStr = newText
            num1 = Random().nextInt(5 - 1) + 1
            num2 = Random().nextInt(5 - 1) + 1

            layoutSearchResult.beInvisible()
            this.searchViewmodel.search(newText!!, key, packageName)
            searchInLocal(newText)



        }else{
            layoutSearchResult1.beInvisible()
            layoutSearchResult2.beInvisible()
            layoutSearchResult.beInvisible()
            edtTextPhoneSearch.setText("")
            tvContactsResults.beInvisible()
            tvResultFromServer.beInvisible()
            btnLocalMoreSearchResults.beInvisible()

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