package com.example.swipeassignment.utils

//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.net.ConnectivityManager
//import android.net.Network
//import android.net.NetworkCapabilities
//import android.os.Build
//import com.example.swipeassignment.ui.productlist.UploadProductFragment
//
//class NetworkReceiver(private val fragment: UploadProductFragment) : BroadcastReceiver() {
//
//    override fun onReceive(context: Context?, intent: Intent?) {
//        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            val networkCallback = object : ConnectivityManager.NetworkCallback() {
//                override fun onAvailable(network: Network) {
//                    super.onAvailable(network)
//                    // Trigger upload if the network is available
//                    fragment.uploadSavedProducts()
//                }
//            }
//            connectivityManager.registerDefaultNetworkCallback(networkCallback)
//        } else {
//            // Fallback for older Android versions
//            val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
//            if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
//                // Trigger upload if the network is available
//                fragment.uploadSavedProducts()
//            }
//        }
//    }
//}
