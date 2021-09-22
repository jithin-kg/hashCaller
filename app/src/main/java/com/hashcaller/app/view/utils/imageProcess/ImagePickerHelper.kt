package com.hashcaller.app.view.utils.imageProcess

import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream


/**
 * class to handle the image picked by user
 */
class ImagePickerHelper {
     var picturePath: String = ""
     var imgFile: File? = null


    /**
     * creates an image file from and image uri
     */
    fun processImage(context: Context, selectedImageUri: Uri? ) {
//        getPath(selectedImageUri, context)
      selectedImageUri?.let {
           imgFile = File(it.path)
      }
//        val bytes =
//            selectedImageUri?.let { context.contentResolver.openInputStream(it)?.readBytes() }
//                ?: return
//        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//
//        val inputStrm: InputStream? = context.contentResolver.openInputStream(selectedImageUri)
//            prepareImageForUpload(getBytes(inputStrm!!))
//            //todo DEPERECATED , change this
//            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
//        if (selectedImageUri != null) {
//            val cursor: Cursor? = context.contentResolver.query(
//                selectedImageUri,
//                filePathColumn, null, null, null
//            )
//            if (cursor != null) {
//                cursor.moveToFirst()
//                val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
//                picturePath = cursor.getString(columnIndex)
//                imgFile = File(picturePath)
//                cursor.close()
//            }
//        }
    }



    @Throws(IOException::class)
   private fun getBytes(`is`: InputStream): ByteArray? {
        val byteBuff = ByteArrayOutputStream()
        val buffSize = 1024
        val buff = ByteArray(buffSize)
        var len = 0
        while (`is`.read(buff).also { len = it } != -1) {
            byteBuff.write(buff, 0, len)
        }
        return byteBuff.toByteArray()
    }
    private fun prepareImageForUpload(imageBytes: ByteArray?) {
        val requestFile: okhttp3.RequestBody? =
            imageBytes?.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, imageBytes.size)
//        body = requestFile?.let { MultipartBody.Part.createFormData("image", "image.jpg", it) }


    }
}