package com.hashcaller.app.view.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hashcaller.app.view.ui.sms.individual.util.getRandomColor
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HashCallerViewModel(application: Application): AndroidViewModel(application) {
    var mapOfColorsForString: HashMap<String, Int> = hashMapOf()
    var mapOfColorsForInt : HashMap<Int, Int> = hashMapOf()


    fun initColors() = viewModelScope.launch {

         val defStr =   async {
               var alphabet = 'a'
               while (alphabet <= 'z') {
                   mapOfColorsForString[alphabet.toString()] = getRandomColor()
                   alphabet++
               }
           }

        val defInt = async {
            var num = 0
            while (num<=9){
                mapOfColorsForInt[num] = getRandomColor()
                num++
            }
        }

        try {
            defInt.await()
            defStr.await()
        }catch (e:Exception){
            Log.d(TAG, "initColors: $e")
        }
    }

    companion object {
        const val TAG = "__HashCallerViewModel"
    }


}