package com.ecotrack.app.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

/**
 * İnternet bağlantısı kontrolü için yardımcı sınıf.
 * API çağrısı yapmadan önce çağrılır; bağlantı yoksa yerel veriye düşülür.
 */
public class AgBaglantisi {

    public static boolean internetVarMi(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network == null) return false;
            NetworkCapabilities caps = cm.getNetworkCapabilities(network);
            return caps != null && (
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                            || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            // Eski cihazlar için (API 24-22)
            return cm.getActiveNetworkInfo() != null
                    && cm.getActiveNetworkInfo().isConnected();
        }
    }
}
