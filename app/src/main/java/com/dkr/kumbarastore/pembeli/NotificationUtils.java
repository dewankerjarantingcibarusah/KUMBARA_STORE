package com.dkr.kumbarastore.pembeli;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.dkr.kumbarastore.R;
import com.dkr.kumbarastore.penjual.TampilanpenjualActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class NotificationUtils {

    public static final String PAYMENT_CHANNEL_ID = "payment_channel";
    public static final String SIGNUP_CHANNEL_ID = "signup_channel";
    public static final String SENDORDER_CHANNEL_ID = "sendorder_channel";
    public static final String PAYMENT_LOCAL_CHANNEL_ID = "payment_local_channel";
    public static final String CANCEL_LOCAL_CHANNEL_ID = "cancel_local_channel";
    public static final String ADMIN_GROUP_ID = "admin_group";
    public static final String MEMBER_GROUP_ID = "member_group";

    // Method to display FCM payment notification
    public static void displayPaymentNotification(Context context, String title, String body) {
        createNotificationChannelWithGroup(context, PAYMENT_CHANNEL_ID, "Payment Notifications", "Notifications for payment transactions",ADMIN_GROUP_ID, "Notifications for Admin");

        Intent intent = new Intent(context, TampilanpenjualActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        int notifId = UUID.randomUUID().hashCode();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PAYMENT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_akun)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notifId, builder.build());
    }
    public static void displaySignupNotification(Context context, String title, String body) {
        createNotificationChannelWithGroup(context, SIGNUP_CHANNEL_ID, "Signup Notifications", "Notifications for new signups",ADMIN_GROUP_ID, "Notifications for Admin");

        Intent intent = new Intent(context, TampilanpenjualActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        int notifId = UUID.randomUUID().hashCode();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, SIGNUP_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_akun)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notifId, builder.build());
    }
    public static void displaySendOrderNotification(Context context, String title, String body) {
        createNotificationChannelWithGroup(context, SENDORDER_CHANNEL_ID, "Send Order Notifications", "Notifications for new Send Order",MEMBER_GROUP_ID, "Notifications for Member");

        Intent intent = new Intent(context, RiwayatPesananPembeliActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("selectedTab", 1);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        int notifId = UUID.randomUUID().hashCode();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, SENDORDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_akun)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notifId, builder.build());
    }
    // Method to display local payment notification
    public static void displayPaymentLocal(Context context, String title, String body, String orderNumber, String kta) {
        int notificationId = getNotificationIdForOrder(orderNumber) + 1000; // Tambahkan offset untuk memastikan ID unik

        saveOrderDetails(context, kta, orderNumber, notificationId);

        createNotificationChannelWithGroup(context, PAYMENT_LOCAL_CHANNEL_ID, "payment_local_channel", "Notifications for payment transactions",MEMBER_GROUP_ID, "Notifications for Member");

        Intent intent = new Intent(context, RiwayatPesananPembeliActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("notificationId", notificationId); // Pass notification ID to the activity
        intent.putExtra("selectedTab", 0);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent dismissIntent = new Intent(context, NotificationDismissedReceiver.class);
        dismissIntent.putExtra("notificationId", notificationId);
        PendingIntent pendingDismissIntent = PendingIntent.getBroadcast(context, notificationId, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PAYMENT_LOCAL_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_akun)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(pendingDismissIntent) // Set the delete intent
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }
    public static void displayCancelLocal(Context context, String title, String body, String orderNumber, String kta) {
        int notificationId = getNotificationIdForOrder(orderNumber) + 2000; // Tambahkan offset untuk memastikan ID unik

        saveOrderCancel(context, kta, orderNumber, notificationId);

        createNotificationChannelWithGroup(context, CANCEL_LOCAL_CHANNEL_ID, "cancel_local_channel", "Notifications for order cancellations",MEMBER_GROUP_ID, "Notifications for Admin");

        Intent intent = new Intent(context, RiwayatPesananPembeliActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("notificationId", notificationId); // Pass notification ID to the activity
        intent.putExtra("selectedTab", 3);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent dismissIntent = new Intent(context, NotificationDismissedReceiver.class);
        dismissIntent.putExtra("notificationId", notificationId);
        PendingIntent pendingDismissIntent = PendingIntent.getBroadcast(context, notificationId, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CANCEL_LOCAL_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_akun)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(pendingDismissIntent) // Set the delete intent
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }

    private static int getNotificationIdForOrder(String orderNumber) {
        // Generate a consistent notification ID based on the order number
        return orderNumber.hashCode();
    }
    public static void saveOrderDetails(Context context, String kta, String orderNumber, int notificationId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("OrderPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Simpan data menggunakan notificationId sebagai kunci
        editor.putString("kta_" + notificationId, kta);
        editor.putString("orderNumber_" + notificationId, orderNumber);
        editor.apply();
    }

    public static void saveOrderCancel(Context context, String kta, String orderNumber, int notificationId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("OrderCancel", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Simpan data menggunakan notificationId sebagai kunci
        editor.putString("kta_" + notificationId, kta);
        editor.putString("orderNumber_" + notificationId, orderNumber);
        editor.apply();
    }
    public static void clearOrderDetails(Context context, int notificationId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("OrderPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Hapus data menggunakan notificationId sebagai kunci
        editor.remove("kta_" + notificationId);
        editor.remove("orderNumber_" + notificationId);
        editor.apply();
    }
    public static void clearOrderCancel(Context context, int notificationId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("OrderCancel", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Hapus data menggunakan notificationId sebagai kunci
        editor.remove("kta_" + notificationId);
        editor.remove("orderNumber_" + notificationId);
        editor.apply();
    }
    // Method to display FCM signup notification


    public static void createNotificationChannelWithGroup(Context context, String channelId, String channelName, String channelDescription, String groupId, String groupName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            // Create notification group
            NotificationChannelGroup group = new NotificationChannelGroup(groupId, groupName);
            notificationManager.createNotificationChannelGroup(group);

            // Create notification channel
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);
            channel.setGroup(groupId);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);

            notificationManager.createNotificationChannel(channel);
        }
    }


}
