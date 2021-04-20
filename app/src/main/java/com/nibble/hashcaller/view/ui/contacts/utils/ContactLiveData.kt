package com.nibble.hashcaller.view.ui.contacts.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import com.nibble.hashcaller.stubs.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.Set
import kotlin.collections.mutableListOf

/**
 * Created by Jithin KG on 22,July,2020
 * To get the list of contacts live data from content provider
 *
 */
class ContactLiveData(private val context: Context, private val lifecycleScope: LifecycleCoroutineScope):
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

        var isLoading: MutableLiveData<Boolean> = MutableLiveData(true)

        val listOfContacts = mutableListOf<Contact>()
        var cursor:Cursor? = null
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME
        )
        try {
            val projection = arrayOf(
                ContactsContract.Contacts.NAME_RAW_CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                ContactsContract.Contacts.PHOTO_URI

            )
             cursor = context.contentResolver.query(
                URI,
                projection,
                null,
                null,
                 ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " COLLATE NOCASE ASC"
            )
            if(cursor != null && cursor.moveToFirst()){
                do{

    //                val id = cursor.getLong(0)
    //                val name = cursor.getString(1)
//                    val id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID))
//                    Log.d(TAG, "id is $id ")
//                    val name =
//                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
//                     val photoURI =  cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))
//                    var photoThumnail = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
//
//                    ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
//                    val phoneNo =
//                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

//                    Log.d(TAG, "phone num is $phoneNo")
//                    Log.d(TAG, "name is  $name")
                    var id = cursor.getString(0).toLong()
                    var name = cursor.getString(1)
                    var phoneNo = cursor.getString(2)

                    val photoThumnail = cursor.getString(3)

                    var photoURI = if(cursor.getString(4) == null) "" else cursor.getString(4)
                    if(name!=null){
                        if(prevName != name && lastNumber != phoneNo){
                            listOfContacts.add(Contact(
                                id,
                                name,
                                phoneNo,
                                photoThumnail,
                                photoURI

                            ))
                            lastNumber = phoneNo
                            prevName = name
                        }



                    }



                }while (cursor.moveToNext())
                cursor.close()
            }
        }catch (e:Exception){
            Log.e(TAG, "getContacts: execption $e")
        }finally {
            cursor?.close()
        }
        isLoading.postValue(false)
       val sortedList = sortAndSet(listOfContacts)
        return@withContext listOfContacts

    }

    private fun sortAndSet(listOfMessages: MutableList<Contact>): ArrayList<Contact> {
        val s: Set<Contact> = LinkedHashSet(listOfMessages)
        val data = ArrayList(s)

        return data
    }
    // so if there is any change in data this function will query and get latest data
    override suspend fun getContentProviderValue(text: String?): List<Contact> = getContacts(context)
}
