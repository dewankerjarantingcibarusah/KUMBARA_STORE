package com.dkr.kumbarastore.pembeli;

import android.content.Intent;
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

import com.dkr.kumbarastore.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RincianPembatalanPesananActivity extends AppCompatActivity {
    private RecyclerView rvCancelDetail;
    private FirebaseFirestore firestore;
    private String kta;
    private String orderNumber;
    private TextView tvpesanandibatalkan, tvDibatalkanoleh, tvDiBatalkanPada, tvAlasan;
    private RincianPembatalanPesananAdapter batalkan;
    private List<Product> productListdibatalkan;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rincian_pembatalan_pesanan);

        rvCancelDetail = findViewById(R.id.rvRincianpesananpembatalan);
        tvpesanandibatalkan = findViewById(R.id.tvTekspesanandibatalkan);
        tvDibatalkanoleh = findViewById(R.id.tvKataDibatalkanoleh);
        tvDiBatalkanPada = findViewById(R.id.tvKataDiBatalkanPada);
        tvAlasan = findViewById(R.id.tvKataAlasan);


        rvCancelDetail.setLayoutManager(new LinearLayoutManager(this));

        // Initialize variables
        productListdibatalkan = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        firestore = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Rincian Pembatalan");

        if (getIntent().hasExtra("kta") && getIntent().hasExtra("orderNumber")) {
            kta = getIntent().getStringExtra("kta");
            orderNumber = getIntent().getStringExtra("orderNumber");

            // Load order detail data
            loadOrderDetail();
            loadBuyerData();
        }
        CardView cvRincianPesanan = findViewById(R.id.cvRincianPesanan);
        cvRincianPesanan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String from = getIntent().getStringExtra("from");

                // Create an Intent to carry result data
                Intent resultIntent = new Intent();

                // dari pesanan
                if ("rincianpesanan1".equals(from)) {
                    // If coming from "dikemas", return to RincianpesanandibatalkanActivity
                    resultIntent = new Intent(RincianPembatalanPesananActivity.this, RincianpesanandibatalkanActivity.class);
                    resultIntent.putExtra("kta", kta);
                    resultIntent.putExtra("orderNumber", orderNumber);
                    resultIntent.putExtra("from", "rincianpembatalan1");
                    setResult(RESULT_OK, resultIntent);
                    finish();

                    // dari dikemas
                } else if ("dikemas".equals(from)) {
                    resultIntent = new Intent(RincianPembatalanPesananActivity.this, RincianpesanandibatalkanActivity.class);
                    resultIntent.putExtra("kta", kta);
                    resultIntent.putExtra("orderNumber", orderNumber);
                    resultIntent.putExtra("from", "rincianpembatalan2");
                    setResult(RESULT_OK, resultIntent);
                    startActivity(resultIntent);

                }



            }
        });
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
                            String Dibatalkan = documentSnapshot.getString("diBatalkan");
                            String Alasan = documentSnapshot.getString("alasanPembatalan");
                            String cancelTime = documentSnapshot.getString("cancelTime");

                            String DiBatalkan = "pada " + cancelTime;

                            tvpesanandibatalkan.setText(DiBatalkan);
                            tvAlasan.setText(Alasan);
                            tvDibatalkanoleh.setText(Dibatalkan);
                            tvDiBatalkanPada.setText(cancelTime);
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

                        batalkan = new RincianPembatalanPesananAdapter(this, mergedList);
                        rvCancelDetail.setAdapter(batalkan);

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal memuat detail order.", Toast.LENGTH_SHORT).show();
                    });
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

    @Override
    public void onBackPressed() {
        // Retrieve the source of navigation
        String from = getIntent().getStringExtra("from");

        // Create an Intent to carry result data
        Intent resultIntent = new Intent();

        // dari awal di batalkan
        if ("dikemas".equals(from)) {

            // If coming from "dikemas", return to RincianpesanandibatalkanActivity
            resultIntent = new Intent(RincianPembatalanPesananActivity.this, RiwayatPesananPembeliActivity.class);
            resultIntent.putExtra("selectedTab", 3);
            resultIntent.putExtra("kta", kta);
            resultIntent.putExtra("orderNumber", orderNumber);
            startActivity(resultIntent);

        }  else if ("rincianpesanan1".equals(from)) {

            // If coming from "dikemas", return to RincianpesanandibatalkanActivity
            resultIntent = new Intent(RincianPembatalanPesananActivity.this, RincianpesanandibatalkanActivity.class);
            resultIntent.putExtra("kta", kta);
            resultIntent.putExtra("orderNumber", orderNumber);
            resultIntent.putExtra("from", "rincianpembatalan1");
            setResult(RESULT_OK, resultIntent);

        } else if ("rincianpesanan2".equals(from)) {

            // If coming from "dikemas", return to RincianpesanandibatalkanActivity
            resultIntent = new Intent(RincianPembatalanPesananActivity.this, RiwayatPesananPembeliActivity.class);
            resultIntent.putExtra("selectedTab", 3);
            resultIntent.putExtra("kta", kta);
            resultIntent.putExtra("orderNumber", orderNumber);
            startActivity(resultIntent);
        }

        // Finish the current activity
        finish();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Retrieve the source of navigation
                String from = getIntent().getStringExtra("from");

                // Create an Intent to carry result data
                Intent resultIntent = new Intent();

                // dari awal di batalkan
                if ("dikemas".equals(from)) {

                    // If coming from "dikemas", return to RincianpesanandibatalkanActivity
                    resultIntent = new Intent(RincianPembatalanPesananActivity.this, RiwayatPesananPembeliActivity.class);
                    resultIntent.putExtra("selectedTab", 0);
                    resultIntent.putExtra("kta", kta);
                    resultIntent.putExtra("orderNumber", orderNumber);
                    startActivity(resultIntent);

                }  else if ("rincianpesanan1".equals(from)) {

                    // If coming from "dikemas", return to RincianpesanandibatalkanActivity
                    resultIntent = new Intent(RincianPembatalanPesananActivity.this, RincianpesanandibatalkanActivity.class);
                    resultIntent.putExtra("kta", kta);
                    resultIntent.putExtra("orderNumber", orderNumber);
                    resultIntent.putExtra("from", "rincianpembatalan1");
                    setResult(RESULT_OK, resultIntent);

                } else if ("rincianpesanan2".equals(from)) {

                    // If coming from "dikemas", return to RincianpesanandibatalkanActivity
                    resultIntent = new Intent(RincianPembatalanPesananActivity.this, RiwayatPesananPembeliActivity.class);
                    resultIntent.putExtra("selectedTab", 3);
                    resultIntent.putExtra("kta", kta);
                    resultIntent.putExtra("orderNumber", orderNumber);
                    startActivity(resultIntent);
                }

                // Finish the current activity
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
       }