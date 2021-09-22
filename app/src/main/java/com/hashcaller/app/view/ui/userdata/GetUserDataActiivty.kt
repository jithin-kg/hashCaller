package com.hashcaller.app.view.ui.userdata

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hashcaller.app.databinding.ActivityGetUserDataActiivtyBinding
import com.hashcaller.app.network.user.IuserService
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.utils.internet.InternetChecker
import com.hashcaller.app.view.ui.MyWebViewClient
import com.hashcaller.app.view.ui.auth.getinitialInfos.UserInfoInjectorUtil
import com.hashcaller.app.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.hashcaller.app.view.ui.settings.SettingsActivity
import com.hashcaller.app.view.utils.imageProcess.ImagePickerHelper
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class GetUserDataActiivty : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityGetUserDataActiivtyBinding
    private lateinit var viewModel: UserInfoViewModel
    private var tokenHelper: TokenHelper? = null
    private var user: FirebaseUser? = null
    private lateinit var imagePickerHelper : ImagePickerHelper
    private lateinit var internetChecker:InternetChecker
    private val fileName = "myData.txt"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetUserDataActiivtyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        user = FirebaseAuth.getInstance().currentUser
        tokenHelper =  TokenHelper(user)
//        binding.btnRequestData.setOnClickListener(this)
        initViewmodel()
//        binding.webView.loadUrl("http://www.tutorialspoint.com")
//        binding.webView.webChromeClient = MyBrowser() as WebChromeClient
        val map: MutableMap<String, String> = HashMap()
       lifecycleScope.launchWhenStarted {
           val token = TokenHelper(FirebaseAuth.getInstance().currentUser).getToken()
           map["Authorization"] = token!!
           binding.webView.loadUrl("${IuserService.BASE_URL}user/getMyData",  map)
//           binding.webView.loadUrl("https://www.javatpoint.com/android-webview-example")
////           binding.webView.getSettings().setJavaScriptEnabled(true)
           binding.webView.webViewClient = MyWebViewClient(this@GetUserDataActiivty)
       }
       //
//           //
//
//
//
//
//       }

//        getUserData()
    }

    private fun getUserData() {
        lifecycleScope.launchWhenStarted {
            var fis: FileInputStream? = null
            try{
                fis = openFileInput(fileName)
                val isr = InputStreamReader(fis)
                val br = BufferedReader(isr)
                val sb = StringBuilder()
                var text:String? = ""
                var hasNextLine = true
                while (hasNextLine){
                    text =  br.readLine()
                    if(text!=null){

                        sb.append(text).append("\n")
                    }else {

                        hasNextLine = false
                    }
                }
//                binding.tvUserData.text = sb
//        Log.d(TAG, "saveFileToExternalStorage: $sb")
            }catch (e: Exception){
                Log.d(SettingsActivity.TAG, "readFile: $e")
            }
            finally {
                fis?.close()

            }
        }

    }

    private fun initViewmodel() {
        viewModel = ViewModelProvider(
            this, UserInfoInjectorUtil.provideUserInjectorUtil(
                applicationContext,
                tokenHelper
            )
        ).get(
            UserInfoViewModel::class.java
        )
        imagePickerHelper = ImagePickerHelper()
        internetChecker = InternetChecker(this)
    }



    override fun onClick(v: View?) {
        when(v?.id){
//            R.id.btnRequestData -> {
//                if(isEmailValid()){
//                    viewModel.requestForUserInfoStoredInServer( binding.edtTextEmail.text.toString())
//                    toast("A confirmation email is sent to your email")
////                    createPdf("sample pdf")
//                }else {
//                    toast("Please enter a valid email")
//                }
//            }
        }
    }
}