package com.example.juha.foodrecipeapp;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class Utilities {

    public static boolean isConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

}
