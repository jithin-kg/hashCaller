package com.hashcaller.app.view.ui.contacts.individualContacts.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import com.hashcaller.app.local.db.contactInformation.ContactTable
import com.hashcaller.app.local.db.contactInformation.IContactIformationDAO
import com.hashcaller.app.stubs.Contact
import com.hashcaller.app.view.ui.call.db.CallLogTable
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServer
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServerDAO
import com.hashcaller.app.view.ui.call.db.ICallLogDAO
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream


/**
 * Created by Jithin KG on 23,July,2020
 * retrieving data using live data
 */
//class IndividualContactRepository(private val context: IContactIformationDAO)
//    :
//    ContentProviderLiveData<IndividualContact>(context,
//        URI
//    ){
class IndividualContactRepository(
    private val dao: IContactIformationDAO,
    private val context: Context,
    private val callersInfoFromServer: CallersInfoFromServerDAO,
    private val phoneNum: String?,
    private val callLogDAO: ICallLogDAO,
    private val libPhoneCodeHelper: LibPhoneCodeHelper,
    private val countryISO: String
)
   {
       lateinit var cursor:Cursor
//    private val idString:String = id.toString()

    companion object{
//        val URI: Uri = ContactsContract.Contacts.CONTENT_URI
        private const val TAG = "__IndividualContactRepository"
    }

    /**
     * Returns the contact information for the IndividualcontactViewactivity
     * from the local db with additional contact information succh as location, carrier..
     */
    suspend fun getIndividualContactFromDb(phoneNum: String): CallersInfoFromServer? = withContext(Dispatchers.IO) {
        val formatedNum =
            libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(phoneNum), countryISO)
        return@withContext callersInfoFromServer.find(formatedNum)
    }
    suspend fun getPhoto(id: Long, phoneNum: String?): String {
        retrieveContactPhoto(id)
//        retrieveContactPhoto(context, phoneNum)
//        getContactsDetails(phoneNum!!)
//        getContactPhotoFromContentProvider(id)
        return " "
    }


       private fun retrieveContactPhoto(contactID: Long) {
           var photo: Bitmap? = null
           try {
               val inputStream =
                   ContactsContract.Contacts.openContactPhotoInputStream(
                       context.contentResolver,
                       ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactID)
                   )
               if (inputStream != null) {
                   photo = BitmapFactory.decodeStream(inputStream)
//                   val imageView: ImageView = findViewById(R.id.img_contact) as ImageView
//                   imageView.setImageBitmap(photo)
               }

               inputStream?.close()
           } catch (e: IOException) {
               e.printStackTrace()
           }
       }

       @SuppressLint("LongLogTag")
       fun getContactsDetails(address: String): Bitmap? {
           var bp = BitmapFactory.decodeResource(
               context.resources,
               1
           )
           val selection =
               ContactsContract.CommonDataKinds.Phone.NUMBER + " = '" + address + "'"
           val phones = context.contentResolver.query(
               ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, selection,
               null, null
           )
           while (phones!!.moveToNext()) {
               val image_uri = phones.getString(
                   phones.getColumnIndex(
                       ContactsContract.CommonDataKinds.Phone.PHOTO_URI
                   )
               )
               if (image_uri != null) {
                   try {
                       bp = MediaStore.Images.Media
                           .getBitmap(
                               context.contentResolver,
                               Uri.parse(image_uri)
                           )
                   } catch (e: FileNotFoundException) {
                       // TODO Auto-generated catch block
                       Log.d(TAG, "getContactsDetails: exception $e ")
                       e.printStackTrace()
                   } catch (e: IOException) {
                       // TODO Auto-generated catch block
                       Log.d(TAG, "getContactsDetails: exception $e")
                       e.printStackTrace()
                   }
               }
           }
           return bp
       }
       fun retrieveContactPhoto(
           context: Context,
           number: String?
       ): Bitmap? {
           val contentResolver = context.contentResolver
           var contactId: Long? = null
           val uri = Uri.withAppendedPath(
               ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
               Uri.encode(number)
           )
           val projection = arrayOf(
               ContactsContract.PhoneLookup.DISPLAY_NAME,
               ContactsContract.PhoneLookup._ID
           )
           val cursor = contentResolver.query(
               uri,
               projection,
               null,
               null,
               null
           )
           if (cursor != null) {
               while (cursor.moveToNext()) {
                   contactId =
                       cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID))
               }
               cursor.close()
           }
           var photo = BitmapFactory.decodeResource(
               context.resources,
               1
           )
           try {
               if (contactId != null) {
                   val inputStream: InputStream? =
                       ContactsContract.Contacts.openContactPhotoInputStream(
                           context.contentResolver,
                           ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
                       )
                   if (inputStream != null) {
                       photo = BitmapFactory.decodeStream(inputStream)
                   }
                   assert(inputStream != null)
                   inputStream?.close()
               }
           } catch (e: IOException) {
               e.printStackTrace()
           }
           return photo
       }
       @SuppressLint("LongLogTag")
       /**
        * So the thumbnail will be Contacts.Photo.CONTENT_DIRECTORY and the full photo will be Contacts.Photo.DISPLAY_PHOTO
        */
       private fun getContactPhotoFromContentProvider(id: Long): ByteArrayInputStream? {


               val contactUri =
                   ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id)
               val photoUri =
                   Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
               val cursor: Cursor = context.getContentResolver().query(
                   photoUri,
                   arrayOf<String>(ContactsContract.Contacts.Photo.PHOTO),
                   null,
                   null,
                   null
               )
                   ?: return null
               try {
                   if (cursor.moveToFirst()) {
                       val data = cursor.getBlob(0)
                       if (data != null) {
                           val img = ByteArrayInputStream(data)
                           return img
                       }
                   }
               } finally {
                   cursor.close()
               }
               return null


//           Log.d(TAG, "getContactFromContentProvider: $cursor")
//           if(cursor!=null && cursor.moveToFirst()){
//               Log.d(TAG, "getContactFromContentProvider: ")
//           }
       }

       fun getMoreInfoFOrNumber(phoneNum: String?) {
//           this.dao.getInfoForNumber(phoneNum)
       }

       /**
        * function to get contact details for a number
        */
       @SuppressLint("LongLogTag")
       suspend fun getContactDetailForNumberFromCp(phoneNumber: String): Contact?  = withContext(Dispatchers.IO) {
           var cursor:Cursor? = null
           val phoneNum = phoneNumber.replace("+", "").trim()
           val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
           val cursor2 = context.contentResolver.query(uri, null,  null, null, null )

            var  contact:Contact? = null
           try{
               if(cursor2!=null && cursor2.moveToFirst()){
//                    Log.d(TAG, "getConactInfoForNumber: data exist")
                   val name = cursor2.getString(cursor2.getColumnIndexOrThrow("display_name"))
                   val contactId = cursor2.getLong(cursor2.getColumnIndex("contact_id"))
                   val normalizedNumber:String? = cursor2.getString(cursor2.getColumnIndex("normalized_number"))
//                    contact = Contact(id=contactId, name, normalizedNumber, null)
                    contact = Contact(id=contactId,nameInLocalPhoneBook = name, phoneNumber = normalizedNumber?:phoneNumber)
               }


           }catch (e:Exception){
               Log.d(TAG, "getConactInfoForNumber: exception $e")
           }finally {
               cursor2?.close()
           }
           return@withContext contact
       }

       /**
        * function to get non thumbnail image ie clear image from cprovider
        */
       suspend fun getClearImageFromCprovider(phoneNum: String): String? = withContext(Dispatchers.IO) {
           var photoUri:String? = null
           val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNum));
           val cursor = context.contentResolver.query(uri, null,  null, null, null )
           if(cursor!=null && cursor.moveToFirst()){
//                    Log.d(TAG, "getConactInfoForNumber: data exist")
               photoUri = cursor.getString(cursor.getColumnIndexOrThrow( ContactsContract.Contacts.PHOTO_URI))
           }
           return@withContext photoUri
       }



        fun getInfoFromServerForContact(): LiveData<ContactTable?>? {
           return phoneNum?.let { formatPhoneNumber(it) }?.let { dao?.findOne(it) }
       }

       suspend fun getCallLogInfoForNum(phoneNum: String): CallLogTable?  = withContext(Dispatchers.IO){
        val formatedNum = libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(phoneNum), countryISO)
           return@withContext callLogDAO.find(formatedNum)
       }

       suspend fun marAsReportedByUser(contactAddressList: List<String>) {
           for(num in contactAddressList){
               val formatedAdders = libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(num), countryISO)
               callLogDAO?.markAsReportedByUser(formatedAdders, 1)
           }
       }




//   suspend fun getIndividualContactFromContentProvider(context : Context, phoneNum: String):List<com.hashcaller.app.network.user.Contact>{
//       var isLoading: MutableLiveData<Boolean> = MutableLiveData(true)
//
//       val listOfContacts = mutableListOf<com.hashcaller.app.network.user.Contact>()
//       var cursor: Cursor? = null
//       val projection = arrayOf(
//           ContactsContract.Contacts._ID,
//           ContactsContract.Contacts.DISPLAY_NAME
//       )
//       try {
//           cursor = context.contentResolver.query(
//               ContactLiveData.URI,
//               null,
//               null,
//               null,
//               ContactsContract.Contacts.DISPLAY_NAME
//           )
//           if(cursor != null && cursor.moveToFirst()){
//               do{
//
////                val id = cursor.getLong(0)
////                val name = cursor.getString(1)
//                   val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID))
//                   Log.d(TAG, "id is $id ")
//                   val name =
//                       cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
//                   val phoneNo =
//                       cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
//
//                   Log.d(TAG, "phone num is $phoneNo")
//                   Log.d(TAG, "name is  $name")
//                   if(name!=null){
//                       listOfContacts.add(com.hashcaller.app.network.user.Contact(1, name, phoneNo))
//                       val c = com.hashcaller.app.network.user.Contact(1, name)
//
//                   }
//
//
//
//               }while (cursor.moveToNext())
//               cursor.close()
//           }
//       }catch (e:Exception){
//           Log.d(TAG, "getContacts: execption $e")
//       }finally {
//           cursor?.close()
//       }
//       isLoading.postValue(false)
//       return listOfContacts
//
//   }
//    @SuppressLint("LongLogTag")
//    private fun getIndividualContact(context: Context): IndividualContact {
//
//        val contactId = idString
//        val cContactIdString = ContactsContract.Contacts._ID
//        val cCONTACT_CONTENT_URI = ContactsContract.Contacts.CONTENT_URI
//        val cDisplayNameColumn = ContactsContract.Contacts.DISPLAY_NAME
//
//        var nameToDisplay = ""
//        var phoneNumber = ""
//
////        ContactsContract.Contacts.PHOTO_URI
//
//
//        val selection = "$cContactIdString = ? "
//        val selectionArgs =
//            arrayOf<String>(java.lang.String.valueOf(contactId))
//
//        val cursor: Cursor? = context.contentResolver
//            .query(cCONTACT_CONTENT_URI, null, selection, selectionArgs, null)
//        if (cursor != null && cursor.getCount() > 0) {
//            cursor.moveToFirst()
//            while (cursor != null && !cursor.isAfterLast()) {
//                if (cursor.getColumnIndex(cContactIdString) >= 0) {
//                    if (contactId == cursor.getString(cursor.getColumnIndex(cContactIdString))) {
//                        nameToDisplay =
//                            cursor.getString(cursor.getColumnIndex(cDisplayNameColumn))
//                        Log.d("__IndividualContactRepository"," getIndividualContact: $nameToDisplay")
//
//                        break
//                    }
//                }
//                cursor.moveToNext()
//            }
//
//        }
//        cursor?.close()
//         phoneNumber = getPhoneNumber()
//
//
//        return IndividualContact(
//            id,
//            nameToDisplay,
//             phoneNumber
//        )
//    }

//    @SuppressLint("LongLogTag")
//    private fun getPhoneNumber(): String {
//        val contactId = idString
////        val cContactIdString = ContactsContract.Contacts._ID
//        val cContactIdString = CommonDataKinds.Phone.CONTACT_ID
//        var phoneNum = ""
//
//        val selection = "$cContactIdString = ? "
//        val selectionArgs =
//            arrayOf<String>(java.lang.String.valueOf(contactId))
//
//        val cursor: Cursor? = context.contentResolver
//            .query(CommonDataKinds.Phone.CONTENT_URI, null, selection, selectionArgs, null)
//        if (cursor != null && cursor.count > 0) {
//            cursor.moveToFirst()
//            while (cursor != null && !cursor.isAfterLast) {
//                if (cursor.getColumnIndex(cContactIdString) >= 0) {
//                    if (contactId == cursor.getString(cursor.getColumnIndex(cContactIdString))) {
//
//                       phoneNum =  cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER))
//                        Log.d("__IndividualContactRepository", "getPhoneNumber: $phoneNum")
////                        val name: String =
////                            cursor.getString(cursor.getColumnIndex(cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER)))
//////                        Log.d("__IndividualContactRepository"," getIndividualContact: $name")
//
//                        break
//                    }
//                }
//                cursor.moveToNext()
//            }
//
//        }
//        cursor?.close()
//
//        return phoneNum
//    }
//    override fun getContentProviderValue() = getIndividualContact(context)
}