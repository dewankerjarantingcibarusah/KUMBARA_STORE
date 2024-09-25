package com.dkr.kumbarastore.pembeli;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "MyFirebaseBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        RemoteMessage remoteMessage = new RemoteMessage(intent.getExtras());
        Log.d(TAG, "Pesan diterima: " + remoteMessage.getMessageId());
    }
}
