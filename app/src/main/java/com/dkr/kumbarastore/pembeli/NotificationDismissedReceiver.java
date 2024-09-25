package com.dkr.kumbarastore.pembeli;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.dkr.kumbarastore.LoginActivity;

public class NotificationDismissedReceiver extends BroadcastReceiver {
    private SharedPreferences sharedPreferences;
    private static final String LOGIN_STATUS_KEY = "isLoggedIn";
    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getIntExtra("notificationId", -1);

        // Jika pengguna belum login dan notifikasi dihapus, arahkan mereka ke tampilan login
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            Intent loginIntent = new Intent(context, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(loginIntent);
        } else {
            // Hapus data order jika diperlukan
            if (notificationId != -1) {
                NotificationUtils.clearOrderDetails(context, notificationId);
                NotificationUtils.clearOrderCancel(context, notificationId);
            }
        }
    }
    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean(LOGIN_STATUS_KEY, false);
    }
}


