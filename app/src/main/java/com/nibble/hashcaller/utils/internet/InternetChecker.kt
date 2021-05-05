package com.nibble.hashcaller.utils.internet

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest

class InternetChecker(private val context: Context) {
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    private val validNetworks: MutableSet<Network> = HashSet()



    fun checkNetwork(nCback: (isAvail:Boolean) -> Unit) {
    networkCallback = createNetworkCallback(nCback)
    val networkRequest = NetworkRequest.Builder()
        .addCapability(NET_CAPABILITY_INTERNET)
        .build()
    cm.registerNetworkCallback(networkRequest, networkCallback)


}
    private fun createNetworkCallback(nCback: (isAvail: Boolean) -> Unit) = object : ConnectivityManager.NetworkCallback() {
        /*
         Called when a network is detected. If that network has internet, save it in the Set.
         Source: https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback#onAvailable(android.net.Network)
        */
        override fun onAvailable(network: Network) {
            val networkCapabilities = cm.getNetworkCapabilities(network)
            val hasInternetCapability = networkCapabilities?.hasCapability(NET_CAPABILITY_INTERNET)
            if (hasInternetCapability == true) {
                // check if this network actually has internet
//                CoroutineScope(Dispatchers.IO).launch {
//                    val hasInternet = DoesNetworkHaveInternet.execute(network.socketFactory)
//                    if(is){
//                        withContext(Dispatchers.Main){
                            validNetworks.add(network)
                return nCback(validNetworks.size >0)
//                        }
//                    }
//                }
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