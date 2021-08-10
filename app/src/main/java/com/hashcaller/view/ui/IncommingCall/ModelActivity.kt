package com.hashcaller.view.ui.IncommingCall

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hashcaller.R.layout
import com.hashcaller.databinding.ActivityModelBinding


class ModelActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {
    var DayOfWeek = arrayOf(
        "Sunday", "Monday", "Tuesday",
        "Wednesday", "Thursday", "Friday", "Saturday"
    )
    var dX = 0f
    var dY = 0f
    var lastAction = 0
    private lateinit var popupView:View
    private lateinit var binding:ActivityModelBinding
    private lateinit var popupWindow: PopupWindow
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModelBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.button3.setOnClickListener(this)



    }

    override fun onClick(v: View?) {
        val layoutInflater = baseContext
            .getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupView = layoutInflater.inflate(layout.popup_layout, null)
        //Specify the length and width through constants
        //Specify the length and width through constants
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        //Make Inactive Items Outside Of PopupWindow
        val focusable = true
        //Create a window with our parameters
         popupWindow = PopupWindow(popupView, width, height, focusable)
        //Set the location of the window on the screen
        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

        popupView.setOnTouchListener(this)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event!!.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dX = popupView.x - event!!.rawX
                dY = popupView.y - event!!.rawY
                lastAction = MotionEvent.ACTION_DOWN
            }
            MotionEvent.ACTION_MOVE -> {
//                popupView.y = event!!.rawY + dY
//                popupView.setX(event!!.rawX + dX)
                val offstx = event!!.rawX + dX
                val offsty = event!!.rawY + dY
                lastAction = MotionEvent.ACTION_MOVE
                popupWindow.update(offstx.toInt(), offsty.toInt(),-1, -1, true )
            }
            MotionEvent.ACTION_UP -> if (lastAction === android.view.MotionEvent.ACTION_DOWN) Toast.makeText(
                this,
                "Clicked!",
                Toast.LENGTH_SHORT
            ).show()
            else -> return false
        }
        return true

    }
}