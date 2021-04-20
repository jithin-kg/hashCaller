package com.nibble.hashcaller.view.ui.contacts.individualContacts

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.contacts.utils.ContentProviderLiveData

class IndividualContactLiveData(
    private val context: Context,
    private val lifecycleScope: LifecycleCoroutineScope
):
ContentProviderLiveData<Contact>(context, URI, lifecycleScope) {

    companion object{
        //        val URI: Uri = ContactsContract.Contacts.CONTENT_URI
        val URI: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        private const val TAG = "__IndividualContactLiveData"
        var phoneNumber:String? = ""
    }

@SuppressLint("LongLogTag")
private fun getContacts(context: Context):Contact{

        var isLoading: MutableLiveData<Boolean> = MutableLiveData(true)
        var c:Contact = Contact(
            1L,
            "sample name",
            "sample phone num",
            "photoThumnail",
            "photoURI"
        )
//        val listOfContacts = mutableListOf<Contact>()
        var cursor: Cursor? = null
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME
        )
        try {
            var phoneNumField = ContactsContract.CommonDataKinds.Phone.NUMBER
            cursor = context.contentResolver.query(
                URI,
                null,
                "'$phoneNumField' ='$phoneNumber'",
                null,
                ContactsContract.Contacts.DISPLAY_NAME
            )
            if(cursor != null && cursor.moveToFirst()){
                do{

                    //                val id = cursor.getLong(0)
                    //                val name = cursor.getString(1)
                    val id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID))
                    Log.d(TAG, "id is $id ")
                    val name =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val photoURI =  cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))
                    val times_used = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))


                    val phoneNo =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                    Log.d(TAG, "phone num is $phoneNo")
                    Log.d(TAG, "name is  $name")
                    if(name!=null){

                         c = Contact(
                             id,
                             name,
                             photoURI,
                             "photoThumnail",
                             photoURI
                         )

                    }



                }while (cursor.moveToNext())
                cursor.close()
            }
        }catch (e:Exception){
            Log.d(TAG, "getContacts: execption $e")
        }finally {
            cursor?.close()
        }
        isLoading.postValue(false)
        return c




    }
    // so if there is any change in data this function will query and get latest data
    override suspend fun getContentProviderValue(text: String?): Contact = getContacts(context)
}