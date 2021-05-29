package com.nibble.hashcaller.view.ui.contacts.utils

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.lifecycle.LifecycleCoroutineScope
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.contacts.ContactsQueryHelper
import com.nibble.hashcaller.view.ui.contacts.getAllContactsCursor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.Set

/**
 * Created by Jithin KG on 22,July,2020
 * To get the list of contacts live data from content provider
 *
 */
class ContactLiveData(
    private val context: Context,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val contactQueryHelper: ContactsQueryHelper
):
    ContentProviderLiveData<List<Contact>>(
        context,
        URI,
        lifecycleScope
    ) {
    private var lastNumber = ""
    private var prevName = ""
    companion object{
//        val URI: Uri = ContactsContract.Contacts.CONTENT_URI
        val URI: Uri =ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        private const val TAG = "__ContactLiveData"
    }

    private suspend fun  getContacts(context: Context):List<Contact> = withContext(Dispatchers.IO){

        return@withContext contactQueryHelper.getAllContacts()

    }

    private fun sortAndSet(listOfMessages: MutableList<Contact>): ArrayList<Contact> {
        val s: Set<Contact> = LinkedHashSet(listOfMessages)
        val data = ArrayList(s)

        return data
    }
    // so if there is any change in data this function will query and get latest data
    override suspend fun getContentProviderValue(text: String?): List<Contact> = getContacts(context)
}
