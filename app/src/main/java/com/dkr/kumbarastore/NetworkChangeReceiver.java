package com.dkr.kumbarastore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnected()) {
            // Terhubung ke jaringan, cek apakah data tersedia
            new CheckInternetTask(context).execute();
        } else {
            // Tidak terhubung ke jaringan
            redirectToNoInternetActivity(context);
        }
    }

    private void redirectToNoInternetActivity(Context context) {
        Intent jaringanIntent = new Intent(context, JaringanActivity.class);
        jaringanIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Membuka aktivitas baru
        context.startActivity(jaringanIntent);
    }

    private static class CheckInternetTask extends AsyncTask<Void, Void, Boolean> {
        private Context context;

        public CheckInternetTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500); // Set timeout 1.5 detik
                urlc.connect();
                return (urlc.getResponseCode() == 200); // Jika respons OK, berarti internet tersedia
            } catch (IOException e) {
                return false; // Jika ada exception, dianggap tidak ada internet
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                // Jika tidak ada data, pindah ke aktivitas tanpa internet
                Intent jaringanIntent = new Intent(context, JaringanActivity.class);
                jaringanIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Membuka aktivitas baru
                context.startActivity(jaringanIntent);
            } else {
                // Jika data tersedia, tidak melakukan apa-apa atau bisa menampilkan toast

            }
        }
    }
}
