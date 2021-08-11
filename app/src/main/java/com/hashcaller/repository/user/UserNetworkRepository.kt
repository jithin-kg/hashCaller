package com.hashcaller.repository.user

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.hashcaller.BasicResponseItem
import com.hashcaller.network.RetrofitClient
import com.hashcaller.network.user.GetUserDataResponse
import com.hashcaller.network.user.GetUserInfoDTO
import com.hashcaller.network.user.IuserService
import com.hashcaller.network.user.SingupResponse
import com.hashcaller.utils.auth.TokenHelper
import com.hashcaller.utils.auth.TokenManager
import com.hashcaller.view.ui.auth.getinitialInfos.db.UserInfo
import com.hashcaller.view.ui.auth.getinitialInfos.db.UserInfoDAO
import com.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.hashcaller.view.ui.profile.RequestUserInfoDTO
import com.hashcaller.view.utils.imageProcess.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

/**
 * Created by Jithin KG on 13,August,2020
 */
class UserNetworkRepository(
    private val tokenManager: TokenManager,
    private val userInfoDAO: UserInfoDAO,
    private val senderInfoFromServerDAO: CallersInfoFromServerDAO,
    private val imageCompressor: ImageCompressor,
    private val tokenHelper: TokenHelper?
){
    private lateinit var firstName:RequestBody
    private lateinit var lastName :RequestBody
    private lateinit var hashedNum :RequestBody
    private lateinit var phoneNumber :RequestBody
    private lateinit var countryCode :RequestBody
    private lateinit var countryISO :RequestBody
    private var token:String? = ""

    private var retrofitService:IuserService = RetrofitClient.createaService(IuserService::class.java)

    suspend fun updateUserInfoInServer(userInfo: UserInfoDTO, imgMultipartBody: MultipartBody.Part?): Response<SingupResponse>? = withContext(Dispatchers.IO) {
        prepareRquestBody(userInfo)
        token = tokenHelper?.getToken()
        token?.let {
            return@withContext retrofitService.updateUserInfo(
                firstName,
                lastName,
                hashedNum,
                phoneNumber,
                countryCode,
                countryISO,
                imgMultipartBody,
                it
            )
        }
        return@withContext null


    }
    suspend fun signup(userInfo: UserInfoDTO, imgMultipartBody: MultipartBody.Part?): Response<SingupResponse>?   = withContext(Dispatchers.IO) {
//        retrofitService = RetrofitClient.createaService(IuserService::class.java)
        prepareRquestBody(userInfo)
        //        Log.d(TAG, "signup: ${response?.body()?.message}")
        token = tokenHelper?.getToken()
        token?.let {
            return@withContext retrofitService?.signup(
                firstName,
                lastName,
                hashedNum,
                phoneNumber,
                countryCode,
                countryISO,
                imgMultipartBody,
                it
            )
        }
        return@withContext null

    }

    private suspend fun prepareRquestBody(userInfo: UserInfoDTO) {
        token = tokenHelper?.getToken()
        firstName = createPartFromString(userInfo.firstName)
        lastName = createPartFromString(userInfo.lastName)
        hashedNum = createPartFromString(userInfo.hashedNum)
        phoneNumber = createPartFromString(userInfo.phoneNumber)
        countryCode = createPartFromString(userInfo.countryCode)
        countryISO = createPartFromString(userInfo.countryISO)
    }

    fun createPartFromString(str:String): RequestBody {
        return str.toRequestBody(MultipartBody.FORM)
    }
    suspend  fun saveUserInfoInLocalDb(userInfo: UserInfo)  = withContext(Dispatchers.IO){

        userInfoDAO.insert(userInfo)
    }

    /**
     * function to get user info from local db
     */
     fun getUserInfoLiveData(): LiveData<UserInfo> {
        return this.userInfoDAO.getUserInfoLiveData()
    }
    suspend fun getUserInfo():UserInfo? = withContext(Dispatchers.IO){
        return@withContext userInfoDAO?.getUser()
    }
    suspend fun updateUserInfoInDb(firstName: String, lastName: String, imageUri: String) = withContext(Dispatchers.IO) {
        userInfoDAO?.updateUserInfo(firstName, lastName, imageUri)
    }

    suspend fun setContactsInHashMap() {

    }

    suspend fun getUserInfoFromServer(
        hashedNum: String,
        formattedPhoneNum: String
    ): Response<SingupResponse>? = withContext(Dispatchers.IO) {
        val token = tokenHelper?.getToken()
        return@withContext token?.let { retrofitService.getUserInfo(it, GetUserInfoDTO(hashedNum, formattedPhoneNum)) }
    }

    suspend fun insertNewUserIntoDb(userInfo: UserInfo)  = withContext(Dispatchers.IO) {
        userInfoDAO?.insert(user = userInfo)
    }

    suspend fun getCompressedImageBody(context: Context, imgFile: File): MultipartBody.Part  = withContext(Dispatchers.Default){
        return@withContext  imageCompressor.getCompressedImagePart(imgFile)

    }

    suspend fun requestForUserInfoInserver(email:String)  = withContext(Dispatchers.IO){
        token = tokenHelper?.getToken()
        token?.let { retrofitService.requestUserInfoInServer(it, RequestUserInfoDTO(email)) }
    }

    suspend fun getMyData(): Response<GetUserDataResponse>? {
        token = tokenHelper?.getToken()
        return token?.let { retrofitService.getMyData(it) }
    }

    suspend fun saveFile(res: GetUserDataResponse?, fos: FileOutputStream) = withContext(Dispatchers.IO) {
//        var fos: FileOutputStream? = null
//        var fis: FileInputStream? = null
        try {
//            fos =  openFileOutput(fileName, AppCompatActivity.MODE_PRIVATE)
             res?.data?.let {
                 val fName = it.firstName?:""
                 val lName = it.lastName?:""
                 val avatarPhoto = it.image?:"N/A"
                 val contacts = it.contacts

                 val strToWrite = "firstName: $fName \n " +
                         "lastName: $lName \n" +
                         "avatarPhoto: $avatarPhoto \n" +
                         "contacts: $contacts"
                 fos?.write(strToWrite.toByteArray())
             }

//        toast("file saved to $filesDir / $fileName")
//            Log.d(TAG, "file saved to $filesDir / $fileName")
        }catch (e: Exception){
            Log.d(TAG, "saveFileToExternalStorage: $e")
        }finally {
            fos?.close()
        }

    }

    suspend fun deactivate() :Response<BasicResponseItem<String>>?  = withContext(Dispatchers.IO) {
        token = tokenHelper?.getToken()
        return@withContext token?.let { retrofitService.deactivateMyAccount(it) }
    }


    companion object{
        private const val TAG = "__UserNetworkRepository"
    }
}