package com.hashcaller.app.view.ui.call.dialer.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

/**
 * This class is used to handle Inconsistency  in recyclerview
 * https://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in
 */

class CustomLinearLayoutManager(context: Context) : LinearLayoutManager(context) {
    @SuppressLint("LongLogTag")
    override fun onLayoutChildren(recycler: Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Log.d(TAG, "Inconsistency detected")
        }
    }

    companion object{
        const val TAG = "__CustomLinearLayoutManager"
    }
}
