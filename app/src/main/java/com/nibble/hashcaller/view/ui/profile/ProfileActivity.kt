package com.nibble.hashcaller.view.ui.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityProfileBinding
import com.nibble.hashcaller.repository.user.UserInfoDTO
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoInjectorUtil
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.REQUEST_CODE_IMG_PICK
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.utils.getDecodedBytes
import com.nibble.hashcaller.view.utils.imageProcess.ImagePickerHelper
import com.nibble.hashcaller.view.utils.validateInput
import com.nibble.hashcaller.work.formatPhoneNumber
import okhttp3.MultipartBody

class ProfileActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: UserInfoViewModel
    private var firstName:String? = null
    private var lastName:String? = null
    private lateinit var imagePickerHelper : ImagePickerHelper
    var body: MultipartBody.Part? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewmodel()
        observeUserInfo()
        observeFormfields()
        initListeners()
    }

    private fun initListeners() {
        binding.ivAvatar.setOnClickListener(this)
        binding.btnUpdate.setOnClickListener(this)
    }

    private fun observeFormfields() {
        binding.editTextFName.doOnTextChanged{text, start, before, count ->
            toggleSaveButton(text.toString(), firstName)

        }
        binding.editTextLName.doOnTextChanged { text, start, before, count ->
            toggleSaveButton(text.toString(), lastName)
        }
    }

    private fun toggleSaveButton(text: String, nameInDb: String?) {
        if(!text.isNullOrEmpty()){
            if(text!= nameInDb){
                binding.btnUpdate.beVisible()
            }
            if (binding.editTextFName.text.toString() == firstName && binding.editTextLName.text.toString() == lastName){
                binding.btnUpdate.beInvisible()
            }

        }
    }

    private fun observeUserInfo() {
        viewModel.userInfoLivedata.observe(this, Observer {
            if (it != null) {
                val fLetter = formatPhoneNumber(it.firstname)[0].toString()
                binding.tvFirstLetterMain.text = fLetter
                firstName = "${it.firstname}"
                lastName = "${it.lastName}"

                binding.editTextFName.setText(firstName)
                binding.editTextLName.setText(lastName)

                if(!it.photoURI.isNullOrEmpty()){
                    binding.ivAvatar.setImageBitmap(getDecodedBytes(it.photoURI))
                    binding.tvFirstLetterMain.beInvisible()
                }else{
                    binding.tvFirstLetterMain.beVisible()
                }
            }
        })
    }

    private fun initViewmodel() {
        viewModel = ViewModelProvider(
            this, UserInfoInjectorUtil.provideUserInjectorUtil(
                this
            )
        ).get(
            UserInfoViewModel::class.java
        )
        imagePickerHelper = ImagePickerHelper()

    }
    companion object{
        const val TAG = "__ProfileActivity"
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.ivAvatar ->{
                startImagePickActivity()
            }
            R.id.btnUpdate ->{
                updateUserInfo()
            }
        }
    }

    private fun updateUserInfo() {
        val firstName = binding.editTextFName.text.toString().trim()
        val lastName = binding.editTextLName.text.toString().trim()

        viewModel.compresSAndPrepareForUpload(imagePickerHelper.imgFile,
            this).observe(this,
            Observer {
                body = it

                binding.editTextFName.error = null
//    binding.editTextEmail.error = null
                binding.editTextLName.error = null
                val isValid = validateInput(firstName, lastName, binding.outlinedTextField, binding.outlinedTextField2);
                if(isValid){

                    var userInfo = UserInfoDTO()
                    userInfo.firstName = firstName;
                    userInfo.lastName =  lastName;

                    update(userInfo, body)
                }
            })



    }

    private fun update(userInfo: UserInfoDTO, body: MultipartBody.Part?){

    }
    private fun startImagePickActivity() {
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, REQUEST_CODE_IMG_PICK)
    }
    @SuppressLint("LongLogTag")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                REQUEST_CODE_IMG_PICK -> if (resultCode == RESULT_OK && data != null) {
                    binding.btnUpdate.beVisible()
                    val selectedImageUri: Uri? = data.data
                    binding.ivAvatar.setImageURI(selectedImageUri)
                    viewModel.processImage(this, selectedImageUri, imagePickerHelper)
                }
            }
        }

    }

}