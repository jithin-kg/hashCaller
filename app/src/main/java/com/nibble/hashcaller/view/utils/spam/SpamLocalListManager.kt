package com.nibble.hashcaller.view.utils.spam

import android.util.Log
import android.view.MenuItem
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import kotlinx.android.synthetic.main.bottom_sheet_block.*

/**
 *
 */
object SpamLocalListManager{
    val SPAMM_TYPE_PUBLIC_SERVICE = 0
    val SPAMM_TYPE_SURVEY = 1
    val SPAMM_TYPE_ROBOCALL = 2
    val SPAMM_TYPE_SALES = 3
    val SPAMM_TYPE_SCAM = 4
    val SPAM_TYPE_FRAUD = 5

    var SPAMMER_BUISINESS = 0
    var SPAMMER_PEERSON= 1
    
    
    fun menuItemClickPerformed(menuItem: MenuItem?, bottomSheetDialog: BottomSheetDialog): Int {
        bottomSheetDialog.radioGroup.clearCheck()
        Log.d(IndividualSMSActivity.TAG, "onMenuItemClick: ")


        when(menuItem?.itemId){
            R.id.popupMarkAllAsRead->{
                setTextInMoreview(menuItem.title, bottomSheetDialog)

                return SpamLocalListManager.SPAMM_TYPE_PUBLIC_SERVICE
            }
            R.id.roboCall->{
                setTextInMoreview(menuItem.title, bottomSheetDialog)

                return   SpamLocalListManager.SPAMM_TYPE_ROBOCALL
            }
            R.id.survey->{
                setTextInMoreview(menuItem.title, bottomSheetDialog)
                return SpamLocalListManager.SPAMM_TYPE_SURVEY
            }
            R.id.fraud->{
                setTextInMoreview(menuItem.title, bottomSheetDialog)

                return SpamLocalListManager.SPAM_TYPE_FRAUD
            }

        }
        return -1
    }

    private fun setTextInMoreview(
        title: CharSequence,
        bottomSheetDialog: BottomSheetDialog
    ) {
        bottomSheetDialog.radioGroup.clearCheck()
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