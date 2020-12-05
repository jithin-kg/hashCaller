package com.nibble.hashcaller.view.ui.IncommingCall

import android.app.Activity
import android.app.Dialog
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager


class IncommingDialog(private val activity: Activity?): Dialog(activity!!) {
    private lateinit var dialog:Dialog
    fun showDialog( msg: String?) {
        actionBar?.hide()
        actionBar?.setDisplayShowTitleEnabled(false)
        activity?.title =""
        actionBar?.title = ""

         dialog = Dialog(activity!!)
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        val window: Window = activity.window
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )

        dialog.setContentView(com.nibble.hashcaller.R.layout.dialog)
//        val text = dialog.findViewById(R.id.text_dialog) as TextView
//        text.text = msg
//        val dialogButton: Button = dialog.findViewById(R.id.btn_dialog) as Button
//        dialogButton.setOnClickListener(object : View.OnClickListener() {
//            override fun onClick(v: View?) {
//                dialog.dismiss()
//            }
//        })
        dialog.show()
    }

    override fun setCanceledOnTouchOutside(cancel: Boolean) {
        super.setCanceledOnTouchOutside(cancel)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_OUTSIDE) {
            println("TOuch outside the dialog ******************** ")
            dialog.dismiss()
        }
        return false
    }
}