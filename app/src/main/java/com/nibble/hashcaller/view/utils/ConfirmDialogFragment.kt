package com.nibble.hashcaller.view.utils

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ConfirmDialogFragment(
    private val confirmationHandler: ConfirmationClickListener,
    private val message: String,
    val type: Int
): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
           if(type == 2){
               builder.setMessage(message)
                   .setPositiveButton("Yes",
                       DialogInterface.OnClickListener { dialog, id ->
                           // FIRE ZE MISSILES!
                           confirmationHandler.onYesConfirmation()
                       })
                   .setNegativeButton("No",
                       DialogInterface.OnClickListener { dialog, id ->
                           // User cancelled the dialog
                       })
           }else{
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
    fun onYesConfirmation()
}