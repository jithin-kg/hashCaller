package com.nibble.hashcaller.view.utils.imageProcess

import android.content.Context
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ImageCompressor(private val context: Context) {
    suspend fun getCompressedImagePart(imgFile: File): MultipartBody.Part {
        val compressedImageFile: File = Compressor.compress(
            context,
            imgFile
        ) {
            resolution(48, 48)
//                                    quality(80)
//                                    format(Bitmap.CompressFormat.WEBP)
            size(4000) // 4 kb
        }
        val requestFile: RequestBody =
            compressedImageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val  body: MultipartBody.Part =  MultipartBody.Part.createFormData(
            "image",
            compressedImageFile.name,
            requestFile
        );
        return body
    }
}