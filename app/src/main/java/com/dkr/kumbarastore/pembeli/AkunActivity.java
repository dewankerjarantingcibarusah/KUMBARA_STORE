package com.dkr.kumbarastore.pembeli;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationManagerCompat;

import com.dkr.kumbarastore.LoginActivity;
import com.dkr.kumbarastore.R;
import com.dkr.kumbarastore.SignupActivity;
import com.dkr.kumbarastore.UpdateDatabase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class AkunActivity extends AppCompatActivity {

    private AlertDialog alertDialog;
    private ImageView imageView;
    private TextView tvNomorPembeli, tvNamaPembeli, tvAlamatPembeli, LihatRiwayat;

    private FirebaseFirestore firestore;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String LOGIN_STATUS_KEY = "isLoggedIn";
    private static final String ROLE_KEY = "role";

    UpdateDatabase UpdateDatabase = new UpdateDatabase();

    String waktu = UpdateDatabase.getWaktu();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_akun);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String kta = sharedPreferences.getString("kta", "");

        tvNamaPembeli =findViewById(R.id.tvNama);
        tvNomorPembeli = findViewById(R.id.tvNomor);
        tvAlamatPembeli =findViewById(R.id.tvAlamat);

        firestore = FirebaseFirestore.getInstance();


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_akun);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_Beranda) {
                startActivity(new Intent(getApplicationContext(), BerandaActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.bottom_kateogri) {
                startActivity(new Intent(getApplicationContext(), KategoriActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.bottom_akun) {
                // Already in AkunActivity, do nothing
                return true;
            }
            return false;
        });

        CardView cvKeluar = findViewById(R.id.cvKeluarAkun);
        cvKeluar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceInfo = getDeviceInfo();
                String role = sharedPreferences.getString(ROLE_KEY, ""); // Ambil role dari SharedPreferences
                String kta = sharedPreferences.getString("kta", ""); // Ambil KTA dari SharedPreferences

                // Panggil metode untuk menghapus token perangkat
                deleteDeviceTokens(role, kta, deviceInfo);

                clearNotificationsForKTA(kta);

                clearAllOrderDetails(AkunActivity.this);



                logoutUser();
            }
        });

        CardView cvdikemas = findViewById(R.id.cvDikemas);
        cvdikemas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AkunActivity.this, RiwayatPesananPembeliActivity.class);
                intent.putExtra("kta", kta);
                intent.putExtra("from", "akun");
                intent.putExtra("selectedTab", 0);
                startActivity(intent);
            }
        });

        CardView cvdikirim = findViewById(R.id.cvDikirim);
        cvdikirim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AkunActivity.this, RiwayatPesananPembeliActivity.class);
                intent.putExtra("kta", kta);
                intent.putExtra("from", "akun");
                intent.putExtra("selectedTab", 1);
                startActivity(intent);
            }
        });

        CardView cvselesai = findViewById(R.id.cvSelesai);
        cvselesai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AkunActivity.this, RiwayatPesananPembeliActivity.class);
                intent.putExtra("kta", kta);
                intent.putExtra("from", "akun");
                intent.putExtra("selectedTab", 2);
                startActivity(intent);
            }
        });

        CardView cvdibatalkan = findViewById(R.id.cvDibatalkan);
        cvdibatalkan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AkunActivity.this, RiwayatPesananPembeliActivity.class);
                intent.putExtra("kta", kta);
                intent.putExtra("from", "akun");
                intent.putExtra("selectedTab", 3);
                startActivity(intent);
            }
        });
        LihatRiwayat = findViewById(R.id.tvLihatRiwayat);
        LihatRiwayat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String role = sharedPreferences.getString(ROLE_KEY, ""); // Ambil role dari SharedPreferences
                String kta = sharedPreferences.getString("kta", ""); // Ambil KTA dari SharedPreferences

                Intent intent = new Intent(AkunActivity.this, RiwayatPesananPembeliActivity.class);
                intent.putExtra("kta", kta);
                intent.putExtra("from", "akun");
                startActivityForResult(intent, 100); // Request code 100
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar); // Ganti dengan ID toolbar Anda
        setSupportActionBar(toolbar);



        // Atur teks nama di Toolbar jika perlu
        getSupportActionBar().setTitle("Profil Saya");



        String nama = sharedPreferences.getString("nama", "");

        if (!isLoggedIn()) {
            showLoginAlertDialog();

        } else {
            loadUserData(kta);
            cvKeluar.setVisibility(View.VISIBLE);
        }

    }

    private void loadUserData(String kta) {
        // Pastikan KTA tidak kosong
        if (!kta.isEmpty()) {
            firestore.collection("users")
                    .whereEqualTo("kta", kta)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    // Ambil data nama dan nomor telepon dari Firestore
                                    String nama = documentSnapshot.getString("nama");
                                    String nomorTelepon = documentSnapshot.getString("notelp");

                                    // Tampilkan data nama dan nomor telepon ke TextView yang sesuai
                                    tvNamaPembeli.setText(nama);
                                    tvNomorPembeli.setText(nomorTelepon);

                                    // Ambil dokumen ID untuk mengakses sub-koleksi alamat
                                    String documentId = documentSnapshot.getId();

                                    // Ambil alamat utama dari sub-koleksi "alamat pengiriman"
                                    firestore.collection("users")
                                            .document(documentId)
                                            .collection("alamat pengiriman")
                                            .whereEqualTo("alamatutama", "yes")
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot alamatSnapshots) {
                                                    if (!alamatSnapshots.isEmpty()) {
                                                        for (QueryDocumentSnapshot alamatSnapshot : alamatSnapshots) {
                                                            // Ambil data alamat dari sub-koleksi alamat
                                                            String alamat = alamatSnapshot.getString("alamat");
                                                            // Tampilkan data alamat ke TextView yang sesuai
                                                            tvAlamatPembeli.setText(alamat);
                                                        }
                                                    } else {
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                }
                                            });
                                }
                            } else {
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
        } else {
        }
    }
    public static void clearAllOrderDetails(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("OrderPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Ambil semua kunci dan hapus satu per satu
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            editor.remove(entry.getKey());
        }

        editor.apply();
    }
    private void clearNotificationsForKTA(String kta) {
        SharedPreferences sharedPreferences = getSharedPreferences("OrderPrefs", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("kta_")) {
                int notificationId = Integer.parseInt(key.substring(4));
                String storedKta = sharedPreferences.getString(key, null);
                if (kta.equals(storedKta)) {
                    // Hapus notifikasi dari panel notifikasi
                    notificationManager.cancel(notificationId);

                    // Hapus detail dari SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove("kta_" + notificationId);
                    editor.remove("orderNumber_" + notificationId);
                    editor.apply();
                }
            }
        }
    }
    private String getDeviceInfo() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String osVersion = Build.VERSION.RELEASE;
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        return manufacturer + " " + model + " (Android " + osVersion + ") - ID: " + deviceId;
    }

    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean(LOGIN_STATUS_KEY, false);
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
    private void showLoginAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Anda belum login");
        builder.setMessage("Silakan login atau register untuk melanjutkan.");

        builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent loginIntent = new Intent(AkunActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                dialog.dismiss(); // Menutup dialog setelah memulai LoginActivity
                finish(); // Menutup AkunActivity dan kembali ke BerandaActivity setelah login
            }
        });

        builder.setNegativeButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent registerIntent = new Intent(AkunActivity.this, SignupActivity.class);
                startActivity(registerIntent);
                dialog.dismiss(); // Menutup dialog setelah memulai SignupActivity
                finish(); // Menutup AkunActivity dan kembali ke BerandaActivity setelah registrasi
            }
        });

        builder.setCancelable(true);
        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // Kembali ke BerandaActivity jika dialog dibatalkan (tanpa memilih Login atau Register)
                Intent intent = new Intent(AkunActivity.this, BerandaActivity.class);
                startActivity(intent);
                finish();
            }
        });
        alertDialog.show();
    }
    private void logoutUser() {
        clearUserData();

        startActivity(new Intent(AkunActivity.this, LoginActivity.class));
        finish();
    }
    private void clearUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("nama"); // Menghapus nama pengguna
        editor.remove("kta"); // Menghapus KTA
        editor.remove(ROLE_KEY); // Menghapus role
        editor.remove(LOGIN_STATUS_KEY); // Atau hapus status login jika perlu

        // Hapus nilai lain yang perlu dihapus setelah logout
        editor.apply();
    }
    private void deleteImageAndClearUrl(String imageName) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("uploads/" + imageName+".jpg");

        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File berhasil dihapus
                Toast.makeText(AkunActivity.this, "File deleted successfully", Toast.LENGTH_SHORT).show();
                clearImageUrlInFirestore(imageName);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Gagal menghapus file
                Toast.makeText(AkunActivity.this, "Failed to delete file", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void clearImageUrlInFirestore(String imageName) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("items")
                .document(imageName)
                .update("imageUrl", "")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // URL gambar berhasil dihapus di Firestore
                        Toast.makeText(AkunActivity.this, "Image URL cleared in Firestore", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Gagal menghapus URL gambar di Firestore
                        Toast.makeText(AkunActivity.this, "Failed to clear image URL in Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, BerandaActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


}
