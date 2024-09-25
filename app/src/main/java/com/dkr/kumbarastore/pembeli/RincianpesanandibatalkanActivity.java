package com.dkr.kumbarastore.pembeli;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dkr.kumbarastore.LoginActivity;
import com.dkr.kumbarastore.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

public class RincianpesanandibatalkanActivity extends AppCompatActivity {
    private TextView tvBatalkan;

    private List<Product> productList;
    private RincianpesanandibatalkanAdapter adapter;
    private RecyclerView rvCancelDetail;
    private FirebaseFirestore firestore;
    private String kta;
    private String orderNumber;
    private TextView tvNamaPembeli, tvNomorPembeli, tvAlamatPembeli, tvOrderTime, tvDeliveryTime, tvKatatotalharga, tvTotalHarga, tvNopesanan, tvCancelTime;

    private RincianpesanandibatalkanAdapter batalkan;
    private List<Product> productListdibatalkan;
    private SharedPreferences sharedPreferences;
    private static final String LOGIN_STATUS_KEY = "isLoggedIn";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rincianpesanandibatalkan);

        rvCancelDetail = findViewById(R.id.rvRincianpesanandibatalkan);
        tvNamaPembeli = findViewById(R.id.tvNamaPembeli);
        tvNomorPembeli = findViewById(R.id.tvNomorPembeli);
        tvAlamatPembeli = findViewById(R.id.tvAlamatPembeli);
        tvOrderTime = findViewById(R.id.tvKataWaktuPemesanan);
        tvDeliveryTime = findViewById(R.id.tvTekspesanandisiapkan);
        tvCancelTime = findViewById(R.id.tvKataWaktuDibatalkan);
        tvTotalHarga = findViewById(R.id.tvHargaTotalPesanan);
        tvNopesanan = findViewById(R.id.tvKataNopesanan);
        tvKatatotalharga = findViewById(R.id.tvKatatotalpesanan);

        rvCancelDetail.setLayoutManager(new LinearLayoutManager(this));

        // Initialize variables
        productListdibatalkan = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Rincian Pesanan");

        tvBatalkan = findViewById(R.id.tvTekspesanandibatalkan);

        tvBatalkan.setText("Pesanan telah dibatalkan. Klik \"Lihat Rincian Pembatalan\" untuk informasi lebih lanjut.");

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        if (!isLoggedIn()) {
            Intent loginIntent = new Intent(RincianpesanandibatalkanActivity.this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        } else {
            // Get notificationId from intent
            int notificationId = getIntent().getIntExtra("notificationId", -1);
            if (notificationId != -1) {
                // Get kta and orderNumber from cache
                kta = loadOrderCancelDetails(notificationId)[0];
                orderNumber = loadOrderCancelDetails(notificationId)[1];
            } else {
                // Get kta and orderNumber from intent
                kta = getIntent().getStringExtra("kta");
                orderNumber = getIntent().getStringExtra("orderNumber");
            }

            if (kta != null && orderNumber != null) {
                // Check if data exists in cache
                if (isDataInCache(kta, orderNumber)) {
                    loadDataFromCache();
                } else {
                    // Load order detail data from Firestore and save to cache
                    loadOrderDetail();
                    loadBuyerData();
                }
            } else {
                // Handle case when kta or orderNumber is null
                Toast.makeText(this, "Data tidak lengkap.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        CardView cvRincianPembatalan = findViewById(R.id.cvRincianPembatalan);
        cvRincianPembatalan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String from = getIntent().getStringExtra("from");

                // dari riwayat
                if ("riwayatdibatalkan".equals(from)) {
                    // If coming from "dikemas", return to RincianpesanandibatalkanActivity
                    Intent intent = new Intent(RincianpesanandibatalkanActivity.this, RincianPembatalanPesananActivity.class);
                    intent.putExtra("kta", kta);
                    intent.putExtra("orderNumber", orderNumber);
                    intent.putExtra("from", "rincianpesanan1");
                    startActivityForResult(intent, 100); // Request code 100

                //dari pembatalan

                } else if ("rincianpembatalan2".equals(from)){
                    Intent intent = new Intent(RincianpesanandibatalkanActivity.this, RincianPembatalanPesananActivity.class);
                    intent.putExtra("kta", kta);
                    intent.putExtra("orderNumber", orderNumber);
                    intent.putExtra("from", "dikemas");
                    startActivityForResult(intent, 100); // Request code 100
                }


            }
        });
    }

    private String[] loadOrderCancelDetails(int notificationId) {
        SharedPreferences sharedPreferences = getSharedPreferences("OrderCancel", MODE_PRIVATE);
        String kta = sharedPreferences.getString("kta_" + notificationId, null);
        String orderNumber = sharedPreferences.getString("orderNumber_" + notificationId, null);
        if (kta != null && orderNumber != null) {
            return new String[]{kta, orderNumber};
        } else {
            return new String[]{null, null};
        }
    }

    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean(LOGIN_STATUS_KEY, false);
    }

    private void loadSavedOrderCancel(int notificationId) {
        SharedPreferences sharedPreferences = getSharedPreferences("OrderCancel", MODE_PRIVATE);

        String kta = sharedPreferences.getString("kta_" + notificationId, null);
        String orderNumber = sharedPreferences.getString("orderNumber_" + notificationId, null);

        if (kta != null && orderNumber != null) {
            this.kta = kta;
            this.orderNumber = orderNumber;

            // Load order detail data
            loadOrderDetail();
            loadBuyerData();
        } else {
            Intent loginIntent = new Intent(RincianpesanandibatalkanActivity.this, BerandaActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        }
    }
    private void loadBuyerData() {
        // Pastikan bahwa variabel kta dan orderNumber diinisialisasi dan tidak kosong
        if (kta == null || kta.isEmpty() || orderNumber == null || orderNumber.isEmpty()) {
            return;
        }

        firestore.collection("orders")
                .document("dibatalkan") // Mengacu pada dokumen "dikemas"
                .collection(kta) // Mengacu pada koleksi KTA tertentu
                .document(orderNumber) // Mengacu pada dokumen orderNumber
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String namaPembeli = documentSnapshot.getString("namaPembeli");
                        String nomorPembeli = documentSnapshot.getString("nomorPembeli");
                        String alamatPembeli = documentSnapshot.getString("alamatPembeli");
                        String orderTime = documentSnapshot.getString("orderTime");
                        String cancelTime = documentSnapshot.getString("cancelTime");


                        // Tampilkan data pembeli dalam TextViews
                        tvNopesanan.setText(orderNumber);
                        tvNamaPembeli.setText(namaPembeli);
                        tvNomorPembeli.setText(nomorPembeli);
                        tvAlamatPembeli.setText(alamatPembeli);
                        tvOrderTime.setText(orderTime);
                        tvCancelTime.setText(cancelTime);

// Simpan data pembeli ke cache
                        Map<String, Object> orderData = new HashMap<>();
                        orderData.put("namaPembeli", namaPembeli);
                        orderData.put("nomorPembeli", nomorPembeli);
                        orderData.put("alamatPembeli", alamatPembeli);
                        orderData.put("orderTime", orderTime);
                        orderData.put("cancelTime", cancelTime);

                        saveDataToCache(kta, orderNumber, orderData, productListdibatalkan);
                    } else {
                    }
                })
                .addOnFailureListener(e -> {
                });
    }

    private void loadOrderDetail() {
        if (kta == null || kta.isEmpty() || orderNumber == null || orderNumber.isEmpty()) {
            return;
        }

        firestore.collection("orders")
                .document("dibatalkan")
                .collection(kta)
                .document(orderNumber)
                .collection("produk_pembelian")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productListdibatalkan.clear();

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        String name = documentSnapshot.getString("name");
                        String price = documentSnapshot.getString("price");
                        int quantity = documentSnapshot.getLong("quantity").intValue();

                        Product product = new Product(imageUrl, name, price, "");
                        product.setQuantity(quantity);
                        productListdibatalkan.add(product);
                    }

                    // Merge products by name
                    List<Product> mergedList = mergeProducts(productListdibatalkan);

                    batalkan = new RincianpesanandibatalkanAdapter(this, mergedList);
                    rvCancelDetail.setAdapter(batalkan);

                    double totalHarga = calculateTotalHarga(mergedList);
                    displayTotalHarga(totalHarga);
                    displayKataTotalHarga(totalHarga);

                    // Simpan data ke cache
                    Map<String, Object> orderData = new HashMap<>();
                    orderData.put("namaPembeli", tvNamaPembeli.getText().toString());
                    orderData.put("nomorPembeli", tvNomorPembeli.getText().toString());
                    orderData.put("alamatPembeli", tvAlamatPembeli.getText().toString());
                    orderData.put("orderTime", tvOrderTime.getText().toString());
                    orderData.put("cancelTime", tvCancelTime.getText().toString());

                    saveDataToCache(kta, orderNumber, orderData, mergedList);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat detail order.", Toast.LENGTH_SHORT).show();
                });
    }
    private boolean isDataInCache(String kta, String orderNumber) {
        SharedPreferences sharedPreferences = getSharedPreferences("OrderCache", MODE_PRIVATE);
        return sharedPreferences.contains("kta") && sharedPreferences.contains("orderNumber")
                && sharedPreferences.getString("kta", "").equals(kta)
                && sharedPreferences.getString("orderNumber", "").equals(orderNumber);
    }

    private void saveDataToCache(String kta, String orderNumber, Map<String, Object> orderData, List<Product> products) {
        SharedPreferences sharedPreferences = getSharedPreferences("OrderCache", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("kta", kta);
        editor.putString("orderNumber", orderNumber);
        editor.putString("namaPembeli", (String) orderData.get("namaPembeli"));
        editor.putString("nomorPembeli", (String) orderData.get("nomorPembeli"));
        editor.putString("alamatPembeli", (String) orderData.get("alamatPembeli"));
        editor.putString("orderTime", (String) orderData.get("orderTime"));
        editor.putString("cancelTime", (String) orderData.get("cancelTime"));

        Gson gson = new Gson();
        String productListJson = gson.toJson(products);
        editor.putString("productList", productListJson);

        editor.apply();
    }
    private void loadDataFromCache() {
        SharedPreferences sharedPreferences = getSharedPreferences("OrderCache", MODE_PRIVATE);

        kta = sharedPreferences.getString("kta", null);
        orderNumber = sharedPreferences.getString("orderNumber", null);

        if (kta != null && orderNumber != null) {
            String namaPembeli = sharedPreferences.getString("namaPembeli", null);
            String nomorPembeli = sharedPreferences.getString("nomorPembeli", null);
            String alamatPembeli = sharedPreferences.getString("alamatPembeli", null);
            String orderTime = sharedPreferences.getString("orderTime", null);
            String cancelTime = sharedPreferences.getString("cancelTime", null);

            // Tampilkan data pembeli dalam TextViews
            tvNopesanan.setText(orderNumber);
            tvNamaPembeli.setText(namaPembeli);
            tvNomorPembeli.setText(nomorPembeli);
            tvAlamatPembeli.setText(alamatPembeli);
            tvOrderTime.setText(orderTime);
            tvCancelTime.setText(cancelTime);

            String productListJson = sharedPreferences.getString("productList", null);
            if (productListJson != null) {
                Gson gson = new Gson();
                Type productListType = new TypeToken<ArrayList<Product>>() {}.getType();
                List<Product> products = gson.fromJson(productListJson, productListType);

                // Merge products by name
                List<Product> mergedList = mergeProducts(products);

                batalkan = new RincianpesanandibatalkanAdapter(this, mergedList);
                rvCancelDetail.setAdapter(batalkan);

                double totalHarga = calculateTotalHarga(mergedList);
                displayTotalHarga(totalHarga);
                displayKataTotalHarga(totalHarga);
            }
        }
    }

    private List<Product> mergeProducts(List<Product> productListdibatalkan) {
        Map<String, Product> mergedMap = new HashMap<>();

        for (Product product : productListdibatalkan) {
            String productName = product.getName();
            if (mergedMap.containsKey(productName)) {
                // Product already exists in the map, update quantity
                Product existingProduct = mergedMap.get(productName);
                existingProduct.setQuantity(existingProduct.getQuantity() + product.getQuantity());
            } else {
                // Product doesn't exist in the map, add it
                mergedMap.put(productName, product);
            }
        }

        // Convert map values to list
        return new ArrayList<>(mergedMap.values());
    }
    private double calculateTotalHarga(List<Product> products) {
        double total = 0.0;
        for (Product product : products) {
            try {
                double price = Double.parseDouble(product.getPrice().replaceAll("[^\\d.]", ""));
                total += price * product.getQuantity();
            } catch (NumberFormatException e) {
            }
        }
        return total;
    }
    private void displayTotalHarga(double totalHarga) {
        String formattedHarga = formatRupiah(totalHarga);
        tvTotalHarga.setText(formattedHarga);
    }
    private void displayKataTotalHarga(double totalHarga) {
        String formattedHarga = formatRupiah(totalHarga);
        tvKatatotalharga.setText("Mohon lakukan pembayaran sebesar " + formattedHarga + " saat menerima produk");
    }

    private String formatRupiah(double harga) {
        java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance();
        formatter.setMaximumFractionDigits(0);
        formatter.setCurrency(java.util.Currency.getInstance("IDR"));
        return formatter.format(harga);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            kta = data.getStringExtra("kta");
            orderNumber = data.getStringExtra("orderNumber");

            if (kta != null && orderNumber != null) {
                // Periksa apakah data ada di cache
                if (isDataInCache(kta, orderNumber)) {
                    loadDataFromCache();
                } else {
                    // Muat data dari Firestore dan simpan ke cache
                    loadOrderDetail();
                    loadBuyerData();
                }
            } else {
                Toast.makeText(this, "Data tidak lengkap.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onBackPressed() {
        // Retrieve the source of navigation
        String from = getIntent().getStringExtra("from");

        // Create an Intent to carry result data
        Intent resultIntent = new Intent();

        // Check the source and decide the navigation
        if ("riwayatdibatalkan".equals(from)) {
            // If coming from "dikemas", return to RincianpesanandibatalkanActivity
            resultIntent = new Intent(RincianpesanandibatalkanActivity.this, RiwayatPesananPembeliActivity.class);
            resultIntent.putExtra("kta", kta);
            resultIntent.putExtra("orderNumber", orderNumber);
            resultIntent.putExtra("selectedTab", 3);
            startActivity(resultIntent);

        } else if ("rincianpembatalan1".equals(from)) {
            // If coming from "dikemas", return to RincianpesanandibatalkanActivity
            resultIntent = new Intent(RincianpesanandibatalkanActivity.this, RiwayatPesananPembeliActivity.class);
            resultIntent.putExtra("kta", kta);
            resultIntent.putExtra("orderNumber", orderNumber);
            resultIntent.putExtra("selectedTab", 3);
            startActivity(resultIntent);

        }  else if ("rincianpembatalan2".equals(from)) {
            // If coming from "dikemas", return to RincianpesanandibatalkanActivity
            resultIntent = new Intent(RincianpesanandibatalkanActivity.this, RincianPembatalanPesananActivity.class);
            resultIntent.putExtra("kta", kta);
            resultIntent.putExtra("orderNumber", orderNumber);
            resultIntent.putExtra("from", "rincianpesanan2");
            startActivity(resultIntent);

        }

        // Finish the current activity
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                String from = getIntent().getStringExtra("from");

                // Create an Intent to carry result data
                Intent resultIntent = new Intent();

                // Check the source and decide the navigation
                if ("riwayatdibatalkan".equals(from)) {
                    // If coming from "dikemas", return to RincianpesanandibatalkanActivity
                    resultIntent = new Intent(RincianpesanandibatalkanActivity.this, FragmentRiwayatPesananPembelidibatalkanActivity.class);
                    resultIntent.putExtra("kta", kta);
                    resultIntent.putExtra("orderNumber", orderNumber);
                    startActivity(resultIntent);

                } else if ("rincianpembatalan1".equals(from)) {
                    // If coming from "dikemas", return to RincianpesanandibatalkanActivity
                    resultIntent = new Intent(RincianpesanandibatalkanActivity.this, FragmentRiwayatPesananPembelidibatalkanActivity.class);
                    resultIntent.putExtra("kta", kta);
                    resultIntent.putExtra("orderNumber", orderNumber);
                    startActivity(resultIntent);

                }  else if ("rincianpembatalan2".equals(from)) {
                    // If coming from "dikemas", return to RincianpesanandibatalkanActivity
                    resultIntent = new Intent(RincianpesanandibatalkanActivity.this, RincianPembatalanPesananActivity.class);
                    resultIntent.putExtra("kta", kta);
                    resultIntent.putExtra("orderNumber", orderNumber);
                    resultIntent.putExtra("from", "rincianpesanan2");
                    startActivity(resultIntent);

                }
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
