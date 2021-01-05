package com.nibble.hashcaller.view.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nibble.hashcaller.R
import kotlinx.android.synthetic.main.activity_phone_auth.*

class ActivityPhoneAuth : AppCompatActivity() {
    var displayedInstruction = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_auth)

        editTextPhone?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                val hashGenerator = HasValueGenerator()
                val hashValue: String? =
                    hashGenerator.generateHash(editTextPhone?.text.toString())
                if (!displayedInstruction) {
                    textViewInstruction?.setText("Take a look at how we save your phone number")
                    displayedInstruction = true
                }
                textViewHashValue?.text = hashValue
            }

            override fun afterTextChanged(s: Editable) {
                Log.d("Activity_phone_auth", "afterTextChanged: ")
            }
        })
    }

    fun passPhoneNumber(view: View?) {
        val phoneNumber =
            editTextPhone?.text.toString().trim { it <= ' ' }
        val i = Intent(this@ActivityPhoneAuth, testauth::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
        i.putExtra("phoneNumber", phoneNumber)
        startActivity(i)
        finish()
    }
}