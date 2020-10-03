package com.nibble.hashcaller.view.ui.contacts.search

import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchViewModel
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_search_phone.*


class ActivitySearchPhone : AppCompatActivity() {
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var contactsSearchAdapter: SearchAdapter
    private lateinit var contactsSearchAdapterLocal: SearchAdapterLocal
    private lateinit var owner:ActivitySearchPhone
    private lateinit var  recyclerView:RecyclerView
    private lateinit var  recyclerViewLocal:RecyclerView

    companion object{
        private const val TAG = "__ActivitySearchPhone"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_phone)
        owner = this


        searchViewModel = ViewModelProvider(this, SearchInjectorUtil.provideUserInjectorUtil(this)).get(
            SearchViewModel::class.java)


         recyclerView =
            findViewById<View>(R.id.recyclerViewSearchResults) as RecyclerView
        recyclerViewLocal =
            findViewById<View>(R.id.recyclerViewLocalSearch) as RecyclerView

        val adapter = SearchAdapter(applicationContext){id:Long -> onContactitemClicked(id) }

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        recyclerViewLocal.setHasFixedSize(true)
        recyclerViewLocal.layoutManager = LinearLayoutManager(this)
        recyclerViewLocal.adapter = adapter
        prepareSearchView()
        initAdapter()

//
        searchViewModel.mt.observe(owner, Observer { 
            it.let {
                if(it!=null){
                    Log.d(TAG, "onCreate: ${it}")
                    setAdapterLocal(it)
//                    Log.d(TAG, "onCreate: ${it?.get(1)}")
                }
//                for (item in cntcs){
//                    Log.d(TAG, "onCreate: $cntcs")
//                }
                Log.d(TAG, "onCreate: ${it?.size}")
                
//                Log.d(TAG, "onCreate: ${it?.get(0)}")
            }
        })
        searchViewSearchPhone.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
         override fun onQueryTextChange(newText: String?): Boolean {
             // your text view here
             Log.d(TAG, "onQueryTextChange: $newText")
             searchViewModel.getContactsFromDb(newText.toString())
            searchContactInServer(newText);


             return true
         }

         override fun onQueryTextSubmit(query: String?): Boolean {
             Log.d(TAG, "onQueryTextSubmit: $query")
             return true
         }
     })

    }

    private fun searchContactInServer(newText: String?) {
        searchViewModel.search(newText!!).observe(owner, Observer {
            it.let {
                    resource ->
                when(resource.status){
                    Status.SUCCESS->{
                        pgBarSearch.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        Log.d(TAG, "onQueryTextChange is mhan: $it")
                        resource.data?.let {
                                searchResult->
                            setAdapter(searchResult)
                        }
                    }
                    Status.LOADING->{
                        //show loading
                        pgBarSearch.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        Log.d(TAG, "onQueryTextChange: Loading....")
                    }
                    else ->{
                        Log.d(TAG, "onQueryTextChange: Error ${resource}")
                        recyclerView.visibility = View.VISIBLE
                        pgBarSearch.visibility = View.GONE
                        Toast.makeText(owner, it.message, Toast.LENGTH_LONG).show()
                    }
                }
            }

        })
    }

    private fun initAdapter() {

        recyclerViewSearchResults.layoutManager = LinearLayoutManager(this@ActivitySearchPhone)
        val topSpacingDecorator =
            TopSpacingItemDecoration(
                30
            )
        recyclerViewSearchResults.addItemDecoration(topSpacingDecorator)

        contactsSearchAdapter = SearchAdapter(this@ActivitySearchPhone){id:Long -> onContactitemClicked(id) }
        recyclerViewSearchResults.adapter = contactsSearchAdapter

        //local search adapter

        recyclerViewLocalSearch.layoutManager = LinearLayoutManager(this@ActivitySearchPhone)
        val topSpacingDecorator2 =
            TopSpacingItemDecoration(
                30
            )
        recyclerViewLocalSearch.addItemDecoration(topSpacingDecorator2)

        contactsSearchAdapterLocal = SearchAdapterLocal(this@ActivitySearchPhone){ id:Long -> onContactitemClicked(id) }
        recyclerViewLocalSearch.adapter = contactsSearchAdapterLocal
    }

    private fun onContactitemClicked(id: Long) {
        Log.d(TAG, "onContactItemClicked: $id")
//        val intent = Intent(this@ActivitySearchPhone, IndividualCotactViewActivity::class.java )
//        intent.putExtra(CONTACT_ID, id)
//        startActivity(intent)

    }

    /**
     * Set searchview focused by default
     */
    private fun prepareSearchView() {
        searchViewSearchPhone.isIconifiedByDefault = true;
        searchViewSearchPhone.isFocusable = true;
        searchViewSearchPhone.isIconified = false;
        searchViewSearchPhone.requestFocusFromTouch();
    }
    private fun setAdapter(searchResult: SerachRes) {
        contactsSearchAdapter.apply {
            setContactList(searchResult)
        }
    }

    private fun setAdapterLocal(it: List<ContactTable>) {
        contactsSearchAdapterLocal.apply {
            setContactList(it)
        }
    }



}