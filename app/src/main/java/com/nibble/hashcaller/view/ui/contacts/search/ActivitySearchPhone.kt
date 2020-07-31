package com.nibble.hashcaller.view.ui.contacts.search

import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import kotlinx.android.synthetic.main.activity_phone_auth.*
import kotlinx.android.synthetic.main.activity_search_phone.*

class ActivitySearchPhone : AppCompatActivity() {
    private lateinit var contactsSearchViewModel: ContactsSearchViewModel
    companion object{
        private const val TAG = "__ActivitySearchPhone"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_phone)

    contactsSearchViewModel = ViewModelProvider(this).get(ContactsSearchViewModel::class.java)

        prepareSearchView()

     searchViewSearchPhone.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
         override fun onQueryTextChange(newText: String?): Boolean {
             // your text view here
             Log.d(TAG, "onQueryTextChange: $newText")
            contactsSearchViewModel.findContactForNum(newText!!)
             return true
         }

         override fun onQueryTextSubmit(query: String?): Boolean {
             Log.d(TAG, "onQueryTextSubmit: $query")
             return true
         }
     })

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