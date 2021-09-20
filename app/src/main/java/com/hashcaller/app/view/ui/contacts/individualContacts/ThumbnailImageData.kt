package com.hashcaller.app.view.ui.contacts.individualContacts

data  class ThumbnailImageData(var imageFoundFrom:Int =IMAGE_NOT_FOUND,
                               var imageStr:String="",
                               var avatarGoogle:String = ""
                               ) {

    companion object{
        const val IMAGE_NOT_FOUND = 0
        const val IMAGE_FOUND_FROM_DB = 1
        const val IMAGE_FOUND_FROM_DB_GOOGLE = 3
        const val IMAGE_FOUND_FROM_C_PROVIDER = 2
    }
}