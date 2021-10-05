package com.hashcaller.app.view.ui.sms.individual

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.Constraints
import androidx.lifecycle.lifecycleScope
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ActivitySampleBinding
import com.hashcaller.app.view.ui.sms.individual.util.*
import kotlinx.coroutines.delay

class SampleActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivitySampleBinding
    private var isExpanded = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.layoutcallMain1.setOnClickListener (this)
        binding.materialCardView.setOnClickListener(this)


//        setSupportActionBar(findViewById(R.id.toolbar))


    }
    companion object {
        const val TAG = "__SampleActivity"
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.layoutcallMain1 -> {
                onLayoutClieked()
            }
            R.id.materialCardView -> {
                onLayoutClieked()

            }
        }

    }

    private fun onLayoutClieked() {
        Log.d(TAG, "onLayoutClieked: $isExpanded")
        if(isExpanded){

            lifecycleScope.launchWhenCreated {
                isExpanded = false
                delay(200L)
                binding.layoutcallMain1.beVisible()
                binding.materialCardView.beInvisible()
                binding.materialCardView.slideVisibilityToTop(false, 500L)
                delay(501L)
                binding.materialCardView.layoutParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, 0)
                binding.layoutExpandableCall
            }

//            binding.layoutExpandableCall.beInvisible()
//            binding.layoutExpandableCall.layoutParams =
//                LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, 0)
        }else {

            lifecycleScope.launchWhenCreated {
                delay(200L)
                isExpanded = true
                binding.layoutcallMain1.beInvisible()
                binding.materialCardView.beVisible()
                binding.materialCardView.slideVisibilityToBottom(true, 500L)
                delay(501L)
                binding.materialCardView.layoutParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            }
//            binding.layoutExpandableCall.beVisible()
//            binding.layoutExpandableCall.layoutParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//            binding.layoutExpandableCall.slideVisibility(true)
        }
    }
}