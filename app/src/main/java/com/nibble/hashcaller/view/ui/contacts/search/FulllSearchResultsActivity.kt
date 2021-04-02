package com.nibble.hashcaller.view.ui.contacts.search

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.individualContacts.IndividualCotactViewActivity
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchInjectorUtil
import com.nibble.hashcaller.view.ui.sms.individual.util.CONTACT_ID

class FulllSearchResultsActivity : AppCompatActivity() {
    private lateinit var recyclerView:RecyclerView
    private lateinit var adapter:SearchAdapter
    private lateinit  var searchViewmodel: FullSearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fulll_search_results)
        initViewmodel()
        initRecyclerview()
//        var searchQuery =


    }



    private fun initViewmodel() {
        this.searchViewmodel = ViewModelProvider(
            this, SearchInjectorUtil.provideUserInjectorUtil(
                this
            )
        ).get(FullSearchViewModel::class.java)
    }

    private fun initRecyclerview() {
        recyclerView =
            findViewById<View>(R.id.rcrViewFullSearchResults) as RecyclerView
         adapter = SearchAdapter(applicationContext){id:Long -> onContactitemClicked(id) }
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

    }

    @SuppressLint("LongLogTag")
    private fun onContactitemClicked(id: Long) {
        Log.d(TAG, "onContactItemClicked: $id")
        val intent = Intent(this, IndividualCotactViewActivity::class.java )
        intent.putExtra(CONTACT_ID, id)
        startActivity(intent)

    }

    companion object{
        const val TAG = "__FulllSearchResultsActivity"
    }
}