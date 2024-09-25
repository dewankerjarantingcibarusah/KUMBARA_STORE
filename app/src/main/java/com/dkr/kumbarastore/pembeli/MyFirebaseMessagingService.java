package com.dkr.kumbarastore.pembeli;

import android.util.Log;

import com.dkr.kumbarastore.MyApp;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String type = remoteMessage.getData().get("type");

            if (MyApp.isInForeground()) {
                if ("payment".equals(type)) {
                    NotificationUtils.displayPaymentNotification(this, title , body);
                } else if ("signup".equals(type)) {
                    NotificationUtils.displaySignupNotification(this, title, body);
                } else if ("send".equals(type)) {
                    NotificationUtils.displaySendOrderNotification(this, title, body);
                }
            } else {
                if ("payment".equals(type)) {
                    NotificationUtils.displayPaymentNotification(this, title, body);
                } else if ("signup".equals(type)) {
                    NotificationUtils.displaySignupNotification(this, title, body);
                }else if ("send".equals(type)) {
                    NotificationUtils.displaySendOrderNotification(this, title, body);
                }
            }
        }
    }
}
