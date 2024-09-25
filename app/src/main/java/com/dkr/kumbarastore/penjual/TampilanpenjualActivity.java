package com.dkr.kumbarastore.penjual;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.dkr.kumbarastore.LoginActivity;
import com.dkr.kumbarastore.R;
import com.dkr.kumbarastore.UpdateDatabase;
import com.dkr.kumbarastore.pembeli.NotificationUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.common.util.concurrent.Futures;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TampilanpenjualActivity extends AppCompatActivity {
    private Handler handler;
    private Runnable runnable;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String LOGIN_STATUS_KEY = "isLoggedIn";

    private static final String ROLE_KEY = "role";
    private FirebaseFirestore firestore;
    private TextView tvAngkaDikemas,tvAngkaDibatalkan,TxtRiwayatPenjualan;
    private Futures ApiFutures;

    UpdateDatabase UpdateDatabase = new UpdateDatabase();

    String waktu = UpdateDatabase.getWaktu();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tampilanpenjual);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        firestore = FirebaseFirestore.getInstance();

        tvAngkaDikemas=findViewById(R.id.tvAngkaDikemas);
        tvAngkaDibatalkan=findViewById(R.id.tvAngkaDibatalkan);
        TxtRiwayatPenjualan=findViewById(R.id.TxtRiwayatPenjualan);

        //new LocationChangeReceiver().checkAndRedirectToLocationSettings(this);
       // LocationChangeReceiver.checkLocationStatus(this);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                loadKtasAndOrdersCount();
                handler.postDelayed(this, 5000);
            }
        };


        handler.post(runnable);



        String kta = sharedPreferences.getString("kta", "");
        String deviceInfo = getDeviceInfo();

        TxtRiwayatPenjualan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TampilanpenjualActivity.this, RiwayatPenjualanActivity.class);

                intent.putExtra("selectedTab", 0);
                startActivity(intent);
            }
        });

        CardView cvTambahKategori = findViewById(R.id.cvTambahKategori);
        cvTambahKategori.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TampilanpenjualActivity.this, TambahKategoriActivity.class));
                finish();
            }
        });
        // Tombol untuk logout
        CardView cvKeluar = findViewById(R.id.cvKeluarAkun);
        cvKeluar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        CardView cvproduk = findViewById(R.id.cvProduk);
        cvproduk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TampilanpenjualActivity.this, UploadActivity.class));
                finish();
            }
        });

        CardView cvTambahPesan = findViewById(R.id.cvTambahPesan);
        cvTambahPesan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TampilanpenjualActivity.this, TambahPesanNotifActivity.class));
                finish();
            }
        });
    }


    private void loadKtasAndOrdersCount() {
        List<String> ktaList = new ArrayList<>();

        firestore.collection("users")
                .whereEqualTo("posisi", "pembeli")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String kta = document.getString("kta");
                            if (kta != null) {
                                ktaList.add(kta);
                            }
                        }

                        // Setelah mendapatkan semua KTA, lanjutkan untuk menghitung pesanan "dikemas"
                        loadDikemasCount(ktaList);
                        loadDibatalkanCount(ktaList);
                    } else {
                    }
                })
                .addOnFailureListener(e -> {
                   });
    }

    private void saveDikemasCount(String kta, int dikemasCount) {
        firestore.collection("orders")
                .document("status_summary")
                .collection("dikemas_count")
                .document(kta)
                .set(new HashMap<String, Object>() {{
                    put("dikemasCount", dikemasCount);
                }})
                .addOnSuccessListener(aVoid -> {
                    // Status tersimpan dengan sukses
                })
                .addOnFailureListener(e -> {
                    // Tangani kegagalan penyimpanan
                });
    }

    private void loadDikemasCount(List<String> ktaList) {
        List<Task<Void>> tasks = new ArrayList<>();

        for (String kta : ktaList) {
            Task<QuerySnapshot> queryTask = firestore.collection("orders")
                    .document("dikemas")
                    .collection(kta)
                    .get();

            Task<Void> countAndSaveTask = queryTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                QuerySnapshot querySnapshot = task.getResult();
                int dikemasCount = 0;

                for (QueryDocumentSnapshot document : querySnapshot) {
                    String status = document.getString("status");
                    if ("dikemas".equals(status)) {
                        dikemasCount++;
                    }
                }

                saveDikemasCount(kta, dikemasCount);
                return Tasks.forResult(null); // Return a completed task
            });

            tasks.add(countAndSaveTask);
        }

        Tasks.whenAll(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // After all tasks are complete, calculate the total count
                calculateTotalDikemasCount(ktaList);
            } else {
                // Handle failure
            }
        });
    }

    private void calculateTotalDikemasCount(List<String> ktaList) {
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

        for (String kta : ktaList) {
            tasks.add(firestore.collection("orders")
                    .document("status_summary")
                    .collection("dikemas_count")
                    .document(kta)
                    .get());
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            int totalDikemas = 0;

            for (Object result : results) {
                DocumentSnapshot snapshot = (DocumentSnapshot) result;
                if (snapshot.exists()) {
                    long dikemasCount = snapshot.getLong("dikemasCount");
                    totalDikemas += dikemasCount;
                }
            }

            // Update TextView with total count
            tvAngkaDikemas.setText(String.valueOf(totalDikemas));
        }).addOnFailureListener(e -> {
            // Handle failure
        });
    }

    private void saveDibatalkanCount(String kta, int dibatalkanCount) {
        firestore.collection("orders")
                .document("status_summary")
                .collection("dibatalkan_count")
                .document(kta)
                .set(new HashMap<String, Object>() {{
                    put("dibatalkanCount", dibatalkanCount);
                }})
                .addOnSuccessListener(aVoid -> {
                    // Status tersimpan dengan sukses
                })
                .addOnFailureListener(e -> {
                    // Tangani kegagalan penyimpanan
                });
    }

    private void loadDibatalkanCount(List<String> ktaList) {
        List<Task<Void>> tasks = new ArrayList<>();

        for (String kta : ktaList) {
            Task<QuerySnapshot> queryTask = firestore.collection("orders")
                    .document("dibatalkan")
                    .collection(kta)
                    .get();

            Task<Void> countAndSaveTask = queryTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                QuerySnapshot querySnapshot = task.getResult();
                int dibatalkanCount = 0;

                for (QueryDocumentSnapshot document : querySnapshot) {
                    String status = document.getString("status");
                    if ("dibatalkan".equals(status)) {
                        dibatalkanCount++;
                    }
                }

                saveDibatalkanCount(kta, dibatalkanCount);
                return Tasks.forResult(null); // Return a completed task
            });

            tasks.add(countAndSaveTask);
        }

        Tasks.whenAll(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // After all tasks are complete, calculate the total count
                calculateTotalDibatalkanCount(ktaList);
            } else {
                // Handle failure
            }
        });
    }

    private void calculateTotalDibatalkanCount(List<String> ktaList) {
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

        for (String kta : ktaList) {
            tasks.add(firestore.collection("orders")
                    .document("status_summary")
                    .collection("dibatalkan_count")
                    .document(kta)
                    .get());
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            int totalDikemas = 0;

            for (Object result : results) {
                DocumentSnapshot snapshot = (DocumentSnapshot) result;
                if (snapshot.exists()) {
                    long dikemasCount = snapshot.getLong("dibatalkanCount");
                    totalDikemas += dikemasCount;
                }
            }

            // Update TextView with total count
            tvAngkaDibatalkan.setText(String.valueOf(totalDikemas));
        }).addOnFailureListener(e -> {
            // Handle failure
        });
    }
    private void logoutUser() {
        String role = sharedPreferences.getString(ROLE_KEY, ""); // Ambil role dari SharedPreferences
        String kta = sharedPreferences.getString("kta", ""); // Ambil KTA dari SharedPreferences
        String deviceInfo = getDeviceInfo(); // Implementasi method untuk mendapatkan deviceInfo

        // Panggil metode untuk menghapus token perangkat
        deleteDeviceTokens(role, kta, deviceInfo);

        // Hapus data pengguna yang tersimpan di SharedPreferences
        clearUserData();




        // Redirect ke halaman login setelah logout
        startActivity(new Intent(TampilanpenjualActivity.this, LoginActivity.class));
        finish();
    }

    private String getDeviceInfo() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String osVersion = Build.VERSION.RELEASE;
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        return manufacturer + " " + model + " (Android " + osVersion + ") - ID: " + deviceId;
    }

    private void clearUserData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(LOGIN_STATUS_KEY, false);
        editor.remove("nama"); // Hapus nama pengguna
        editor.remove("kta");  // Hapus nomor KTA
        // Hapus nilai lain yang perlu dihapus setelah logout
        editor.apply();
    }

    private void deleteDeviceTokens(String role, String kta, String deviceInfo) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Mengupdate dokumen token perangkat berdasarkan deviceInfo
        db.collection("tokens")
                .document(role) // Gunakan role sebagai ID dokumen utama
                .collection(kta)
                .whereEqualTo("deviceInfo", deviceInfo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Update dokumen yang ditemukan
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("deviceInfo", deviceInfo);
                            updateData.put("role", role);
                            updateData.put("token", FieldValue.delete()); // Menghapus token
                            updateData.put("waktuAktifTerakhir", waktu ); // Default value, to be set later
                            updateData.put("statusAkun", "logout" ); // Default value, to be set later
                            updateData.put("statusAplikasi", "tidak aktif");

                            db.collection("tokens")
                                    .document(role)
                                    .collection(kta)
                                    .document(document.getId())
                                    .update(updateData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Berhasil diupdate
                                    })
                                    .addOnFailureListener(e -> {
                                        // Gagal diupdate
                                    });
                        }
                    } else {
                        // Task gagal
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Hentikan runnable saat activity berada di belakang
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mulai runnable kembali saat activity aktif
        handler.post(runnable);
    }
    private boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            // Jika tombol back sudah ditekan sebelumnya, langsung kembali ke LoginActivity
            finishAffinity();
        } else {
            // Jika tombol back baru ditekan sekali, tampilkan pesan toast
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Kembali sekali lagi untuk keluar aplikasi", Toast.LENGTH_SHORT).show();

            // Reset status setelah 2 detik
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }
}
