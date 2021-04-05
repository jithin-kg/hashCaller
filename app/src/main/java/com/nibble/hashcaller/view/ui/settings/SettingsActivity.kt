package com.nibble.hashcaller.view.ui.settings

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.manageblock.BlockManageActivity
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initListeners()

    }

    private fun initListeners() {
        imgBtnBackMain.setOnClickListener(this)
        layoutManageBlocking.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.layoutManageBlocking ->{
                startBlockManageActivity()
            }
            R.id.imgBtnBackMain ->{
                finish()
            }
        }
    }

    private fun startBlockManageActivity() {
        val intent = Intent(this, BlockManageActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }
}