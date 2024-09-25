package com.dkr.kumbarastore;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkUtils {

    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void checkNetworkQuality(final NetworkCallback callback) {
        executorService.submit(() -> {
            boolean isPingSuccessful = isPingSuccessful("https://www.google.com");
            Log.d("NetworkUtils", "Ping successful: " + isPingSuccessful); // Log result
            if (callback != null) {
                callback.onResult(isPingSuccessful);
            }
        });
    }


    public static boolean isPingSuccessful(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(1000); // 1 second
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();
            urlConnection.disconnect();

            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {

            return false;
        }
    }

    public interface NetworkCallback {
        void onResult(boolean isPingSuccessful);
    }
}