package com.nibble.hashcaller.view.ui.call.floating

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.*
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.sms.individual.util.beGone
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Window(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val rootView = layoutInflater.inflate(R.layout.window, null)

    private val windowParams = WindowManager.LayoutParams(
        0,
        0,
        0,
        0,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        },
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
        PixelFormat.TRANSLUCENT
    )


    private fun getCurrentDisplayMetrics(): DisplayMetrics {
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        return dm
    }


    private fun calculateSizeAndPosition(
        params: WindowManager.LayoutParams,
        widthInDp: Int,
        heightInDp: Int
    ) {
        val dm = getCurrentDisplayMetrics()
        // We have to set gravity for which the calculated position is relative.
        //do not remove commented code for params.width and height
        params.gravity = Gravity.TOP or Gravity.LEFT
//        params.width = (widthInDp * dm.density).toInt()
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
//        params.height = (heightInDp * dm.density).toInt()
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
//        params.x = (dm.widthPixels - params.width) / 2
        params.x = 0
//        params.horizontalMargin = 8f

        params.y = (dm.heightPixels - params.height) / 2
    }


    private fun initWindowParams() {
        calculateSizeAndPosition(windowParams, 300, 80)
    }


    private fun initWindow() {
        // Using kotlin extension for views caused error, so good old findViewById is used
        rootView.findViewById<View>(R.id.imgBtnCloseIncommin).setOnClickListener { close() }
        rootView.findViewById<View>(R.id.layoutWindowParent).setOnClickListener {
            Toast.makeText(context, "Adding notes to be implemented.", Toast.LENGTH_SHORT).show()
        }
//       rootView.findViewById<View>(R.id.layoutWindowParent).registerDraggableTouchListener()
        rootView.findViewById<View>(R.id.layoutWindowParent).registerDraggableTouchListener(
            initialPosition = { Point(windowParams.x, windowParams.y) },
            positionListener = { x, y -> setPosition(x, y) }
        )

    }
    private fun setPosition(x: Int, y: Int) {
//        windowParams.x = x
        windowParams.y = y
        update()
        if(rootView.findViewById<View>(R.id.layoutDragIndicator).visibility == View.VISIBLE){
            rootView.findViewById<View>(R.id.layoutDragIndicator).beGone()
        }

    }
    private fun update() {
        try {
            windowManager.updateViewLayout(rootView, windowParams)
        } catch (e: Exception) {
            // Ignore exception for now, but in production, you should have some
            // warning for the user here.
        }
    }
    init {
        initWindowParams()
        initWindow()
    }


    fun open() {
        try {
            windowManager.addView(rootView, windowParams)
        } catch (e: Exception) {
            // Ignore exception for now, but in production, you should have some
            // warning for the user here.
        }
    }


    fun close() {
        try {
            windowManager.removeView(rootView)
        } catch (e: Exception) {
            // Ignore exception for now, but in production, you should have some
            // warning for the user here.
        }
    }

}