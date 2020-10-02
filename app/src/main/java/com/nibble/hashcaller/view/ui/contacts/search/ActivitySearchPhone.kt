package com.nibble.hashcaller.view.ui.contacts.search

import android.content.Intent
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
import com.nibble.hashcaller.network.search.model.SerachRes
import com.nibble.hashcaller.network.user.Status
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.IndividualCotactViewActivity
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ID
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_search_phone.*


class ActivitySearchPhone : AppCompatActivity() {
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var contactsSearchAdapter: SearchAdapter
    private lateinit var owner:ActivitySearchPhone
   
    companion object{
        private const val TAG = "__ActivitySearchPhone"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_phone)
        owner = this


        searchViewModel = ViewModelProvider(this, SearchInjectorUtil.provideUserInjectorUtil(this)).get(
            SearchViewModel::class.java)


        val recyclerView =
            findViewById<View>(R.id.recyclerViewSearchResults) as RecyclerView
        val adapter = SearchAdapter(applicationContext){id:Long -> onContactitemClicked(id) }

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        prepareSearchView()
        initAdapter()

//

        searchViewSearchPhone.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
         override fun onQueryTextChange(newText: String?): Boolean {
             // your text view here
             Log.d(TAG, "onQueryTextChange: $newText")

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




             return true
         }

         override fun onQueryTextSubmit(query: String?): Boolean {
             Log.d(TAG, "onQueryTextSubmit: $query")
             return true
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


}