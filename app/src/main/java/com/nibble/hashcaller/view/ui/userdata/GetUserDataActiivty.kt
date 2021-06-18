package com.nibble.hashcaller.view.ui.userdata

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityGetUserDataActiivtyBinding
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.internet.InternetChecker
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoInjectorUtil
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.nibble.hashcaller.view.utils.imageProcess.ImagePickerHelper
import java.io.File

class GetUserDataActiivty : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityGetUserDataActiivtyBinding
    private lateinit var viewModel: UserInfoViewModel
    private var tokenHelper: TokenHelper? = null
    private var user: FirebaseUser? = null
    private lateinit var imagePickerHelper : ImagePickerHelper
    private lateinit var internetChecker:InternetChecker


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetUserDataActiivtyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        user = FirebaseAuth.getInstance().currentUser
        tokenHelper =  TokenHelper(user)
        binding.btnRequestData.setOnClickListener(this)
        initViewmodel()
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
            R.id.btnRequestData -> {
                if(isEmailValid()){
                    viewModel.requestForUserInfoStoredInServer( binding.edtTextEmail.text.toString())
//                    createPdf("sample pdf")
                }
            }
        }
    }
//    cena@09876Cena
    //    cena@09876Cena digital ocean password
//cena09876 realcallertest@outlook.com
    fun createPdf(textToPdf: String) {

        // create a new document
        val document = PdfDocument()

        // crate a page description
        var pageInfo: PdfDocument.PageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()

        // start a page
        var page: PdfDocument.Page = document.startPage(pageInfo)
        var canvas = page.canvas
        var paint = Paint()
        paint.color = Color.RED
        canvas.drawCircle(50F, 50F, 30F, paint)
        paint.color = Color.BLACK
        canvas.drawText(textToPdf, 80F, 50F, paint)
        //canvas.drawt
        // finish the page
        document.finishPage(page)
        // draw text on the graphics object of the page

        // Create Page 2
        pageInfo = PdfDocument.PageInfo.Builder(300, 600, 2).create()
        page = document.startPage(pageInfo)
        canvas = page.canvas
        paint = Paint()
        paint.color = Color.BLUE
        canvas.drawCircle(100F, 100F, 100F, paint)
        document.finishPage(page)

        // write the document content
        val directory_path = Environment.getExternalStorageDirectory().path + "/mypdf/"
        val file = File(directory_path)
        if (!file.exists()) {
            file.mkdirs()
        }
        val targetPdf = directory_path + "test-2.pdf"
        val filePath = File(targetPdf)
    }

        fun isEmailValid(): Boolean {
        val email = binding.edtTextEmail.text.toString()
        if(email.isNullOrEmpty()){
            return false
        }
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    }
}