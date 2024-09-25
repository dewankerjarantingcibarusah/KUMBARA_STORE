package com.dkr.kumbarastore.pembeli;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dkr.kumbarastore.LoginActivity;
import com.dkr.kumbarastore.R;
import com.dkr.kumbarastore.UpdateDatabase;
import com.google.android.gms.location.LocationCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
public class RincianpesanandikirimActivity extends AppCompatActivity {

    private RecyclerView rvOrderDetail;
    private RincianpesanandikirimAdapter adapter;
    private List<Product> productList;
    private SharedPreferences sharedPreferences;
    private static final String LOGIN_STATUS_KEY = "isLoggedIn";
    private FirebaseFirestore firestore;
    private String kta;
    private String orderNumber;
    private Button btnBatalkanPesanan, btnPesananSelesai;
    private TextView tvNamaPembeli, tvNomorPembeli, tvAlamatPembeli, tvOrderTime, tvDeliveryTime, tvKatatotalharga, tvTotalHarga, tvNopesanan,tvWaktuPembayaran1, tvWaktuPembayaran, tvWaktuPengiriman;
    private Map<String, Object> buyerData = new HashMap<>();
    private Map<String, Object> orderDetailData = new HashMap<>();

    private String namaPembeli1, nomorPembeli1, alamatPembeli1, orderTime1, deliveryTime1;
    private String alasanPembatalan;

    UpdateDatabase UpdateDatabase = new UpdateDatabase();

    String waktu = UpdateDatabase.getWaktu();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rincianpesanandikirim);

        // Initialize views
        rvOrderDetail = findViewById(R.id.rvRincianpesanandikemas);
        tvNamaPembeli = findViewById(R.id.tvNamaPembeli);
        tvNomorPembeli = findViewById(R.id.tvNomorPembeli);
        tvAlamatPembeli = findViewById(R.id.tvAlamatPembeli);
        tvOrderTime = findViewById(R.id.tvKataWaktuPemesanan);
        tvWaktuPembayaran = findViewById(R.id.tvKataWaktuPembayaran);
        tvWaktuPembayaran1 = findViewById(R.id.tvWaktuPembayaran);
        tvWaktuPengiriman = findViewById(R.id.tvKataWaktuPengiriman);
        tvDeliveryTime = findViewById(R.id.tvTekspesanandisiapkan);
        tvTotalHarga = findViewById(R.id.tvHargaTotalPesanan);
        tvNopesanan = findViewById(R.id.tvKataNopesanan);
        tvKatatotalharga = findViewById(R.id.tvKatatotalpesanan);
        btnPesananSelesai=findViewById(R.id.btnPesananSelesai);
        btnBatalkanPesanan=findViewById(R.id.btnBatalkanPesanan);

        rvOrderDetail.setLayoutManager(new LinearLayoutManager(this));

        // Initialize variables
        productList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Rincian Pesanan");

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        if (!isLoggedIn()) {
            // Pengguna belum login, arahkan ke tampilan login
            Intent loginIntent = new Intent(RincianpesanandikirimActivity.this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish(); // Tutup aktivitas ini
        } else {
            // Cek apakah aktivitas dibuka dari notifikasi
            if (getIntent().hasExtra("notificationId")) {
                int notificationId = getIntent().getIntExtra("notificationId", -1);
                if (notificationId != -1) {
                    // Ambil orderNumber dan kta dari notifikasi yang disimpan
                    String[] orderDetails = loadSavedOrderDetails(notificationId);
                    if (orderDetails != null) {
                        kta = orderDetails[0];
                        orderNumber = orderDetails[1];
                        // Cek keberadaan pesanan
                        checkOrderExists(kta, orderNumber);
                    } else {
                        showOrderNotFoundMessage();
                    }
                } else {
                    showOrderNotFoundMessage();
                }
            } else if (getIntent().hasExtra("kta") && getIntent().hasExtra("orderNumber")) {
                kta = getIntent().getStringExtra("kta");
                orderNumber = getIntent().getStringExtra("orderNumber");
                // Cek keberadaan pesanan
                checkOrderExists(kta, orderNumber);
            } else {
                showOrderNotFoundMessage();
            }
        }

            btnBatalkanPesanan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showBottomSheetDialog();
                }
            });
        btnPesananSelesai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog();
            }
        });

    }

    private void checkOrderExists(String kta, String orderNumber) {
        firestore.collection("orders")
                .document("dikirim")
                .collection(kta)
                .document(orderNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        // Order exists, proceed to load the details
                        loadOrderDetail();
                        loadBuyerData();
                    } else {
                        // Order does not exist
                        showOrderNotFoundMessage();
                    }
                });
    }

    private void showOrderNotFoundMessage() {
        Toast.makeText(this, "Pesanan anda telah di selesaikan.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(RincianpesanandikirimActivity.this, BerandaActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String[] loadSavedOrderDetails(int notificationId) {
        SharedPreferences sharedPreferences = getSharedPreferences("OrderPrefs", MODE_PRIVATE);
        String kta = sharedPreferences.getString("kta_" + notificationId, null);
        String orderNumber = sharedPreferences.getString("orderNumber_" + notificationId, null);
        if (kta != null && orderNumber != null) {
            return new String[]{kta, orderNumber};
        } else {
            return null;
        }
    }

    private void showBottomSheetDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.bottom_batalkanpesanan);

        RadioGroup radioGroup = dialog.findViewById(R.id.radioGroupAlasanPembatalan);
        RadioButton radioButton1 = dialog.findViewById(R.id.radioButton1);
        RadioButton radioButton2 = dialog.findViewById(R.id.radioButton2);
        RadioButton radioButton3 = dialog.findViewById(R.id.radioButton3);
        Button btnPembatalan = dialog.findViewById(R.id.btnKonfirmasiPembatalan);
        TextInputLayout otherReasonInputLayout = dialog.findViewById(R.id.otherReasonInputLayout);
        EditText otherReasonEditText = dialog.findViewById(R.id.otherReasonEditText);

        loadMessageData("pembatalan", new OnDataLoadedCallback() {
            @Override
            public void onDataLoaded(String alasan1, String alasan2, String alasan3) {
                // Set the text of RadioButtons with the loaded data
                if (radioButton1 != null) {
                    radioButton1.setText(alasan1 != null ? alasan1 : " ");
                }
                if (radioButton2 != null) {
                    radioButton2.setText(alasan2 != null ? alasan2 : " ");
                }
                if (radioButton3 != null) {
                    radioButton3.setText(alasan3 != null ? alasan3 : " ");
                }
            }
        });

        // Set onCheckedChangeListener for RadioGroup
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButton3) {
                    // Show the TextInputLayout when RadioButton 3 is selected
                    otherReasonInputLayout.setVisibility(View.VISIBLE);
                } else {
                    // Hide the TextInputLayout when other RadioButtons are selected
                    otherReasonInputLayout.setVisibility(View.GONE);
                }
            }
        });

        btnPembatalan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFormValid(radioGroup, otherReasonInputLayout)) {
                    cancelOrder(radioGroup, otherReasonInputLayout, otherReasonEditText);
                    dialog.dismiss();

                }
            }
        });

        dialog.show();
    }
    public interface OnDataLoadedCallback {
        void onDataLoaded(String alasan1, String alasan2, String alasan3);
    }
    public interface OnDataLoadedCallback1 {


        void onDataMessage(String title, String body);
    }
    private void loadMessageData(String documentId, OnDataLoadedCallback callback) {
        firestore.collection("alasan").document(documentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String alasan1 = documentSnapshot.getString("alasan1");
                        String alasan2 = documentSnapshot.getString("alasan2");
                        String alasan3 = documentSnapshot.getString("alasan3");

                        callback.onDataLoaded(alasan1, alasan2, alasan3);
                    } else {
                        callback.onDataLoaded(null, null, null);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onDataLoaded(null, null, null);
                });
    }

    private boolean isFormValid(RadioGroup radioGroup, TextInputLayout otherReasonInputLayout) {
        int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
        if (selectedRadioButtonId == -1) {
            Toast.makeText(this, "Pilih alasan pembatalan.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedRadioButtonId == R.id.radioButton3) {
            EditText otherReasonEditText = otherReasonInputLayout.getEditText();
            if (otherReasonEditText == null || otherReasonEditText.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Isi alasan pembatalan lainnya.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    private void cancelOrder(RadioGroup radioGroup, TextInputLayout otherReasonInputLayout, EditText otherReasonEditText) {
        int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();

        // Load data from Firestore
        loadMessageData("pembatalan", new OnDataLoadedCallback() {
            @Override
            public void onDataLoaded(String alasan1, String alasan2, String alasan3) {
                // Set alasanPembatalan berdasarkan RadioButton yang dipilih
                if (selectedRadioButtonId == R.id.radioButton1) {
                    alasanPembatalan = alasan1 != null ? alasan1 : "Ingin mengubah alamat pengiriman"; // Pesan untuk radioButton1
                } else if (selectedRadioButtonId == R.id.radioButton2) {
                    alasanPembatalan = alasan2 != null ? alasan2 : "Ingin mengubah rincian dan membuat pesanan baru"; // Pesan untuk radioButton2
                } else if (selectedRadioButtonId == R.id.radioButton3) {
                    // Ambil alasan dari TextInputLayout jika radioButton3 dipilih
                    if (otherReasonEditText != null) {
                        alasanPembatalan = otherReasonEditText.getText().toString().trim();
                        if (alasanPembatalan.isEmpty()) {
                            alasanPembatalan = " ";
                        }
                    } else {
                        alasanPembatalan = " ";
                    }
                } else {
                    alasanPembatalan = " ";
                }

                // Periksa ID dari RadioButton yang dipilih dan simpan data
                if (kta != null && !kta.isEmpty() && orderNumber != null && !orderNumber.isEmpty()) {
                    java.util.Calendar calendar = java.util.Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                    String cancelTime = sdf.format(calendar.getTime());

                    // Save buyer data
                    firestore.collection("orders")
                            .document("dibatalkan")
                            .collection(kta)
                            .document(orderNumber)
                            .set(createCancelledOrderData(alasanPembatalan, waktu))
                            .addOnSuccessListener(aVoid -> {
                                // Save product data
                                for (Product product : productList) {
                                    Map<String, Object> productData = new HashMap<>();
                                    productData.put("name", product.getName());
                                    productData.put("price", product.getPrice());
                                    productData.put("quantity", product.getQuantity());
                                    productData.put("imageUrl", product.getImageUrl());

                                    firestore.collection("orders")
                                            .document("dibatalkan")
                                            .collection(kta)
                                            .document(orderNumber)
                                            .collection("produk_pembelian")
                                            .document(product.getName())
                                            .set(productData)
                                            .addOnSuccessListener(productVoid -> {
                                                updateStockAfterCancellation(product.getName(), product.getQuantity());

                                            })
                                            .addOnFailureListener(e -> {
                                            });
                                }

                                searchAndDisplayToken(kta,orderNumber);

                                deleteFromDikemas(orderNumber);

                                // Redirect to RincianpesanandibatalkanActivity
                                Intent intent = new Intent(RincianpesanandikirimActivity.this, RincianPembatalanPesananActivity.class);
                                intent.putExtra("from", "dikemas");
                                intent.putExtra("kta", kta);
                                intent.putExtra("orderNumber", orderNumber);
                                startActivity(intent);

                                finish();
                            })
                            .addOnFailureListener(e -> {
                            });
                } else {
                }
            }
        });
    }

    private Map<String, Object> createCancelledOrderData(String alasanPembatalan, String cancelTime) {
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("namaPembeli", namaPembeli1);
        orderData.put("nomorPembeli", nomorPembeli1);
        orderData.put("alamatPembeli", alamatPembeli1);
        orderData.put("orderTime", orderTime1);
        orderData.put("deliveryTime", deliveryTime1);
        orderData.put("cancelTime", cancelTime);
        orderData.put("status", "dibatalkan");
        orderData.put("diBatalkan", "pembeli");
        orderData.put("alasanPembatalan", alasanPembatalan);

        // Add buyer data
        if (buyerData != null) {
            orderData.putAll(buyerData);
        }

        // Add order details data
        if (orderDetailData != null) {
            orderData.putAll(orderDetailData);
        }

        return orderData;
    }

    private void deleteFromDikemas(String orderNumber) {
        // Menghapus detail pembeli
        firestore.collection("orders")
                .document("dikemas")
                .collection(kta)
                .document(orderNumber)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Detail pembeli berhasil dihapus

                    // Menghapus koleksi produk_pembelian
                    firestore.collection("orders")
                            .document("dikemas")
                            .collection(kta)
                            .document(orderNumber)
                            .collection("produk_pembelian")
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        document.getReference().delete();
                                    }
                                }
                            });

                    // Menghapus subkoleksi products
                    firestore.collection("orders")
                            .document("dikemas")
                            .collection(kta)
                            .document(orderNumber)
                            .collection("produk_pembelian")
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        document.getReference().delete();
                                    }
                                }
                            });

                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void updateStockAfterCancellation(String itemName, int quantityToAdd) {
        // Ambil data barang yang ada
        firestore.collection("items")
                .document(itemName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Ambil data stok dan terjual dari dokumen
                        Long currentStock = documentSnapshot.getLong("stock");
                        Long currentSold = documentSnapshot.getLong("terjual");

                        if (currentStock != null && currentSold != null) {
                            // Hitung stok baru dan jumlah terjual baru
                            int newStock = currentStock.intValue() + quantityToAdd;
                            int newSold = Math.max(0, currentSold.intValue() - quantityToAdd); // Mengurangi terjual dengan aman

                            // Update stok dan terjual di Firestore
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("stock", newStock);
                            updateData.put("terjual", newSold);

                            firestore.collection("items")
                                    .document(itemName)
                                    .update(updateData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Berhasil memperbarui stok dan terjual
                                    })
                                    .addOnFailureListener(e -> {
                                        // Gagal memperbarui stok dan terjual
                                    });
                        } else {
                            // Handle jika stok atau terjual tidak ada (null)
                        }
                    } else {
                        // Handle jika dokumen tidak ditemukan
                    }
                })
                .addOnFailureListener(e -> {
                    // Gagal mengambil data dokumen
                });
    }


    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean(LOGIN_STATUS_KEY, false);
    }


    private void loadBuyerData() {
        // Pastikan bahwa variabel kta dan orderNumber diinisialisasi dan tidak kosong
        if (kta == null || kta.isEmpty() || orderNumber == null || orderNumber.isEmpty()) {
            return;
        }

        firestore.collection("orders")
                .document("dikirim") // Mengacu pada dokumen "dikemas"
                .collection(kta) // Mengacu pada koleksi KTA tertentu
                .document(orderNumber) // Mengacu pada dokumen orderNumber
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String namaPembeli = documentSnapshot.getString("namaPembeli");
                        String nomorPembeli = documentSnapshot.getString("nomorPembeli");
                        String alamatPembeli = documentSnapshot.getString("alamatPembeli");
                        String waktuPemesanan = documentSnapshot.getString("waktupemesanan");
                        String waktuPengiriman = documentSnapshot.getString("waktupengiriman");
                        String waktuPembayaran = documentSnapshot.getString("waktupembayaran");
                        String status = documentSnapshot.getString("status");

                        String formattedDeliveryTime = extractDate(waktuPengiriman);

                        String pesanSiap = "Pesanan Anda telah dikirim pada " + formattedDeliveryTime + ". Mohon lakukan pembayaran secara tunai saat pesanan tiba.";

                        // Tampilkan data pembeli dalam TextViews
                        tvNopesanan.setText(orderNumber);
                        tvNamaPembeli.setText(namaPembeli);
                        tvNomorPembeli.setText(nomorPembeli);
                        tvAlamatPembeli.setText(alamatPembeli);
                        tvOrderTime.setText(waktuPemesanan);
                        tvDeliveryTime.setText(pesanSiap);
                        tvWaktuPengiriman.setText(waktuPengiriman);

                        // Atur visibilitas tvWaktuPembayaran berdasarkan status
                        if ("dikirim".equals(status)) {
                            tvWaktuPembayaran.setVisibility(View.GONE);
                            tvWaktuPembayaran1.setVisibility(View.GONE);
                            btnPesananSelesai.setEnabled(false);
                        } else if ("selesai oleh penjual".equals(status)) {
                            tvWaktuPembayaran.setVisibility(View.VISIBLE);
                            tvWaktuPembayaran1.setVisibility(View.VISIBLE);
                            tvWaktuPembayaran.setText(waktuPembayaran);
                            btnPesananSelesai.setEnabled(true);
                        }

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
                .document("dikirim")
                .collection(kta)
                .document(orderNumber)
                .collection("produk_pembelian")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        String name = documentSnapshot.getString("productName");
                        String price = documentSnapshot.getString("price");
                        int quantity = documentSnapshot.getLong("quantity").intValue();

                        Product product = new Product(imageUrl, name, price, "");
                        product.setQuantity(quantity);
                        productList.add(product);
                    }

                    // Merge products by name
                    List<Product> mergedList = mergeProducts(productList);

                    adapter = new RincianpesanandikirimAdapter(this, mergedList);
                    rvOrderDetail.setAdapter(adapter);

                    double totalHarga = calculateTotalHarga(mergedList);
                    displayTotalHarga(totalHarga);
                    displayKataTotalHarga(totalHarga);

                })
                .addOnFailureListener(e -> {
                });
    }

    private List<Product> mergeProducts(List<Product> productList) {
        Map<String, Product> mergedMap = new HashMap<>();

        for (Product product : productList) {
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


    // Metode untuk mengubah format ke Rupiah
    private String formatRupiah(double harga) {
        java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance();
        formatter.setMaximumFractionDigits(0);
        formatter.setCurrency(java.util.Currency.getInstance("IDR"));
        return formatter.format(harga);
    }

    private String extractDate(String datetime) {
        // Format input yang diterima dari Firestore
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        // Format output yang diinginkan
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");

        try {
            // Parsing string ke dalam objek Date
            Date date = inputFormat.parse(datetime);
            // Mengubah format Date ke string dengan format baru
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // atau kembalikan string kosong jika parsing gagal
        }
    }
    @Override
    public void onBackPressed() {
        finish();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void loadMessageData(String documentId, String ordernumber, RincianpesanandikirimActivity.OnDataLoadedCallback1 callback) {
        firestore.collection("messages").document(documentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String body = documentSnapshot.getString("body");

                        // Replace placeholder with ordernumber
                        if (body != null) {
                            body = body.replace("{ordernumber}", ordernumber);
                        }

                        callback.onDataMessage(title, body);
                    } else {
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }
    private void sendNotification(String token, String ordernumber) {
        // Load message data for sellers
        loadMessageData("Orderan Dibatalkan Penjual", ordernumber, (titlePenjual, bodyPenjual) -> {
            // Kirim Penjual Notifikasi
            FCMSender.sendNotificationPayment(RincianpesanandikirimActivity.this, token, titlePenjual, bodyPenjual);

            // Load message data for buyers
            loadMessageData("Orderan Dibatalkan Pembeli", ordernumber, (titlePembeli, bodyPembeli) -> {
                // Kirim local Notifikasi
                NotificationUtils.displayCancelLocal(RincianpesanandikirimActivity.this, titlePembeli, bodyPembeli, ordernumber, kta);
            });
        });
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
                                                }
                                            } else {
                                            }
                                        });
                            }
                        } else {
                        }
                    } else {
                    }
                });
    }
}
