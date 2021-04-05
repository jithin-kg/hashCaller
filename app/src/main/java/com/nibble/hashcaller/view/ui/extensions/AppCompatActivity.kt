package com.nibble.hashcaller.view.ui.extensions

import android.content.ContentUris
import android.content.Intent
import android.provider.ContactsContract
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu

fun AppCompatActivity.getMyPopupMenu(menu: Int, anchorView: View): PopupMenu {

    val popup = PopupMenu(this, anchorView )
    popup.inflate(menu)
//    popup.setOnMenuItemClickListener(this)
//    popup.show()
    return popup
}

 fun AppCompatActivity.startContactEditActivity(contactId: Long) {
    val i = Intent(Intent.ACTION_EDIT)
    val contactUri =
        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
    i.data = contactUri
    i.putExtra("finishActivityOnSaveCompleted", true)
    startActivity(i)
}