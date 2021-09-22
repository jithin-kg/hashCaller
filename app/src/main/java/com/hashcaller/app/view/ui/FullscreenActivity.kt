package com.hashcaller.app.view.ui

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.hashcaller.app.datastore.PreferencesKeys
import com.hashcaller.app.utils.notifications.blockPreferencesDataStore
import com.hashcaller.app.view.ui.getstarted.GetStartedActivity
import com.hashcaller.app.view.ui.getstarted.GettingStartedSliderActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {

//    private lateinit var binding: ActivityFullscreenBinding
//    private lateinit var fullscreenContent: TextView
//    private lateinit var fullscreenContentControls: LinearLayout
//    private val hideHandler = Handler()

//    @SuppressLint("InlinedApi")
//    private val hidePart2Runnable = Runnable {
//        Log.d(TAG, ": ")
//        // Delayed removal of status and navigation bar
//
//        // Note that some of these constants are new as of API 16 (Jelly Bean)
//        // and API 19 (KitKat). It is safe to use them, as they are inlined
//        // at compile-time and do nothing on earlier devices.
//        fullscreenContent.systemUiVisibility =
//            View.SYSTEM_UI_FLAG_LOW_PROFILE or
//                    View.SYSTEM_UI_FLAG_FULLSCREEN or
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
//                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
//                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
//                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//    }
//    private val showPart2Runnable = Runnable {
//        // Delayed display of UI elements
//        supportActionBar?.show()
//        fullscreenContentControls.visibility = View.VISIBLE
//    }
//    private var isFullscreen: Boolean = false

//    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
//    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
//        when (motionEvent.action) {
//            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
////                delayedHide(AUTO_HIDE_DELAY_MILLIS)
//            }
//            MotionEvent.ACTION_UP -> view.performClick()
//            else -> {
//            }
//        }
//        false
//    }
    private fun onSingnedOutcleanUp() {

        val i = Intent(this, GettingStartedSliderActivity::class.java)
//        startActivityForResult(i, RC_SIGN_IN)
        startActivity(i)
        finish()
    }
    private fun startMainActivity() {
        val i = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i)
        overridePendingTransition (0, 0)
        finish()
    }
    private fun checkUserInfoInDatastore() {
        lifecycleScope.launchWhenCreated {
            val wrapedKey =  booleanPreferencesKey(PreferencesKeys.USER_INFO_AVIALABLE_IN_DB)
            val tokenFlow: Flow<Boolean> = blockPreferencesDataStore.data.map {
                it[wrapedKey]?:false
            }
            if(tokenFlow.first()){
                //user info available
                    withContext(Dispatchers.Main){
                        startMainActivity()
                    }


            }else {
                //user info not available
                    withContext(Dispatchers.Main){
                        onSingnedOutcleanUp()
                    }

            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        binding = ActivityFullscreenBinding.inflate(layoutInflater)
//        setContentView(binding.root)

//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        isFullscreen = true

        // Set up the user interaction to manually show or hide the system UI.
//        fullscreenContent = binding.fullscreenContent
//        val i = Intent(this, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//        startActivity(i)
//        finish()
        checkUserInfoInDatastore()
//        fullscreenContent.setOnClickListener { toggle() }

//        fullscreenContentControls = binding.fullscreenContentControls

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        binding.dummyButton.setOnTouchListener(delayHideTouchListener)
    }

//    override fun onPostCreate(savedInstanceState: Bundle?) {
//        super.onPostCreate(savedInstanceState)
//
//        // Trigger the initial hide() shortly after the activity has been
//        // created, to briefly hint to the user that UI controls
//        // are available.
////        delayedHide(100)
//    }

//    private fun toggle() {
//        if (isFullscreen) {
//            hide()
//        } else {
//            show()
//        }
//    }

//    private fun hide() {
//        // Hide UI first
//        supportActionBar?.hide()
//        fullscreenContentControls.visibility = View.GONE
//        isFullscreen = false
//
//        // Schedule a runnable to remove the status and navigation bar after a delay
//        hideHandler.removeCallbacks(showPart2Runnable)
//        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
//    }

//    private fun show() {
//        // Show the system bar
//        fullscreenContent.systemUiVisibility =
//            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
//                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//        isFullscreen = true
//
//        // Schedule a runnable to display UI elements after a delay
//        hideHandler.removeCallbacks(hidePart2Runnable)
//        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
//    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
//    private fun delayedHide(delayMillis: Int) {
//        hideHandler.removeCallbacks(hideRunnable)
//        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
//    }

    companion object {
        const val TAG = "__FullscreenActivity"
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}