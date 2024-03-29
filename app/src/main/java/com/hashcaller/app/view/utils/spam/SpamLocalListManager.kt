package com.hashcaller.app.view.utils.spam

import android.util.Log
import android.view.MenuItem
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hashcaller.app.view.ui.sms.individual.IndividualSMSActivity
import kotlinx.android.synthetic.main.bottom_sheet_block.*

/**
 *
 */
object SpamLocalListManager{

    
    
    fun menuItemClickPerformed(menuItem: MenuItem?, bottomSheetDialog: BottomSheetDialog): Int {
        bottomSheetDialog.radioGroupOne.clearCheck()
        Log.d(IndividualSMSActivity.TAG, "onMenuItemClick: ")


        when(menuItem?.itemId){
//            R.id.popupMarkAllAsRead->{
//                setTextInMoreview(menuItem.title, bottomSheetDialog)
//
//                return SPAMM_TYPE_PUBLIC_SERVICE
//            }
//            R.id.roboCall->{
//                setTextInMoreview(menuItem.title, bottomSheetDialog)
//
//                return   SPAMM_TYPE_ROBOCALL
//            }
//            R.id.survey->{
//                setTextInMoreview(menuItem.title, bottomSheetDialog)
//                return SPAMM_TYPE_SURVEY
//            }
//            R.id.fraud->{
//                setTextInMoreview(menuItem.title, bottomSheetDialog)
//
//                return SPAM_TYPE_FRAUD
//            }

        }
        return -1
    }

    private fun setTextInMoreview(
        title: CharSequence,
        bottomSheetDialog: BottomSheetDialog
    ) {
        bottomSheetDialog.radioGroupOne.clearCheck()
        var titeString = title
        if(title.length > 4){
            titeString = title.substring(0, 5)
            titeString = "$titeString..."
        }
//        bottomSheetDialog.tvExpand.text = titeString

//       bottomSheetDialog.imgExpand.setImageResource(R.drawable.ic_baseline_expand_more_white)
//        bottomSheetDialog.layoutExpand.background = ContextCompat.getDrawable(this,R.drawable.expand_background)

//        bottomSheetDialog.tvExpand.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))

    }

   
    
   
}