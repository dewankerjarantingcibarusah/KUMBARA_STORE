package com.dkr.kumbarastore.pembeli;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dkr.kumbarastore.LoginActivity;
import com.dkr.kumbarastore.MyApp;
import com.dkr.kumbarastore.R;
import com.dkr.kumbarastore.SignupActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class KeranjangActivity extends AppCompatActivity {
    private RecyclerView rvKeranjang;
    private List<Product> keranjangList;
    private FirebaseFirestore firestore;
    private KeranjangAdapter adapter;
    private String kta;
    private static final String LOGIN_STATUS_KEY = "isLoggedIn";
    private SharedPreferences sharedPreferences;
    private AlertDialog alertDialog;
    private Button btnbeli;

    private SwipeRefreshLayout swipeRefreshLayout;

    private TextView tvTotalHarga;


    private CheckBox chkSelectAll;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keranjang);
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chkSelectAll = findViewById(R.id.chkSelectAll);
        chkSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    adapter.selectAllItems(isChecked);
            }
        });

        rvKeranjang = findViewById(R.id.rvKeranjang);
        rvKeranjang.setLayoutManager(new LinearLayoutManager(this));

        keranjangList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        kta = sharedPreferences.getString("kta", "");

        tvTotalHarga = findViewById(R.id.tvPembayaran); // Inisialisasi TextView


        // Set up ActionBar with back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Keranjang Belanja");

        btnbeli = findViewById(R.id.btnBuatPesanan);
        btnbeli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    List<Product> selectedProducts = adapter.getSelectedProducts();
                    if (selectedProducts.isEmpty()) {
                        Toast.makeText(KeranjangActivity.this, "Pilih setidaknya satu produk untuk membeli", Toast.LENGTH_SHORT).show();
                    } else {

                        validateStockBeforePurchase(selectedProducts);

                    }

            }
        });

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                    loadKeranjangData(); // Muat ulang data saat refresh
                    swipeRefreshLayout.setRefreshing(false);


            }
        });

        loadKeranjangData();
    }
    private void validateStockBeforePurchase(List<Product> selectedProducts) {
        for (Product product : selectedProducts) {
            if (product.getQuantity() > product.getStock()) {
                Toast.makeText(KeranjangActivity.this, "Stock untuk " + product.getName() + " tidak mencukupi", Toast.LENGTH_SHORT).show();
                return; // Exit if any product has insufficient stock
            }
        }

        // If all selected products have sufficient stock, proceed with the purchase
        if (!kta.isEmpty()) {

            for (Product product : selectedProducts) {
                saveToPembelian(kta, product);
            }
            Intent intent = new Intent(KeranjangActivity.this, PaymentActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
    private void saveToPembelian(String kta, Product product) {
        firestore.collection("pembelian")
                .document(kta)
                .collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Hapus semua dokumen dalam subkoleksi "items"
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }

                    // Setelah semua dokumen dihapus, simpan produk baru
                    Map<String, Object> productMap = product.toMap();
                    productMap.put("quantity", product.getQuantity());

                    firestore.collection("pembelian")
                            .document(kta)
                            .collection("items")
                            .document(product.getName())
                            .set(productMap)
                            .addOnSuccessListener(aVoid -> {
                                // Produk berhasil disimpan
                            })
                            .addOnFailureListener(e -> {
                                // Gagal menyimpan produk
                            });
                })
                .addOnFailureListener(e -> {
                    // Gagal mendapatkan dokumen dalam subkoleksi "items"
                });
    }


    private void removeProductFromCart(Product product) {
        if (!kta.isEmpty()) {
            firestore.collection("carts").document(kta)
                    .collection("items")
                    .document(product.getName()) // Menggunakan nama produk sebagai ID dokumen
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        keranjangList.remove(product);
                        adapter.notifyDataSetChanged();
                        adapter.removeItem(product);
                    })
                    .addOnFailureListener(e -> {
                    });
        } else {
        }
    }

    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean(LOGIN_STATUS_KEY, false);
    }

    private void showLoginAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Anda belum login");
        builder.setMessage("Silakan login atau register untuk melanjutkan.");
        builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent loginIntent = new Intent(KeranjangActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                dialog.dismiss();
                finish();
            }
        });

        builder.setNegativeButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent registerIntent = new Intent(KeranjangActivity.this, SignupActivity.class);
                startActivity(registerIntent);
                dialog.dismiss();
                finish();
            }
        });

        builder.setCancelable(true);
        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Intent intent = new Intent(KeranjangActivity.this, BerandaActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        alertDialog.show();
    }

    private void deleteAllItemsByKTA(String kta) {
        if (!kta.isEmpty()) {
            // Ambil semua dokumen dalam koleksi "items" di bawah dokumen KTA
            firestore.collection("pembelian").document(kta)
                    .collection("items")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            // Loop untuk menghapus setiap dokumen
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                // Hapus dokumen dalam "items"
                                firestore.collection("pembelian").document(kta)
                                        .collection("items")
                                        .document(documentSnapshot.getId())
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                            }
                                        });
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
    private void loadKeranjangData() {
        swipeRefreshLayout.setRefreshing(true);
        firestore.collection("carts")
                .document(kta)
                .collection("items")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        keranjangList.clear(); // Kosongkan daftar sebelum menambahkan data baru
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            // Ambil data dari setiap dokumen
                            String imageUrl = documentSnapshot.getString("imageUrl");
                            String name = documentSnapshot.getString("name");
                            String price = documentSnapshot.getString("price");
                            int stock = documentSnapshot.getLong("stock").intValue();
                            int terjual = documentSnapshot.getLong("terjual").intValue();
                            String id = documentSnapshot.getId();
                            int quantity = documentSnapshot.getLong("quantity").intValue();

                            // Buat objek Product dari data Firestore
                            Product product = new Product(imageUrl, name, price, stock,terjual, id);
                            product.setQuantity(quantity); // Set quantity dari Firestore
                            keranjangList.add(product); // Tambahkan ke daftar keranjang
                        }

                        // Gabungkan produk berdasarkan nama
                        List<Product> mergedList = mergeProducts(keranjangList);

                        // Inisialisasi adapter dan sambungkan ke RecyclerView
                        adapter = new KeranjangAdapter(KeranjangActivity.this, mergedList, new KeranjangAdapter.OnCartUpdatedListener() {
                            @Override
                            public void onCartUpdated() {
                                loadKeranjangData(); // Panggil kembali loadKeranjangData() jika ada perubahan di adapter
                            }
                        }, tvTotalHarga); // tvTotalHarga merupakan TextView untuk menampilkan total harga
                        rvKeranjang.setAdapter(adapter); // Set adapter ke RecyclerView
                        swipeRefreshLayout.setRefreshing(false); // Hentikan animasi refresh
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });


    }


    // Metode untuk mendapatkan jumlah item yang sama berdasarkan nama produk
    public int getProductQuantity(String productName, List<Product> productList) {
        int quantity = 0;
        for (Product product : productList) {
            if (product.getName().equals(productName)) {
                quantity++;
            }
        }
        return quantity;
    }


    private List<Product> mergeProducts(List<Product> productList) {
        Map<String, Product> mergedMap = new HashMap<>();
        for (Product product : productList) {
            String productName = product.getName();
            if (mergedMap.containsKey(productName)) {
                Product existingProduct = mergedMap.get(productName);
                existingProduct.setQuantity(existingProduct.getQuantity() + 1);
            } else {
                // Buat copy dari produk untuk dimasukkan ke dalam map
                Product newProduct = product.copy();
                mergedMap.put(productName, newProduct);
            }
        }
        return new ArrayList<>(mergedMap.values());
    }



    @Override
    public void onBackPressed() {
        // Retrieve the source of navigation
        String from = getIntent().getStringExtra("from");

        Intent intent;
        if ("Kategori".equals(from)) {
            // If coming from KategoriActivity, return to KategoriActivity
            intent = new Intent(KeranjangActivity.this, KategoriActivity.class);
        } else {
            // If coming from BerandaActivity, return to BerandaActivity
            intent = new Intent(KeranjangActivity.this, BerandaActivity.class);
        }

        // Set flags to clear the top of the activity stack
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }




}