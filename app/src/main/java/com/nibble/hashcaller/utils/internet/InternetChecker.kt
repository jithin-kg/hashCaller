package com.nibble.hashcaller.utils.internet

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InternetChecker(private val context: Context) {
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    private val validNetworks: MutableSet<Network> = HashSet()

    fun isnetworkAvailable(): Boolean {
        return validNetworks.size > 0
    }
    init {
        checkNetwork()
    }
    fun checkNetwork(){
    networkCallback = createNetworkCallback()
    val networkRequest = NetworkRequest.Builder()
        .addCapability(NET_CAPABILITY_INTERNET)
        .build()
    cm.registerNetworkCallback(networkRequest, networkCallback)
}
    private fun createNetworkCallback() = object : ConnectivityManager.NetworkCallback() {
        /*
         Called when a network is detected. If that network has internet, save it in the Set.
         Source: https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback#onAvailable(android.net.Network)
        */
        override fun onAvailable(network: Network) {
            val networkCapabilities = cm.getNetworkCapabilities(network)
            val hasInternetCapability = networkCapabilities?.hasCapability(NET_CAPABILITY_INTERNET)
            if (hasInternetCapability == true) {
                // check if this network actually has internet
                CoroutineScope(Dispatchers.IO).launch {
                    val hasInternet = DoesNetworkHaveInternet.execute(network.socketFactory)
                    if(hasInternet){
                        withContext(Dispatchers.Main){
                            validNetworks.add(network)
                        }
                    }
                }
            }
        }

        /*
          If the callback was registered with registerNetworkCallback() it will be called for each network which no longer satisfies the criteria of the callback.
          Source: https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback#onLost(android.net.Network)
         */
        override fun onLost(network: Network) {
            validNetworks.remove(network)
        }

    }

}