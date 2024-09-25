package com.dkr.kumbarastore.pembeli;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.dkr.kumbarastore.R;
import com.google.auth.oauth2.GoogleCredentials;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FCMSender {

    private static final String TAG = "FCMSender";
    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/kumbara-store/messages:send";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static void sendNotificationPayment(Context context, String targetToken, String title, String body) {
        sendNotification(context, targetToken, title, body, "payment");
    }
    public static void sendNotificationSignup(Context context, String targetToken, String title, String teks, String body) {
        sendNotification(context, targetToken, title, body, "signup");
    }
    public static void sendNotificationSendOrder(Context context, String targetToken, String title, String body) {
        sendNotification(context, targetToken, title, body, "send");
    }

    private static void sendNotification(Context context, String targetToken, String title, String body, String type) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    InputStream serviceAccount = context.getResources().openRawResource(R.raw.service_account);

                    GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                            .createScoped("https://www.googleapis.com/auth/cloud-platform");

                    OkHttpClient client = new OkHttpClient();

                    Map<String, Object> message = new HashMap<>();
                    message.put("token", targetToken);

                    Map<String, String> data = new HashMap<>();
                    data.put("title", title);
                    data.put("body", body);
                    data.put("type", type);

                    message.put("data", data);

                    Map<String, Object> requestBodyMap = new HashMap<>();
                    requestBodyMap.put("message", message);

                    JSONObject jsonObject = new JSONObject(requestBodyMap);
                    RequestBody requestBody = RequestBody.create(jsonObject.toString(), JSON);

                    Request request = new Request.Builder()
                            .url(FCM_URL)
                            .post(requestBody)
                            .addHeader("Authorization", "Bearer " + credentials.refreshAccessToken().getTokenValue())
                            .addHeader("Content-Type", "application/json; UTF-8")
                            .build();

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }
}

