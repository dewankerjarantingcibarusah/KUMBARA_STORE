package com.dkr.kumbarastore;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

public class MyApp extends Application {

    private static MyApp instance;
    private boolean isInForeground = false;

    private NetworkChangeReceiver networkChangeReceiver;

    private LocationChangeReceiver LocationChangeReceiver;

    private static final String PREFS_NAME = "UserPrefs";
    private static final String ROLE_KEY = "role";
    private static final String LOGIN_STATUS_KEY = "isLoggedIn";
    private SharedPreferences sharedPreferences;
    private int activityReferences = 0;

    private Activity currentActivity;
    private boolean isActivityChangingConfigurations = false;
    private boolean isNetworkAvailable = true; // Assume network is available initially
    private boolean isToastShown = false;
    private Handler toastHandler = new Handler();
    private Toast lastToast;
    private Handler updateHandler = new Handler();
    private Runnable updateRunnable;
    private Handler networkCheckhandler;
    private Runnable networkCheckRunnable;

    private static final long INTERVAL_MS = 3000;



    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Daftarkan LocationChangeReceiver
        LocationChangeReceiver = new LocationChangeReceiver();
        IntentFilter locationFilter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(LocationChangeReceiver, locationFilter);

        // Inisialisasi Runnable
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                // Jalankan metode pembaruan
                UpdateDatabase.checkLocationStatus(getApplicationContext());
                UpdateDatabase.updateLocation(getApplicationContext());
                updateHandler.postDelayed(this, 1000);
            }
        };

        networkCheckhandler = new Handler(Looper.getMainLooper());
        networkCheckRunnable = new Runnable() {
            @Override
            public void run() {
                NetworkUtils.checkNetworkQuality(isPingSuccessful -> {
                    if (isPingSuccessful) {
                        if (!isNetworkAvailable) {
                            // Network became available again
                            isNetworkAvailable = true;
                            if (isToastShown) {
                                networkCheckhandler.post(() -> {
                                    isToastShown = false; // Reset toastShown flag
                                    new Handler(Looper.getMainLooper()).post(() -> showSnackbarAtTop("koneksi internet tersedia"));
                                });
                                networkCheckhandler.postDelayed(this, 1000); // Schedule next run
                            }
                        }
                    } else {
                        if (isNetworkAvailable) {
                            // Network became weak
                            isNetworkAvailable = false;
                            networkCheckhandler.post(() -> {
                                isToastShown = true; // Set toastShown flag
                                // Open JaringanActivity and stop further checks
                                new Handler(Looper.getMainLooper()).post(() -> showSnackbarAtTop("Tidak ada koneksi internet"));
                            });
                        }
                    }
                });
                networkCheckhandler.postDelayed(this, INTERVAL_MS); // Schedule next run
            }
        };
        // Mulai handler pertama kali
        updateHandler.post(updateRunnable);
        networkCheckhandler.post(networkCheckRunnable); // Start periodic checks

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

            @Override
            public void onActivityStarted(Activity activity) {
                // Cek apakah pengguna sudah login dan data penting sudah tersedia
                SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.getBoolean(LOGIN_STATUS_KEY, false);
                String kta = sharedPreferences.getString("kta", "");
                String role = sharedPreferences.getString(ROLE_KEY, "");

                if (!isLoggedIn || TextUtils.isEmpty(kta) || TextUtils.isEmpty(role)) {
                    // Jika belum login atau data penting belum tersedia, arahkan ke layar login atau pengaturan awal

                } else if (++activityReferences == 1 && !isActivityChangingConfigurations) {
                    // Aplikasi masuk ke latar depan
                    currentActivity = activity;
                    isInForeground = true;

                    UpdateDatabase.UpdateAktif(activity.getApplicationContext());
                    {
                      UpdateDatabase.UpdateAktif(activity.getApplicationContext());
                    }
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
                currentActivity = activity;

                isInForeground = true;
            }

            @Override
            public void onActivityPaused(Activity activity) {
                if (currentActivity == activity) {
                    isInForeground = false;
                    currentActivity = null;
                }
                isInForeground = false;
            }

            @Override
            public void onActivityStopped(Activity activity) {
                // Cek apakah pengguna sudah login dan data penting sudah tersedia
                isActivityChangingConfigurations = activity.isChangingConfigurations();
                SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.getBoolean(LOGIN_STATUS_KEY, false);
                String kta = sharedPreferences.getString("kta", "");
                String role = sharedPreferences.getString(ROLE_KEY, "");

                if (!isLoggedIn || TextUtils.isEmpty(kta) || TextUtils.isEmpty(role)) {
                    // Jika belum login atau data penting belum tersedia, arahkan ke layar login atau pengaturan awal

                } else if (--activityReferences == 0 && !isActivityChangingConfigurations) {
                    // Aplikasi masuk ke latar belakang
                    if (currentActivity == activity) {
                        isInForeground = false;
                        currentActivity = null;
                    }
                    isInForeground = false;
                    UpdateDatabase.UpdateTidakAktif(activity.getApplicationContext());
                    {
                        UpdateDatabase.UpdateTidakAktif(activity.getApplicationContext());
                    }
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    private void checkLoginStatus() {

    }
    public static MyApp getInstance() {
        return instance;
    }

    public static boolean isInForeground() {
        return instance.isInForeground;
    }
    public void stopNetworkChecks() {
        networkCheckhandler.removeCallbacks(networkCheckRunnable);
    }

    public void startNetworkChecks() {
        networkCheckhandler.post(networkCheckRunnable);
    }
    @Override
    public void onTerminate() {
        super.onTerminate();
        networkCheckhandler.removeCallbacks(networkCheckRunnable); // Clean up when app is terminated

        if (LocationChangeReceiver != null) {
            unregisterReceiver(LocationChangeReceiver);
        }
    }

    private void showSnackbarAtTop(String message) {
        if (currentActivity != null) {
            View rootView = currentActivity.findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);

            // Modify Snackbar appearance
            View snackbarView = snackbar.getView();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
            params.width = FrameLayout.LayoutParams.WRAP_CONTENT; // Width according to content
            params.height = FrameLayout.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL; // Set Snackbar at the top
            snackbarView.setLayoutParams(params);

            snackbar.show();
        }
    }
}
