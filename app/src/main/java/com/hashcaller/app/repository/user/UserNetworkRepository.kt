package com.hashcaller.app.repository.user

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.hashcaller.app.BasicResponseItem
import com.hashcaller.app.network.RetrofitClient
import com.hashcaller.app.network.user.*
import com.hashcaller.app.utils.GenericResponse
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.utils.auth.TokenManager
import com.hashcaller.app.view.ui.auth.getinitialInfos.db.UserInfo
import com.hashcaller.app.view.ui.auth.getinitialInfos.db.UserInfoDAO
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServerDAO
import com.hashcaller.app.view.ui.profile.RequestUserInfoDTO
import com.hashcaller.app.view.utils.imageProcess.ImageCompressor
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
    private lateinit var bio:RequestBody
    private lateinit var email:RequestBody
    private lateinit var googleProfileImgUrl:RequestBody
    private var token:String? = ""

    private var retrofitService:IuserService = RetrofitClient.createaService(IuserService::class.java)


    suspend fun updateUserInfoInServer(userInfo: UserInfoDTO, imgMultipartBody: MultipartBody.Part?): Response<GenericResponse<UpdateProfileResult>>? = withContext(Dispatchers.IO) {
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
                bio,
                email,
                imgMultipartBody,
                it
            )
        }
        return@withContext null

    }
    suspend fun updateProfileWithGoogle(profile: ResUpdateProfileWithGoogle): Response<GenericResponse<ResUpdateProfileWithGoogle>>? = withContext(Dispatchers.IO) {
        token = tokenHelper?.getToken()
        if(token != null){
            return@withContext retrofitService.updateProfileWithGoogle(
                token!!,
                profile,

                )
        }else {
            return@withContext null
        }
    }


    suspend fun signupWithGoogle(profile: SignupWithGoogleDto): Response<GenericResponse<SignupWithGoogleDto>>? = withContext(Dispatchers.IO){
        token = tokenHelper?.getToken()
        return@withContext token?.let { retrofitService.signupWithGoogleAuth(it, profile) }

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
        email = createPartFromString(userInfo.email)
        bio = createPartFromString(userInfo.bio)
        hashedNum = createPartFromString(userInfo.hashedNum)
        phoneNumber = createPartFromString(userInfo.phoneNumber)
        countryCode = createPartFromString(userInfo.countryCode)
        countryISO = createPartFromString(userInfo.countryISO)

        googleProfileImgUrl = createPartFromString(userInfo.googleProfileImgUrl)

    }

    fun createPartFromString(str:String): RequestBody {
        return str.toRequestBody(MultipartBody.FORM)
    }
    suspend  fun saveUserInfoInLocalDb(userInfo: UserInfo)  = withContext(Dispatchers.IO){
        userInfoDAO.insert(userInfo)
    }
    suspend  fun saveGoogleUserInfoInLocalDb(userInfo: SignupWithGoogleDto)  = withContext(Dispatchers.IO){
        val user = UserInfo(null)
        user.firstname = userInfo.firstName
        user.lastName = userInfo.lastName
//        user.phoneNumber =
        user.googleProfileImgUrl = userInfo.avatarGoogle?:""
        user.email = userInfo.email

        userInfoDAO.insert(user)
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
    suspend fun updateUserInfoInDb(data: UpdateProfileResult) = withContext(Dispatchers.IO) {
        if(data.image.isNullOrEmpty()){
            userInfoDAO?.updateUserInfo(
                firstName = data.firstName,
                lastName = data.lastName,
                email= data.email,
                bio = data.bio
            )
        }else {
            userInfoDAO?.updateUserInfoWithImage(
                firstName = data.firstName,
                lastName = data.lastName,
                imageUri = data.image?:"",
                email= data.email,
                bio = data.bio
            )
        }

    }

    suspend fun updateProfileWithGoogleInDb(data: ResUpdateProfileWithGoogle)  = withContext(Dispatchers.IO){
        userInfoDAO?.updateUserInfoWithGoogle(
            firstName = data.firstName,
            lastName = data.lastName,
            imageUri = data.avatarGoogle?:"",
            email= data.email,
            bio = data.bio
        )
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