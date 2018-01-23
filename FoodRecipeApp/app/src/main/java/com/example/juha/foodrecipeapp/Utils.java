package com.example.juha.foodrecipeapp;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class Utils {

    public static boolean isConnection(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

}
