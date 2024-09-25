package com.dkr.kumbarastore;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;

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

public class UpdateDatabase {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String ROLE_KEY = "role";
    private static final String LOGIN_STATUS_KEY = "isLoggedIn";
    private SharedPreferences sharedPreferences;

    private static String waktu;

    private LocationCallback locationCallback;

    private static final String TIMEZONEDB_API_KEY = "H0FD3KC6UTWL";

    public static void UpdateAktif(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String kta = sharedPreferences.getString("kta", "");
        String role = sharedPreferences.getString("role", "");
        String deviceInfo = getDeviceInfo(context);

        // Mengupdate dokumen token perangkat berdasarkan deviceInfo
        db.collection("tokens")
                .document(role)
                .collection(kta)
                .whereEqualTo("deviceInfo", deviceInfo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(tokenTask1 -> {
                                    if (tokenTask1.isSuccessful()) {
                                        String deviceToken = tokenTask1.getResult();

                                        // Iterasi melalui hasil query untuk memperbarui dokumen yang ditemukan
                                        for (DocumentSnapshot document : task.getResult()) {
                                            Map<String, Object> updateData = new HashMap<>();
                                            updateData.put("deviceInfo", deviceInfo);
                                            updateData.put("role", role);
                                            updateData.put("token", deviceToken);
                                            updateData.put("waktuAktifTerakhir", waktu ); // Default value, to be set later
                                            updateData.put("statusAkun", "login" ); // Default value, to be set later
                                            updateData.put("statusAplikasi", "aktif");

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
                                        // Token gagal diambil
                                    }
                                });
                    } else {
                        // Query gagal
                    }
                });
    }
    public static void UpdateTidakAktif(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String kta = sharedPreferences.getString("kta", "");
        String role = sharedPreferences.getString("role", "");
        String deviceInfo = getDeviceInfo(context);

        // Mengupdate dokumen token perangkat berdasarkan deviceInfo
        db.collection("tokens")
                .document(role)
                .collection(kta)
                .whereEqualTo("deviceInfo", deviceInfo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(tokenTask1 -> {
                                    if (tokenTask1.isSuccessful()) {
                                        String deviceToken = tokenTask1.getResult();

                                        // Iterasi melalui hasil query untuk memperbarui dokumen yang ditemukan
                                        for (DocumentSnapshot document : task.getResult()) {
                                            Map<String, Object> updateData = new HashMap<>();
                                            updateData.put("deviceInfo", deviceInfo);
                                            updateData.put("role", role);
                                            updateData.put("token", deviceToken);
                                            updateData.put("waktuAktifTerakhir", waktu ); // Default value, to be set later
                                            updateData.put("statusAkun", "login" ); // Default value, to be set later
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
                                        // Token gagal diambil
                                    }
                                });
                    } else {
                        // Query gagal
                    }
                });
    }
    public static String checkLocationStatus(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        boolean isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isLocationEnabled) {
            return waktu; // Location services are enabled
        } else {

            java.util.Calendar calendar = java.util.Calendar.getInstance();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            waktu = sdf.format(calendar.getTime());
            return waktu;
        }
    }
    public static void updateLocation(Context context) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        LocationCallback locationCallback = new LocationCallback() {
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
                            getLocalTimeFromTimeZoneDB(latitude, longitude, context);
                        }
                    } catch (IOException e) {
                        // Handle exception here if needed
                    }
                }
            }
        };

        // Coba dapatkan lokasi terakhir
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        // Jika lokasi terakhir tidak tersedia, minta pembaruan lokasi
                        requestNewLocationData(fusedLocationClient, locationCallback);
                    } else {
                        // Lokasi tersedia, gunakan langsung
                        processLocation(location, context);
                    }
                });
    }

    private static void requestNewLocationData(FusedLocationProviderClient fusedLocationClient, LocationCallback locationCallback) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private static void getLocalTimeFromTimeZoneDB(double latitude, double longitude, Context context) {
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
                        String formattedTime = jsonObject.getString("formatted");

                        // Parse the formatted time string into a Date object
                        java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());

                        // Assuming formattedTime is in format "yyyy-MM-dd HH:mm:ss"
                        java.util.Date date = inputFormat.parse(formattedTime);

                        // Format the Date object into the desired format
                         waktu = outputFormat.format(date);

                        // Use the formatted 'waktu' as needed
                    } catch (Exception e) {
                        // Handle exception if needed
                    }
                }
            }
        });
    }


    private static void processLocation(Location location, Context context) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String locality = address.getLocality();
                String subLocality = address.getSubLocality();
                String countryCode = address.getCountryCode();

                String locationName = (subLocality != null ? subLocality + ", " : "") + (locality != null ? locality : countryCode);

                // Panggil TimeZoneDB API untuk mendapatkan waktu lokal berdasarkan lokasi
                getLocalTimeFromTimeZoneDB(latitude, longitude, context);
            }
        } catch (IOException e) {
            // Handle exception if needed
        }
    }

    private static String getDeviceInfo(Context context) {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String osVersion = Build.VERSION.RELEASE;
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return manufacturer + " " + model + " (Android " + osVersion + ") - ID: " + deviceId;
    }
    public String getWaktu() {
        return waktu;
    }
}
