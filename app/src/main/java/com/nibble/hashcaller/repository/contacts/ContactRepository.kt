import android.content.Context
import android.provider.ContactsContract
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Created by Jithin KG on 21,July,2020
 * This ContactsRepository class is used to upload contacts to server
 *
 */
class ContactRepository(context: Context) {

    private val context: Context? = context
    private var contacts: MutableList<ContactUploadDTO> = ArrayList()
    var uniqueMobilePhones: List<ContactUploadDTO> = ArrayList()
    var lastNumber = "0"

    fun fetchContacts(): MutableList<ContactUploadDTO> {
        val cursor = context!!.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        if (cursor?.count ?: 0 > 0) {
            while (cursor!!.moveToNext()) {
                var contact = ContactUploadDTO()
                val name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                var phoneNo =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                phoneNo = phoneNo.trim { it <= ' ' }.replace(" ", "")
                phoneNo = phoneNo.replace("-", "")
                val photoUri =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                val duplicate =
                    AtomicBoolean(false)

                if (lastNumber != phoneNo) {
                    contact.name = name
                    contact.phoneNumber = phoneNo
                    contacts.add(contact)
                    lastNumber = phoneNo
                }
            }
            cursor.close()
        }
        return contacts
    }


}