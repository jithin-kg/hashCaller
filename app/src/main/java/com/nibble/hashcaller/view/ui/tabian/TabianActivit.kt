package com.nibble.hashcaller.view.ui.tabian

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.nibble.hashcaller.R
import kotlinx.android.synthetic.main.activity_tabian.*

/**
 * Created by Jithin KG on 05,July,2020
 */

class TabianActivit : AppCompatActivity() {


    private lateinit var blogAdapter: BlogRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabian)

        initRecyclerView()
        addDataSet()
    }

    private fun addDataSet(){
//        val data = DataSource.createDataSet()
//        blogAdapter.submitList(data)
    }

    private fun initRecyclerView(){

        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@TabianActivit)
            val topSpacingDecorator = TopSpacingItemDecoration(30)
            addItemDecoration(topSpacingDecorator)
            blogAdapter = BlogRecyclerAdapter()
            adapter = blogAdapter
        }
    }


}











