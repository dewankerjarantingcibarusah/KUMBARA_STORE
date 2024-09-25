package com.dkr.kumbarastore;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etNama, etKta;
    private Button btnLogin, btnLewat;
    private TextView textViewSignup;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String LOGIN_STATUS_KEY = "isLoggedIn";
    private static final String ROLE_KEY = "role";

    UpdateDatabase UpdateDatabase = new UpdateDatabase();
    String waktu = UpdateDatabase.getWaktu();

    private LocationCallback locationCallback;

    private static final String TIMEZONEDB_API_KEY = "H0FD3KC6UTWL"; // Ganti dengan API key kamu
    private static final int REQUEST_STORAGE_PERMISSION = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String kta = sharedPreferences.getString("kta", "");

        etNama = findViewById(R.id.etNama);
        etKta = findViewById(R.id.etKta);
        btnLogin = findViewById(R.id.btnLogin);
        btnLewat = findViewById(R.id.btnLewat);
        textViewSignup = findViewById(R.id.textViewSignup);

        btnLogin.setOnClickListener(v -> loginUser());
        btnLewat.setOnClickListener(v -> skipLogin());


        textViewSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });


        checkLoginStatus();
        checkPermissions();
    }

    private void checkPermissions() {
        boolean storagePermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        boolean locationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (!storagePermissionGranted || !locationPermissionGranted) {
            // List to hold the permissions to request
            List<String> permissionsToRequest = new ArrayList<>();

            if (!storagePermissionGranted) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (!locationPermissionGranted) {
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        permissionsToRequest.toArray(new String[0]),
                        REQUEST_STORAGE_PERMISSION);  // Use the same request code for simplicity
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {

            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {

            }
        }
    }

    private void skipLogin() {
        Intent intent = new Intent(LoginActivity.this, BerandaActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void checkLoginStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean(LOGIN_STATUS_KEY, false);


        if (isLoggedIn) {
            String role = sharedPreferences.getString(ROLE_KEY, "");
            redirectToHome(role);
        }
    }

    private void loginUser() {
        final String nama = etNama.getText().toString().trim();
        final String kta = etKta.getText().toString().trim();

        if (TextUtils.isEmpty(nama)) {
            Toast.makeText(this, "Masukkan nama", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(kta)) {
            Toast.makeText(this, "Masukkan nomor KTA", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("nama", nama)
                .whereEqualTo("kta", kta)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            Toast.makeText(LoginActivity.this, "Login gagal. Data pengguna " + nama + " " + kta + " tidak ditemukan, Silahkan Hubungi Admin", Toast.LENGTH_SHORT).show();
                        } else {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            String statusAkun = document.getString("status");

                            if ("block".equals(statusAkun)) {
                                // Jika status akun adalah "block", tampilkan pesan dan hentikan proses login
                                Toast.makeText(LoginActivity.this, "Akun Anda diblokir. Silahkan Hubungi Admin.", Toast.LENGTH_LONG).show();
                            } else {
                                // Jika akun tidak diblokir, lanjutkan dengan proses login
                                String posisi = document.getString("posisi");

                                saveLoginStatus(posisi);
                                saveUserData(document.getString("nama"), document.getString("kta"));

                                FirebaseMessaging.getInstance().getToken()
                                        .addOnCompleteListener(tokenTask1 -> {
                                            if (tokenTask1.isSuccessful()) {
                                                String deviceToken = tokenTask1.getResult();
                                                String deviceInfo = getDeviceInfo();
                                                saveDeviceTokenToFirestore(deviceToken, deviceInfo, posisi, kta);
                                                saveUserToken(deviceToken);
                                            }
                                        });

                                // Redirect to home
                                redirectToHome(posisi);
                            }
                        }
                    } else {
                        // Handle failure
                        Toast.makeText(LoginActivity.this, "Terjadi kesalahan. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                    }

                });
    }


    private String getDeviceInfo() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String osVersion = Build.VERSION.RELEASE;
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        return manufacturer + " " + model + " (Android " + osVersion + ") - ID: " + deviceId;
    }

    private void saveDeviceTokenToFirestore(String deviceToken, String deviceInfo, String role, String kta) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Data yang akan disimpan
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("deviceInfo", deviceInfo);
        tokenData.put("role", role);
        tokenData.put("token", deviceToken);
        tokenData.put("waktuAktifTerakhir", waktu);
        tokenData.put("statusAkun", "login" );
        tokenData.put("statusAplikasi", "aktif");

        // Menyimpan data ke Firestore
        db.collection("tokens")
                .document(role)
                .collection(kta)
                .document(deviceInfo) // Dokumen tunggal "deviceInfo"
                .set(tokenData)
                .addOnSuccessListener(aVoid -> {
                    })
                .addOnFailureListener(e -> {
                   });
    }

    private void saveUserData(String nama, String kta) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("nama", nama);
        editor.putString("kta", kta);
        editor.apply();
    }

    private void saveUserToken(String token) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    private void saveLoginStatus(String role) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(LOGIN_STATUS_KEY, true);
        editor.putString(ROLE_KEY, role);
        editor.apply();
    }

    private void redirectToHome(String role) {
        // Status is not "block", proceed with the redirection
        if ("penjual".equals(role)) {
            Toast.makeText(LoginActivity.this, "Hallo selamat datang kembali.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, TampilanpenjualActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Intent intent = new Intent(LoginActivity.this, BerandaActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
    private void checkLocationStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isLocationEnabled) {


        } else {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            waktu = sdf.format(calendar.getTime());
        }
    }
    private void caritempat() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    try {
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            Address address = addresses.get(0);
                            String locality = address.getLocality();
                            String subLocality = address.getSubLocality();
                            String countryCode = address.getCountryCode();

                            String locationName = (subLocality != null ? subLocality + ", " : "") + (locality != null ? locality : countryCode);

                            // Panggil TimeZoneDB API untuk mendapatkan waktu lokal berdasarkan lokasi
                            getLocalTimeFromTimeZoneDB(latitude, longitude);
                        }
                    } catch (IOException e) {
                        // Handle exception here if needed
                    }
                }
            }
        };

        // Coba dapatkan lokasi terakhir
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location == null) {
                        // Jika lokasi terakhir tidak tersedia, minta pembaruan lokasi
                        requestNewLocationData(fusedLocationClient);
                    } else {
                        // Lokasi tersedia, gunakan langsung
                        processLocation(location);
                    }
                });
    }

    private void requestNewLocationData(FusedLocationProviderClient fusedLocationClient) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void getLocalTimeFromTimeZoneDB(double latitude, double longitude) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        String url = "https://api.timezonedb.com/v2.1/get-time-zone?key=" + TIMEZONEDB_API_KEY + "&format=json&by=position&lat=" + latitude + "&lng=" + longitude;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure if needed
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();

                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        String timeZone = jsonObject.getString("zoneName");
                        waktu = jsonObject.getString("formatted");

                        // Process time zone and formatted time if needed

                    } catch (Exception e) {
                        // Handle exception if needed
                    }
                }
            }
        });
    }

    private void processLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String locality = address.getLocality();
                String subLocality = address.getSubLocality();
                String countryCode = address.getCountryCode();

                String locationName = (subLocality != null ? subLocality + ", " : "") + (locality != null ? locality : countryCode);

                // Panggil TimeZoneDB API untuk mendapatkan waktu lokal berdasarkan lokasi
                getLocalTimeFromTimeZoneDB(latitude, longitude);
            }
        } catch (IOException e) {
            // Handle exception if needed
        }
    }
    @Override
    public void onBackPressed() {
        finishAffinity(); // Menutup semua aktivitas dalam tumpukan back stack
    }
}
