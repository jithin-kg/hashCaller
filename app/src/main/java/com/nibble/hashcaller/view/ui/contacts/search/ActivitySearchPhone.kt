package com.nibble.hashcaller.view.ui.contacts.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.IndividualCotactViewActivity
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ID
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_search_phone.*


class ActivitySearchPhone : AppCompatActivity() {
    private lateinit var contactsSearchViewModel: ContactsSearchViewModel
    private lateinit var contactsSearchAdapter: SearchAdapter
    companion object{
        private const val TAG = "__ActivitySearchPhone"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_phone)

//        initAdapter()


//
        contactsSearchViewModel = ViewModelProvider(this).get(ContactsSearchViewModel::class.java)
        val recyclerView =
            findViewById<View>(R.id.recyclerViewSearchResults) as RecyclerView
        val adapter = SearchAdapter(applicationContext){id:Long -> onContactitemClicked(id) }
//        val adapter = MyListAdapter()

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        prepareSearchView()

//      var sampel:  MutableList<ContactUploadDTO> = mutableListOf()
//        sampel.add(ContactUploadDTO("df", "324"))
//        sampel.add(ContactUploadDTO("dfdfs", "3224"))

//        contactsSearchAdapter.setContactList(sampel)
                     contactsSearchViewModel.contacts.observe(
                 this@ActivitySearchPhone, Observer<List<ContactUploadDTO>>{

                         contacts->
                             Log.d(TAG, "onCreate: change")
                             if(contacts!=null){
                                 Log.d(TAG, "onCreate data change: ${contacts?.size}")
                                 adapter.setContactList(contacts) //Array<SearchContactStub>
                                 Log.d(TAG, "onQueryTextChange: ")
                                 Log.d(TAG, "onCreate: data changed")
                             }

                 })

        searchViewSearchPhone.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
         override fun onQueryTextChange(newText: String?): Boolean {
             // your text view here
             Log.d(TAG, "onQueryTextChange: $newText")

//             adapter.setContactList(myListData)

//             adapter.setContactList(sampel.toList())
//             adapter.setContactList(myListData)
             contactsSearchViewModel.findContactForNum(newText!!)
//             contactsSearchViewModel.findContactForNum("fd"!!).observe(
//                 this@ActivitySearchPhone, Observer<List<SearchContactSTub>>{
//                         contacts->
//                     adapter.setContactList(myListData)
//                     Log.d(TAG, "onQueryTextChange: ")
//                 })
//             adapter.setContactList(sampel)



             return true
         }

         override fun onQueryTextSubmit(query: String?): Boolean {
             Log.d(TAG, "onQueryTextSubmit: $query")
             return true
         }
     })

    }

    private fun initAdapter() {
//      recyclerViewSearchResults.apply {
//          layoutManager = LinearLayoutManager(this@ActivitySearchPhone)
//          contactsSearchAdapter = SearchAdapter(this@ActivitySearchPhone){id:Long->onContactitemClicked(id)}
//      }
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
        val intent = Intent(this@ActivitySearchPhone, IndividualCotactViewActivity::class.java )
        intent.putExtra(CONTACT_ID, id)
        startActivity(intent)

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
}