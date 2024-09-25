package com.dkr.kumbarastore.pembeli;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dkr.kumbarastore.LocationChangeReceiver;
import com.dkr.kumbarastore.NetworkUtils;
import com.dkr.kumbarastore.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BerandaActivity extends AppCompatActivity {

    private TextView tvNama, tvKta, tvWelcome1, tvWelcome2;
    private SharedPreferences sharedPreferences;
    private ImageView imgKeranjang, imgLogo;
    private FirebaseFirestore firestore;
    private AlertDialog alertDialog;
    private CardView cvbebas;

    private RecyclerView rvCategories;
    private ProductAdapter adapter;
    private List<Product> productList;
    private List<Product> filteredList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText etSearch;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String LOGIN_STATUS_KEY = "isLoggedIn";

    private static final int REQUEST_STORAGE_PERMISSION = 1;

    private Handler ahandler;
    private Runnable arunnable;

    private Handler loaddatahandler;

    private Runnable loaddatarunnable;

    private Toast lastToast;

    private boolean isToastShown = false; // Flag untuk melacak apakah Toast sudah ditampilkan

    private boolean hasShownNoInternetToast = false;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beranda);

        tvWelcome1 = findViewById(R.id.tvSelamatdatang1);
        tvWelcome2 = findViewById(R.id.tvSelamatdatang2);
        tvNama = findViewById(R.id.tvNama);
        tvKta = findViewById(R.id.tvKta);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        imgLogo = findViewById(R.id.imageView2); // Pastikan imgLogo diinisialisasi
        String nama = sharedPreferences.getString("nama", "");
        String kta = sharedPreferences.getString("kta", "");
        rvCategories = findViewById(R.id.rvCategories);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        firestore = FirebaseFirestore.getInstance();

        imgLogo=findViewById(R.id.imageView2);
        imgKeranjang = findViewById(R.id.ivCart);
        etSearch = findViewById(R.id.etSearch);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        rvCategories = findViewById(R.id.rvCategories);

        rvCategories.setLayoutManager(new GridLayoutManager(this, 2));

        // Initialize lists
        productList = new ArrayList<>();
        filteredList = new ArrayList<>();
        imgLogo.setVisibility(View.VISIBLE);
        tvNama.setText(nama);
        tvKta.setText(kta);

        if (!isLoggedIn()) {
            imgLogo.setVisibility(View.GONE);
            new LocationChangeReceiver().checkAndRedirectToLocationSettings(this);
            //LocationChangeReceiver.checkLocationStatus(this);
        } else {
            tvWelcome1.setVisibility(View.GONE);
            tvWelcome2.setVisibility(View.GONE);
        }

        ShimmerFrameLayout shimmerFrameLayout;
        shimmerFrameLayout = findViewById(R.id.loading);
        shimmerFrameLayout.startShimmer();
        loadDataFromFirestore();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_Beranda);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_Beranda) {
                return true;
            } else if (item.getItemId() == R.id.bottom_kateogri) {
                startActivity(new Intent(getApplicationContext(), KategoriActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.bottom_akun) {
                startActivity(new Intent(getApplicationContext(), AkunActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });

        imgKeranjang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isLoggedIn()) {
                    Toast.makeText(BerandaActivity.this, "Silahkan login terlebih dahulu", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(BerandaActivity.this, KeranjangActivity.class);
                    intent.putExtra("from", "Beranda");
                    startActivity(intent);
                }


            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                loadDataFromFirestore();

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                loadDataFromFirestore(); // Muat ulang data saat refresh
                swipeRefreshLayout.setRefreshing(false);

            }
        });


    }

    private void loadDataFromFirestore() {
        NetworkUtils.checkNetworkQuality(isPingSuccessful -> {
            if (!isPingSuccessful) {
                // Jika belum pernah menampilkan toast, tampilkan dan set flag jadi true
                if (!hasShownNoInternetToast) {
                    showtoastTop(findViewById(android.R.id.content), "Tidak dapat memuat data");
                    hasShownNoInternetToast = true; // Toast sudah ditampilkan
                }

                // Coba lagi panggil loadDataFromFirestore
                loadDataFromFirestore(); // Menambahkan delay sebelum mencoba lagi
                return; // Jangan lanjut jika tidak ada jaringan
            }

            // Reset flag jika koneksi berhasil
            hasShownNoInternetToast = false;

            // Jika ada koneksi, mulai load data dari Firestore
            productList.clear();

            firestore.collection("items")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String imageUrl = documentSnapshot.getString("imageUrl");
                            String name = documentSnapshot.getString("imageName");
                            String price = documentSnapshot.getString("price");
                            int stock = documentSnapshot.getLong("stock").intValue();
                            int terjual = documentSnapshot.getLong("terjual").intValue();
                            String id = documentSnapshot.getId();

                            if (stock > 0) { // Filter produk dengan stok > 0
                                Product product = new Product(imageUrl, name, price, stock, terjual, id);
                                productList.add(product);
                            }
                        }

                        filterProducts(etSearch.getText().toString());

                        if (adapter == null) {
                            adapter = new ProductAdapter(BerandaActivity.this, productList);
                            rvCategories.setAdapter(adapter);
                        } else {
                            adapter.notifyDataSetChanged();
                        }

                        // Stop shimmer setelah data berhasil dimuat
                        ShimmerFrameLayout shimmerFrameLayout = findViewById(R.id.loading);
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        // Tindakan jika gagal memuat data dari Firestore
                    });
        });
    }

    private void showtoastTop(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);

        // Mendapatkan view dari Snackbar
        View snackbarView = snackbar.getView();

        // Mengubah posisi Snackbar menjadi di atas
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
        params.width = FrameLayout.LayoutParams.WRAP_CONTENT; // Lebar sesuai konten
        params.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.TOP|Gravity.CENTER; // Set Snackbar muncul di atas
        snackbarView.setLayoutParams(params);

        // Menampilkan Snackbar
        snackbar.show();
    }

    private void filterProducts(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            // Jika pencarian kosong, tampilkan semua produk
            filteredList.addAll(productList);
        } else {
            String queryLowerCase = query.toLowerCase();

            for (Product product : productList) {
                String productNameLowerCase = product.getName().toLowerCase();
                // Memeriksa apakah nama produk mengandung kata kunci di mana saja
                if (productNameLowerCase.contains(queryLowerCase)) {
                    filteredList.add(product);
                }
            }
        }

        // Update adapter with filtered list
        if (adapter != null) {
            adapter.updateList(filteredList);
        }
    }


    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean(LOGIN_STATUS_KEY, false);

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // Handle other menu item clicks here if needed
            default:
                return super.onOptionsItemSelected(item);
        }
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
