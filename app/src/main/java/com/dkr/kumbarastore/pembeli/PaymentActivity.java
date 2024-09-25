package com.dkr.kumbarastore.pembeli;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dkr.kumbarastore.MyApp;
import com.dkr.kumbarastore.R;

import com.dkr.kumbarastore.UpdateDatabase;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PaymentActivity extends AppCompatActivity {
    private RecyclerView rvPembelian;
    private PaymentAdapter adapter;
    private TextView tvTotalHarga, tvNomorPembeli, tvNamaPembeli, tvAlamatPembeli;
    private FirebaseFirestore firestore;
    private List<Product> pembelianList;
    private SharedPreferences sharedPreferences;
    private static final String TAG = "PaymentActivity";
    private String kta;
    private boolean wasStopped = false;

    private Button btnBuatPesanan;
    private Handler ahandler;
    private Runnable arunnable;

    private Handler loaddatahandler;

    private Runnable loaddatarunnable;

    private boolean isLoadingData = false;

    private Toast lastToast;
    private boolean isToastShown = false; // Flag untuk melacak apakah Toast sudah ditampilkan
    private Handler toastHandler = new Handler();

    UpdateDatabase UpdateDatabase = new UpdateDatabase();

    String waktu = UpdateDatabase.getWaktu();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        kta = sharedPreferences.getString("kta", "");
        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvPembelian = findViewById(R.id.rvPembelian);
        tvTotalHarga = findViewById(R.id.tvPembayaran);
        tvNamaPembeli =findViewById(R.id.tvNamaPembeli);
        tvNomorPembeli = findViewById(R.id.tvNomorPembeli);
        tvAlamatPembeli =findViewById(R.id.tvAlamatPembeli);

        rvPembelian.setLayoutManager(new LinearLayoutManager(this));

        pembelianList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();



        // Set up ActionBar with back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Pembayaran");

        btnBuatPesanan = findViewById(R.id.btnBuatPesanan);
        btnBuatPesanan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    buatPesanan();



            }
        });

        loadPembelianData();
        loadUserData(kta);

    }

    private void searchAndDisplayToken(String KTA, String ordernumber) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // First, search for the 'penjual' in the 'users' collection
        db.collection("users")
                .whereEqualTo("posisi", "penjual")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (DocumentSnapshot userDocument : querySnapshot.getDocuments()) {
                                String kta = userDocument.getString("kta"); // Assuming 'kta' is the document ID
                                String role = userDocument.getString("posisi"); // Get the 'role' field

                                // Now search in the 'tokens' collection for this 'kta'
                                db.collection("tokens")
                                        .document(role)
                                        .collection(kta)
                                        .whereEqualTo("role", "penjual")
                                        .get()
                                        .addOnCompleteListener(deviceTask -> {
                                            if (deviceTask.isSuccessful()) {
                                                QuerySnapshot deviceQuerySnapshot = deviceTask.getResult();
                                                if (deviceQuerySnapshot != null && !deviceQuerySnapshot.isEmpty()) {
                                                    for (DocumentSnapshot deviceDocument : deviceQuerySnapshot.getDocuments()) {
                                                        String token = deviceDocument.getString("token");
                                                        sendNotification(token, ordernumber);
                                                    }
                                                } else {
                                                    sendNotification(null,ordernumber);
                                                }
                                            } else {
                                                sendNotification(null,ordernumber);
                                            }
                                        });
                            }
                        } else {
                            sendNotification(null,ordernumber);
                        }
                    } else {
                        sendNotification(null,ordernumber);
                    }
                });
    }

    private void loadMessageData(String documentId, String ordernumber, OnDataLoadedCallback callback) {
        firestore.collection("messages").document(documentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String body = documentSnapshot.getString("body");

                        // Replace placeholder with ordernumber
                        if (body != null) {
                            body = body.replace("{ordernumber}", ordernumber);
                        }

                        callback.onDataLoaded(title, body);
                    } else {
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    interface OnDataLoadedCallback {
        void onDataLoaded(String title, String body);
    }


    private void sendNotification(String token, String ordernumber) {
        // Load message data for sellers
        loadMessageData("Orderan Baru Penjual", ordernumber, (titlePenjual, bodyPenjual) -> {
            // Kirim Penjual Notifikasi
            FCMSender.sendNotificationPayment(PaymentActivity.this, token, titlePenjual, bodyPenjual);

            // Load message data for buyers
            loadMessageData("Orderan Baru Pembeli", ordernumber, (titlePembeli, bodyPembeli) -> {
                // Kirim local Notifikasi
                NotificationUtils.displayPaymentLocal(PaymentActivity.this, titlePembeli, bodyPembeli, ordernumber, kta);
            });
        });
    }


    private void buatPesanan() {
        String namaPembeli = tvNamaPembeli.getText().toString();
        String nomorPembeli = tvNomorPembeli.getText().toString();
        String alamatPembeli = tvAlamatPembeli.getText().toString();

        if (pembelianList.isEmpty() || namaPembeli.isEmpty() || nomorPembeli.isEmpty() || alamatPembeli.isEmpty()) {
            return;
        }

        // Generate a unique order number
        String orderNumber = generateOrderNumber();


        // Get current date and time
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm");
        String orderTime = sdf.format(calendar.getTime());

        // Get date three days after order time
        calendar.add(java.util.Calendar.DATE, 3);
        String deliveryTime = new java.text.SimpleDateFormat("dd-MM-yyyy").format(calendar.getTime());

        // Buyer details
        Map<String, Object> buyerData = new HashMap<>();
        buyerData.put("namaPembeli", namaPembeli);
        buyerData.put("nomorPembeli", nomorPembeli);
        buyerData.put("alamatPembeli", alamatPembeli);
        buyerData.put("orderNumber", orderNumber);
        buyerData.put("orderTime", waktu);
        buyerData.put("deliveryTime", deliveryTime);
        buyerData.put("status", "dikemas");

        firestore.collection("orders")
                .document("dikemas")
                .collection(kta)
                .document(orderNumber)
                .set(buyerData)
                .addOnSuccessListener(aVoid -> {
                    // Handle success for saving buyer data

                    // Save each purchased product in the "produk_pembelian" subcollection
                    for (Product product : pembelianList) {
                        Map<String, Object> productData = new HashMap<>();
                        productData.put("name", product.getName());
                        productData.put("price", product.getPrice());
                        productData.put("quantity", product.getQuantity());
                        productData.put("imageUrl", product.getImageUrl());

                        firestore.collection("orders")
                                .document("dikemas")
                                .collection(kta)
                                .document(orderNumber)
                                .collection("produk_pembelian")
                                .document(product.getName())
                                .set(productData)
                                .addOnSuccessListener(docRef -> {
                                    // Handle success for saving product data
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure
                                });
                        // Asumsikan product adalah objek dari kelas Product yang memiliki getter untuk kta, name, dan quantity
                        updateCartQuantityInCart(kta, product.getName(), product.getQuantity());
                        deleteItemFromCart(kta, product.getName());
                        // Reduce quantity in the cart based on product name
                        updateCartQuantityByName(product.getName(), product.getQuantity());
                        updateTerjualQuantityByName(product.getName(), product.getQuantity());
                    }

                    searchAndDisplayToken(kta,orderNumber);

                    // Optionally, clear the cart and navigate to another activity
                    deleteAllItemsByKTA(kta);

                    Intent intent = new Intent(PaymentActivity.this, MenunggukonfirmasiActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("kta", kta); // kta adalah data yang ingin Anda kirim
                    intent.putExtra("orderNumber", orderNumber); // orderNumber adalah nomor pesanan yang telah dibuat
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }
    private void updateCartQuantityInCart(String kta, String productName, int quantityOrdered) {
        // Mengambil dokumen berdasarkan nama barang (productName) dalam koleksi "carts"
        firestore.collection("carts")
                .document(kta)
                .collection("items")
                .whereEqualTo("name", productName) // Query untuk mencari berdasarkan nama barang
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Long currentQuantity = document.getLong("stock");
                        if (currentQuantity != null && currentQuantity > 0) {
                            // Mengurangi quantity dengan aman
                            int newQuantity = Math.max(0, currentQuantity.intValue() - quantityOrdered);

                            // Memperbarui quantity di dokumen
                            firestore.collection("carts")
                                    .document(kta)
                                    .collection("items")
                                    .document(document.getId())
                                    .update("stock", newQuantity)
                                    .addOnSuccessListener(aVoid -> {
                                        // Handle success
                                        Log.d("Firestore", "Quantity updated successfully in cart for product: " + productName);

                                        // Mengecek apakah quantity menjadi 1 setelah pengurangan
                                        if (newQuantity <= 1) {
                                            // Menghapus dokumen dari koleksi jika quantity 1 atau kurang
                                            firestore.collection("carts")
                                                    .document(kta)
                                                    .collection("items")
                                                    .document(document.getId())
                                                    .delete()
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        })
                                                    .addOnFailureListener(e -> {
                                                      });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                       });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                     });
    }

    private void updateCartQuantityByName(String productName, int quantityOrdered) {
        // Mengambil ID dokumen berdasarkan nama barang (productName)
        firestore.collection("items")
                .whereEqualTo("imageName", productName) // Query untuk mencari berdasarkan nama barang
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Long currentQuantity = document.getLong("stock");
                        if (currentQuantity != null && currentQuantity > 0) {
                            // Mengurangi quantity dengan aman
                            int newQuantity = Math.max(0, currentQuantity.intValue() - quantityOrdered);

                            // Memperbarui quantity di dokumen
                            firestore.collection("items")
                                    .document(document.getId())
                                    .update("stock", newQuantity)
                                    .addOnSuccessListener(aVoid -> {
                                         })
                                    .addOnFailureListener(e -> {
                                     });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                   });
    }

    private void updateTerjualQuantityByName(String productName, int quantityOrdered) {
        // Mengambil ID dokumen berdasarkan nama barang (productName)
        firestore.collection("items")
                .whereEqualTo("imageName", productName) // Query untuk mencari berdasarkan nama barang
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Long currentSold = document.getLong("terjual");
                        if (currentSold != null) {
                            // Menambahkan quantityOrdered ke jumlah terjual saat ini
                            int newSold = currentSold.intValue() + quantityOrdered;

                            // Memperbarui jumlah terjual di dokumen
                            firestore.collection("items")
                                    .document(document.getId())
                                    .update("terjual", newSold)
                                    .addOnSuccessListener(aVoid -> {
                                        // Sukses memperbarui jumlah terjual
                                    })
                                    .addOnFailureListener(e -> {
                                        // Gagal memperbarui jumlah terjual
                                    });
                        } else {
                            // Jika tidak ada nilai terjual, set ke quantityOrdered
                            firestore.collection("items")
                                    .document(document.getId())
                                    .update("terjual", quantityOrdered)
                                    .addOnSuccessListener(aVoid -> {
                                        // Sukses memperbarui jumlah terjual
                                    })
                                    .addOnFailureListener(e -> {
                                        // Gagal memperbarui jumlah terjual
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Gagal mengambil dokumen berdasarkan nama barang
                });
    }


    private void deleteItemFromCart(String kta, String productName) {
        firestore.collection("carts")
                .document(kta)
                .collection("items")
                .document(productName) // Menggunakan nama produk sebagai ID dokumen
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Optional: Handle success if needed
                })
                .addOnFailureListener(e -> {
                    });
    }

    private String generateOrderNumber() {
        // Get current date in dd-MM format
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("ddMM");
        String dateStr = sdf.format(new java.util.Date());

        // Generate random alphanumeric string
        String alphanumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder randomStr = new StringBuilder();
        Random random = new Random();
        while (randomStr.length() < 6) { // Adjust length as needed
            int index = random.nextInt(alphanumeric.length());
            randomStr.append(alphanumeric.charAt(index));
        }

        // Concatenate date string and random alphanumeric string
        return dateStr + randomStr.toString();
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


    private void loadPembelianData() {
        if (kta.isEmpty()) {
            return;
        }

        firestore.collection("pembelian").document(kta)
                .collection("items")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        pembelianList.clear(); // Kosongkan daftar sebelum menambahkan data baru

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String imageUrl = documentSnapshot.getString("imageUrl");
                            String name = documentSnapshot.getString("name");
                            String price = documentSnapshot.getString("price");
                            String id = documentSnapshot.getId();
                            int quantity = documentSnapshot.getLong("quantity").intValue();

                            Product product = new Product(imageUrl, name, price, id);
                            product.setQuantity(quantity); // Set quantity dari Firestore
                            pembelianList.add(product);
                        }

                        // Gabungkan produk berdasarkan nama
                        List<Product> mergedList = mergeProducts(pembelianList);

                        loadPembelianData();
                        adapter = new PaymentAdapter(PaymentActivity.this, mergedList);
                        rvPembelian.setAdapter(adapter);

                        double totalHarga = calculateTotalHarga(mergedList);
                        displayTotalHarga(totalHarga);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                            }

                });
    }



    private List<Product> mergeProducts(List<Product> productList) {
        Map<String, Product> mergedMap = new HashMap<>();
        for (Product product : productList) {
            if (mergedMap.containsKey(product.getName())) {
                Product existingProduct = mergedMap.get(product.getName());
                existingProduct.setQuantity(existingProduct.getQuantity() + product.getQuantity());
            } else {
                mergedMap.put(product.getName(), product);
            }
        }
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


    // Metode untuk mengubah format ke Rupiah
    private String formatRupiah(double harga) {
        java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance();
        formatter.setMaximumFractionDigits(0);
        formatter.setCurrency(java.util.Currency.getInstance("IDR"));
        return formatter.format(harga);
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



    @Override
    public void onBackPressed() {

        Intent intent = new Intent(this, KeranjangActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Set wasStopped ke true ketika PaymentActivity dihentikan
        wasStopped = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Memeriksa apakah PaymentActivity sebelumnya dihentikan
        if (wasStopped) {
            String kta = sharedPreferences.getString("kta", "");
            if (!kta.isEmpty()) {
            }

            Intent intent = new Intent(this, KeranjangActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        // Mulai runnable kembali saat activity aktif

        wasStopped = false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                String kta = sharedPreferences.getString("kta", "");
                if (!kta.isEmpty()) {
                }
                Intent intent = new Intent(this, KeranjangActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



}