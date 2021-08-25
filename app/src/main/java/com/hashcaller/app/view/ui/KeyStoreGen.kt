package com.hashcaller.app.view.ui

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


/**
 * Created by Jithin KG on 30,July,2020
 */
class KeyStoreGen {
    private val SAMPLE_ALIAS = "HASHCALLERALIAS"
    val keyGenerator: KeyGenerator = KeyGenerator
        .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
    val keyGenParameterSpec = KeyGenParameterSpec.Builder(
        SAMPLE_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .build()

    // encrypting the data
    fun generateSecretKey(): SecretKey {
        keyGenerator.init(keyGenParameterSpec);
        var secretKey: SecretKey  = keyGenerator.generateKey();
        return secretKey

    }
fun encryptTheKey(secretKey:SecretKey){
    val  cipher : Cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
}
}