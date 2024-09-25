package com.dkr.kumbarastore.penjual;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dkr.kumbarastore.R;
import com.dkr.kumbarastore.UpdateDatabase;
import com.dkr.kumbarastore.pembeli.FCMSender;
import com.dkr.kumbarastore.pembeli.Product;
import com.dkr.kumbarastore.pembeli.RincianpesanandikemasActivity;
import com.dkr.kumbarastore.pembeli.Utils;
import com.google.android.gms.location.LocationCallback;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentRiwayatPenjualanDikemasAdapter extends RecyclerView.Adapter<FragmentRiwayatPenjualanDikemasAdapter.PenjualanViewHolder> {

    private Context context;
    private List<Penjualan> penjualanList;

    UpdateDatabase UpdateDatabase = new UpdateDatabase();

    String waktu = UpdateDatabase.getWaktu();


    private LocationCallback locationCallback;

    private static final String TIMEZONEDB_API_KEY = "H0FD3KC6UTWL"; // Ganti dengan API key kamu

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();



    public FragmentRiwayatPenjualanDikemasAdapter(Context context, List<Penjualan> penjualanList) {
        this.context = context;
        this.penjualanList = penjualanList;
    }

    @NonNull
    @Override
    public PenjualanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pesananpembelitempat, parent, false);
        return new PenjualanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PenjualanViewHolder holder, int position) {
        Penjualan penjualan = penjualanList.get(position);


        holder.cvDikemas.setVisibility(View.VISIBLE);

        String kta = penjualan.getKta();
        String orderNumber = penjualan.getOrderNumber();

        holder.cvKirimPesanan.setOnClickListener(v -> {
            showConfirmationDialog(penjualan, position);
        });



        // Set order number
        holder.txtOrderNumber.setText("No. Pesanan: " + penjualan.getOrderNumber());

        // Set total price
        holder.txtTotalPrice.setText("Total Harga: " + penjualan.getTotalPrice());

        // Set status dikemas
        holder.txtDikemas.setText("Dikemas");

        // Set nama pembeli
        holder.txtNamaPembeli.setText("Nama Pembeli: " + penjualan.getNamaPembeli());

        holder.txtKtaPembeli.setText("KTA Pembeli: " + penjualan.getKta());

        // Clear previous product views
        holder.productsLayout.removeAllViews();

        for (Product product : penjualan.getProducts()) {
            View productView = LayoutInflater.from(context).inflate(R.layout.item_pesananpembeli, holder.productsLayout, false);

            // Set product details
            ImageView ivProductImage = productView.findViewById(R.id.ivProductImage);
            TextView tvProductName = productView.findViewById(R.id.tvProductName);
            TextView txtPrice = productView.findViewById(R.id.txtPrice);
            TextView tvProductQuantity = productView.findViewById(R.id.tvProductQuantity);

            txtPrice.setText(Utils.formatRupiah1(product.getPrice()));
            tvProductName.setText(product.getName());
            tvProductQuantity.setText("x" + product.getQuantity());

            // Load product image
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(ivProductImage);

            // Add product view to productsLayout
            holder.productsLayout.addView(productView);

            holder.productsLayout1.setOnClickListener(v -> {
                Intent intent = new Intent(context, RincianpesanandikemasActivity.class);
                intent.putExtra("orderNumber", penjualan.getOrderNumber());
                intent.putExtra("kta", penjualan.getKta());

                context.startActivity(intent);
            });
        }
    }


    @Override
    public int getItemCount() {
        return penjualanList.size();
    }

    public static class PenjualanViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderNumber, txtDikemas, txtTotalPrice, txtNamaPembeli, txtKtaPembeli;
        ViewGroup productsLayout, productsLayout1;

        CardView cvDikemas,cvKirimPesanan;

        public PenjualanViewHolder(@NonNull View itemView) {
            super(itemView);
            txtOrderNumber = itemView.findViewById(R.id.txtOrderNumber);
            txtDikemas = itemView.findViewById(R.id.txtDikemas);
            txtTotalPrice = itemView.findViewById(R.id.txtTotalPrice);
            txtKtaPembeli = itemView.findViewById(R.id.txtKtaPembeli);
            txtNamaPembeli = itemView.findViewById(R.id.txtNamaPembeli);
            productsLayout1 = itemView.findViewById(R.id.productsLayout1);
            productsLayout = itemView.findViewById(R.id.productsLayout);
            cvDikemas=itemView.findViewById(R.id.cvPesananDikemas);
            cvKirimPesanan=itemView.findViewById(R.id.cvKirimPesanan);
        }
    }
    private void showConfirmationDialog(Penjualan penjualan, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Konfirmasi Kirim Pesanan");
        builder.setMessage("Apakah pesanan ini sudah siap dikirim?");

        // Tombol Kirim
        builder.setPositiveButton("Kirim", (dialog, which) -> {
            // Panggil fungsi kirim pesanan
            kirimPesanan(penjualan, position);
        });

        // Tombol Batal
        builder.setNegativeButton("Batal", (dialog, which) -> {
            // Tutup dialog tanpa melakukan apapun
            dialog.dismiss();
        });

        // Tampilkan dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void kirimPesanan(Penjualan penjualan, int position) {
        // Persiapkan data pembeli (buyerData)
        Map<String, Object> buyerData = new HashMap<>();
        buyerData.put("orderNumber", penjualan.getOrderNumber());
        buyerData.put("namaPembeli", penjualan.getNamaPembeli());
        buyerData.put("ktaPembeli", penjualan.getKta());
        buyerData.put("nomorPembeli", penjualan.getNomorPembeli());
        buyerData.put("alamatPembeli", penjualan.getAlamatPembeli());
        buyerData.put("waktupemesanan", penjualan.getWaktu());
        buyerData.put("waktupengiriman", waktu);
        buyerData.put("waktutenggatkemas", penjualan.getDeliveryTime());
        buyerData.put("status", "dikirim");

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("orders")
                .document("dikirim")
                .collection(penjualan.getKta())
                .document(penjualan.getOrderNumber())
                .set(buyerData)
                .addOnSuccessListener(aVoid -> {
                    // Persiapkan data produk (productData)
                    for (Product product : penjualan.getProducts()) {
                        Map<String, Object> productData = new HashMap<>();
                        productData.put("productName", product.getName());
                        productData.put("price", product.getPrice());
                        productData.put("quantity", product.getQuantity());
                        productData.put("imageUrl", product.getImageUrl());

                        firestore.collection("orders")
                                .document("dikirim")
                                .collection(penjualan.getKta())
                                .document(penjualan.getOrderNumber())
                                .collection("produk_pembelian")
                                .document(product.getName())
                                .set(productData)
                                .addOnSuccessListener(docRef -> {
                                    // Handle success for saving product data
                                    deleteFromDikemas(penjualan.getOrderNumber(), penjualan.getKta());
                                    searchAndDisplayToken(penjualan.getKta(), penjualan.getOrderNumber());

                                    // Hapus item dari list penjualan dan perbarui RecyclerView
                                    penjualanList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, penjualanList.size());
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }


    private void deleteFromDikemas(String orderNumber,String kta ) {
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
    private void searchAndDisplayToken(String KTA, String ordernumber) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Now search in the 'tokens' collection for this 'kta'
        db.collection("tokens")
                .document("pembeli")
                .collection(KTA)
                .whereEqualTo("role", "pembeli")
                .get()
                .addOnCompleteListener(deviceTask -> {
                    if (deviceTask.isSuccessful()) {
                        QuerySnapshot deviceQuerySnapshot = deviceTask.getResult();
                        if (deviceQuerySnapshot != null && !deviceQuerySnapshot.isEmpty()) {
                            for (DocumentSnapshot deviceDocument : deviceQuerySnapshot.getDocuments()) {
                                String token = deviceDocument.getString("token");
                                Log.d("searchAndDisplayToken", "Token ditemukan: " + token);
                                sendNotification(token, ordernumber);
                            }
                        } else {
                            Log.d("searchAndDisplayToken", "Token tidak ditemukan untuk KTA: " + KTA);

                        }
                    } else {
                        Log.e("searchAndDisplayToken", "Gagal mencari token untuk KTA: " + KTA, deviceTask.getException());

                    }
                });
    }

    private void sendNotification(String token, String ordernumber) {
        // Load message data for sellers
        loadMessageData("Orderan Dikirim", ordernumber, (titlePenjual, bodyPenjual) -> {
            // Kirim Penjual Notifikasi
            FCMSender.sendNotificationSendOrder(context, token, titlePenjual, bodyPenjual);
            Log.d("sendNotification", "Notifikasi berhasil dikirim untuk order number: " + ordernumber);

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

                        Log.d("loadMessageData", "Data pesan berhasil dimuat: title=" + title + ", body=" + body);
                        callback.onDataLoaded(title, body);
                    } else {
                        Log.e("loadMessageData", "Dokumen pesan tidak ditemukan: " + documentId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("loadMessageData", "Gagal memuat data pesan dari dokumen: " + documentId, e);
                });
    }

    interface OnDataLoadedCallback {
        void onDataLoaded(String title, String body);
    }
}
