package com.dkr.kumbarastore;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dkr.kumbarastore.pembeli.BerandaActivity;
import com.dkr.kumbarastore.penjual.TampilanpenjualActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TampilanAwalAplikasiActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "UserPrefs";
    private static final String ROLE_KEY = "role";
    private static final String LOGIN_STATUS_KEY = "isLoggedIn";
    private SharedPreferences sharedPreferences;
    private boolean isToastShown = false; // Flag untuk melacak apakah Toast sudah ditampilkan
    private static final int SPLASH_DURATION = 3000; // Durasi splash screen dalam milidetik
    private MediaPlayer mediaPlayer;

    private LocationCallback locationCallback;

    private static final String TIMEZONEDB_API_KEY = "H0FD3KC6UTWL"; // Ganti dengan API key kamu

    private MyApp MyApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tampilan_awal_aplikasi);

        UpdateDatabase.checkLocationStatus(this);
        UpdateDatabase.updateLocation(this);
        MyApp = (MyApp) getApplicationContext();
        MyApp.stopNetworkChecks();

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Mainkan suara splash screen
        playStartSound();


        // Delay dan kemudian pindah ke aktivitas utama
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Buat animasi fade out pada root view
                View rootView = findViewById(android.R.id.content);
                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(rootView, "alpha", 1f, 0f);
                fadeOut.setDuration(1000); // Durasi animasi fade out
                fadeOut.start();

                // Pindah ke aktivitas utama setelah animasi selesai
                fadeOut.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {}

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    checkLoginStatus();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {}

                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                });
            }
        }, SPLASH_DURATION);


    }

    private void checkLoginStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean(LOGIN_STATUS_KEY, false);
        String kta = sharedPreferences.getString("kta", "");
        String nama = sharedPreferences.getString("nama", "");
        String role = sharedPreferences.getString(ROLE_KEY, "");

        if (TextUtils.isEmpty(kta) || TextUtils.isEmpty(role)) {
            // Jika kosong, arahkan pengguna ke LoginActivity
            Intent intent = new Intent(TampilanAwalAplikasiActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0); // Tidak ada animasi default
            finish(); // Menghentikan TampilanAwalAplikasiActivity
            return; // Keluar dari metode
        }
        if (isLoggedIn) {
            String deviceInfo = getDeviceInfo();

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Cek status akun di koleksi "users"
            String cari = kta + " " + nama;
            db.collection("users")
                    .document(cari) // Menggunakan kta + nama sebagai ID dokumen
                    .get()
                    .addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful()) {
                            DocumentSnapshot userDocument = userTask.getResult();
                            if (userDocument.exists()) {
                                String statusAkun = userDocument.getString("status");

                                if ("block".equals(statusAkun)) {
                                    // Status akun adalah "block", tampilkan pesan dan hentikan navigasi
                                    Toast.makeText(TampilanAwalAplikasiActivity.this, "Akun Anda diblokir. Silahkan Hubungi Admin.", Toast.LENGTH_LONG).show();
                                    HapusDataAplikasi();
                                } else {
                                    // Cek status aplikasi di koleksi "tokens"
                                    db.collection("tokens")
                                            .document(role)
                                            .collection(kta)
                                            .whereEqualTo("deviceInfo", deviceInfo)
                                            .get()
                                            .addOnCompleteListener(tokenTask -> {
                                                if (tokenTask.isSuccessful() && !tokenTask.getResult().isEmpty()) {
                                                    DocumentSnapshot tokenDocument = tokenTask.getResult().getDocuments().get(0);
                                                    String statusAplikasi = tokenDocument.getString("statusAkun");

                                                    if ("logout".equals(statusAplikasi)) {
                                                        // Status aplikasi adalah "LogOut", tampilkan pesan dan logout
                                                        String message = String.format("Anda telah keluar dari akun \"%s\", silahkan login kembali.", nama);
                                                        Toast.makeText(TampilanAwalAplikasiActivity.this, message, Toast.LENGTH_LONG).show();
                                                        HapusDataAplikasi(); // Hapus data aplikasi atau logout
                                                        // Tetap di tampilan login, tidak melakukan redirect
                                                    } else {
                                                        // Status aplikasi tidak "LogOut", lanjutkan ke halaman login
                                                        redirectToHome(role);
                                                    }
                                                } else {
                                                    // Tangani jika dokumen token tidak ditemukan atau task gagal
                                                    Intent intent = new Intent(TampilanAwalAplikasiActivity.this, LoginActivity.class);
                                                    startActivity(intent);
                                                    overridePendingTransition(0, 0); // Tidak ada animasi default
                                                    finish(); // Menghentikan TampilanAwalAplikasiActivity
                                                }
                                            });
                                }
                            } else {
                                // Tangani jika dokumen pengguna tidak ditemukan
                                Intent intent = new Intent(TampilanAwalAplikasiActivity.this, LoginActivity.class);
                                startActivity(intent);
                                overridePendingTransition(0, 0); // Tidak ada animasi default
                                finish(); // Menghentikan TampilanAwalAplikasiActivity
                            }
                        } else {
                            // Tangani jika pengecekan status akun gagal
                            Intent intent = new Intent(TampilanAwalAplikasiActivity.this, LoginActivity.class);
                            startActivity(intent);
                            overridePendingTransition(0, 0); // Tidak ada animasi default
                            finish(); // Menghentikan TampilanAwalAplikasiActivity
                        }
                    });
        } else {
            // Jika tidak login, alihkan ke tampilan login
            Intent intent = new Intent(TampilanAwalAplikasiActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0); // Tidak ada animasi default
            finish(); // Menghentikan TampilanAwalAplikasiActivity
        }
        }
    private void HapusDataAplikasi() {
        // Implementasikan logout di sini
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(TampilanAwalAplikasiActivity.this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0); // Tidak ada animasi default
        finish();
    }
    private String getDeviceInfo() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String osVersion = Build.VERSION.RELEASE;
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        return manufacturer + " " + model + " (Android " + osVersion + ") - ID: " + deviceId;
    }
    private void redirectToHome(String role) {
        // Status is not "block", proceed with the redirection
        if ("penjual".equals(role)) {
            Toast.makeText(TampilanAwalAplikasiActivity.this, "Hallo selamat datang kembali.", Toast.LENGTH_SHORT).show();
            UpdateDatabase.UpdateAktif(this);
            Intent intent = new Intent(TampilanAwalAplikasiActivity.this, TampilanpenjualActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            UpdateDatabase.UpdateAktif(this);
            Intent intent = new Intent(TampilanAwalAplikasiActivity.this, BerandaActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
    private void playStartSound() {
        mediaPlayer = MediaPlayer.create(this, R.raw.start_sound);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mp -> {
            mp.release(); // Melepaskan media player setelah selesai memutar suara
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();

        MyApp.startNetworkChecks(); // Start network check when leaving JaringanActivity
    }

}
