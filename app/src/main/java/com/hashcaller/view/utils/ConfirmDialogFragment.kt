package com.hashcaller.view.utils

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableString
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.hashcaller.view.ui.contacts.utils.TYPE_DELETE
import com.hashcaller.view.ui.contacts.utils.TYPE_MUTE

class ConfirmDialogFragment(
    private val confirmationHandler: ConfirmationClickListener,
    private val message: SpannableString,
    private val title:SpannableString,
    private val type: Int
): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)

           if(type == TYPE_DELETE){

               builder.setMessage(message)
                   .setTitle(title)
                   .setPositiveButton("Yes",
                       DialogInterface.OnClickListener { dialog, id ->
                           // FIRE ZE MISSILES!
                           confirmationHandler.onYesConfirmationDelete()
                       })
                   .setNegativeButton("No",
                       DialogInterface.OnClickListener { dialog, id ->
                           // User cancelled the dialog
                       })
           }else if(type == TYPE_MUTE){
               builder.setMessage(message)
                   .setTitle(title)
                   .setPositiveButton("Yes",
                       DialogInterface.OnClickListener { dialog, id ->
                           // FIRE ZE MISSILES!
                           confirmationHandler.onYesConfirmationMute()
                       })
                   .setNegativeButton("No",
                       DialogInterface.OnClickListener { dialog, id ->
                           // User cancelled the dialog
                       })
           }

           else{

               builder.setMessage(message)
               .setNegativeButton("Close",
                   DialogInterface.OnClickListener { dialog, id ->
                       // User cancelled the dialog
                   })
           }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

/**
 * interface to call back on user click events - yes,no
 */
interface ConfirmationClickListener{
    fun onYesConfirmationDelete()
    fun onYesConfirmationMute()
}
