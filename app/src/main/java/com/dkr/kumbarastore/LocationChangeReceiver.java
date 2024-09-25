package com.dkr.kumbarastore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;

public class LocationChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //  checkAndRedirectToLocationSettings(context);
    }

    public static void checkAndRedirectToLocationSettings(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        boolean isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isLocationEnabled) {
            // Jika lokasi tidak diaktifkan, arahkan ke pengaturan lokasi
            redirectToLocationSettings(context);

            // Set up a delay to recheck if the user enabled location
            new Handler().postDelayed(() -> {
                checkAndRedirectToLocationSettings(context);
            }, 3000); // Menunggu 3 detik sebelum mengecek kembali
        }
    }

    public static void redirectToLocationSettings(Context context) {
        Intent locationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        locationSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(locationSettingsIntent);
    }



    public static void checkLocationStatus(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        boolean isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isLocationEnabled) {
            // Jika lokasi tidak diaktifkan, arahkan ke pengaturan lokasi
            redirectToLocationSettings1(context);
        }
    }

    public static void redirectToLocationSettings1(Context context) {
        Intent locationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        locationSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Membuka aktivitas baru
        context.startActivity(locationSettingsIntent);
    }
}
