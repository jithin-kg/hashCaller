package com.hashcaller.app.view.utils

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableString
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ConfirmDialogFragment2(
    private val message: SpannableString,
    private val title:SpannableString,
    private val callback : (click:Int) -> Unit
): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)

               builder.setMessage(message)
                   .setTitle(title)
                   .setPositiveButton("Yes",
                       DialogInterface.OnClickListener { dialog, id ->
                           // FIRE ZE MISSILES!
                           callback(ON_POSITIVE_ACTION)
                       })
                   .setNegativeButton("No",
                       DialogInterface.OnClickListener { dialog, id ->
                           // User cancelled the dialog
                           callback(ON_NEGATIVE_ACTION)
                       })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
    companion object {
        const val ON_POSITIVE_ACTION = 1
        const val ON_NEGATIVE_ACTION= 0
    }
}

