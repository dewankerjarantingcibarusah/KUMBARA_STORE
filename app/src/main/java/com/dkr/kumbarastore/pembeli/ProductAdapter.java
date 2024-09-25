package com.dkr.kumbarastore.pembeli;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dkr.kumbarastore.NetworkUtils;
import com.dkr.kumbarastore.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private FirebaseFirestore firestore;
    private SharedPreferences sharedPreferences;

    private static final int REQUEST_STORAGE_PERMISSION = 1;

    private boolean toastShown = false;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.firestore = FirebaseFirestore.getInstance();
        this.sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
    }
    public void updateList(List<Product> newList) {
        productList.clear();
        productList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);


            holder.ivAddToCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addToCart(product);
                }
            });


        holder.tvProductName.setText(product.getName());

        // Format the price to Rupiah
        holder.tvProductPrice.setText(formatRupiah(product.getPrice()));

        holder.tvProductStok.setText("Stock: " + product.getStock());

        holder.tvProdukTerjual.setText("Terjual: " + product.getTerjual()   );

        Glide.with(context).load(product.getImageUrl()).into(holder.ivProductImage);
        // Tambahkan listener untuk menampilkan bottom sheet dialog saat gambar diklik
        holder.ivProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePopup(product.getImageUrl()); // Mengirim URL gambar ke method showImagePopup
            }
        });

        // Cek konektivitas internet dan atur status tombol

    }
    private void showToastBottom(String message) {
        if (!toastShown) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            toastShown = true;

            // Setel elapsedRealtime() sebagai waktu awal
            long toastStartTime = SystemClock.elapsedRealtime();

            // Gunakan Handler untuk membatalkan Toast setelah 1 detik
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    long elapsedTime = SystemClock.elapsedRealtime() - toastStartTime;
                    if (elapsedTime >= 1000) { // Jika waktu yang lewat lebih dari 1 detik
                        toastShown = false;
                    }
                }
            }, 1000);
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    private void addToCart(Product product) {

        // Check network availability using NetworkUtils
        NetworkUtils.checkNetworkQuality(isPingSuccessful -> {
            if (!isPingSuccessful) {
                View rootView = ((Activity) context).findViewById(android.R.id.content);

                showtoastTop(rootView,"Tidak ada koneksi internet");
                return; // Don't proceed if there's no network
            }

            // Proceed with Firestore operations
            String kta = sharedPreferences.getString("kta", "");
            if (!kta.isEmpty()) {
                firestore.collection("carts")
                        .document(kta)
                        .collection("items")
                        .document(product.getName())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                int existingQuantity = documentSnapshot.getLong("quantity").intValue();
                                int newQuantity = existingQuantity + 1;

                                firestore.collection("carts")
                                        .document(kta)
                                        .collection("items")
                                        .document(product.getName())
                                        .update("quantity", newQuantity)
                                        .addOnSuccessListener(aVoid -> showToastBottom("Produk berhasil ditambahkan ke keranjang"))
                                        .addOnFailureListener(e -> {
                                            // Handle update failure
                                            showToastBottom("Gagal menambahkan produk ke keranjang");
                                        });
                            } else {
                                product.setQuantity(1);
                                firestore.collection("carts")
                                        .document(kta)
                                        .collection("items")
                                        .document(product.getName())
                                        .set(product)
                                        .addOnSuccessListener(aVoid -> showToastBottom("Produk berhasil ditambahkan ke keranjang"))
                                        .addOnFailureListener(e -> {
                                            // Handle set failure
                                            showToastBottom("Gagal menambahkan produk ke keranjang");
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Handle failure to get document
                            showToastBottom("Gagal memeriksa produk di keranjang");
                        });
            } else {
                Toast.makeText(context, "Silahkan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage, ivAddToCart;
        TextView tvProductName, tvProductPrice, tvProductStok, tvProdukTerjual;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            ivAddToCart = itemView.findViewById(R.id.ivAddToCart);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductStok = itemView.findViewById(R.id.tvProductStok);
            tvProdukTerjual = itemView.findViewById(R.id.tvProductTerjual);
        }
    }
    // Method to format price to Rupiah
    private String formatRupiah(String harga) {
        try {
            double hargaDouble = Double.parseDouble(harga.replaceAll("[^\\d.]", ""));
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            formatter.setMaximumFractionDigits(0); // No decimal points
            return formatter.format(hargaDouble);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return harga; // Return original string if format fails
        }
    }
    // Method untuk menampilkan bottom sheet dialog dengan gambar dari Firestore
    private void showImagePopup(String imageUrl) {
        // Inflate layout untuk bottom sheet dialog
        View view = LayoutInflater.from(context).inflate(R.layout.image_popup, null);

        // Inisialisasi PhotoView
        PhotoView photoView = view.findViewById(R.id.photoView);
        Glide.with(context).load(imageUrl).into(photoView); // Menggunakan Glide untuk memuat gambar ke PhotoView
        ProgressBar progressBar = view.findViewById(R.id.progressBar); // Deklarasi progressBar

        // Buat dan tampilkan bottom sheet dialog
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(view);

        // Inisialisasi Toolbar dari layout
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Gambar Produk"); // Set judul toolbar


        // Aktifkan tombol kembali di toolbar
        toolbar.setNavigationIcon(R.drawable.ic_kembali);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Tutup dialog saat tombol kembali diklik
            }
        });

        Button btnDownload = dialog.findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStoragePermission(imageUrl, progressBar); // Panggil checkStoragePermission sebelum download

                // Handle the click event for "Hapus dari Favorit" button
            }
        });
    //
        // Tampilkan bottom sheet dialog
        dialog.show();
    }
    private void checkStoragePermission(String imageUrl, ProgressBar progressBar) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Jika izin belum diberikan, minta izin kepada pengguna
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            // Jika izin sudah diberikan, mulai proses download
            downloadImage(imageUrl, progressBar);
        }
    }

    private void downloadImage(String imageUrl, ProgressBar progressBar) {
        NetworkUtils.checkNetworkQuality(isPingSuccessful -> {
            if (!isPingSuccessful) {

                View rootView = ((Activity) context).findViewById(android.R.id.content);

                showtoastTop(rootView,"Tidak ada koneksi internet");
                return; // Don't proceed if there's no network
            }

            String fileName = Uri.parse(imageUrl).getLastPathSegment();
            String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
            Uri uri = Uri.parse("file://" + destination + fileName);

            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl))
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationUri(uri);

            downloadManager.enqueue(request);

            progressBar.setVisibility(View.VISIBLE);

            BroadcastReceiver onComplete = new BroadcastReceiver() {
                @Override
                public void onReceive(Context ctxt, Intent intent) {
                    progressBar.setVisibility(View.GONE);
                    showToastBottom("File Telah Didownload");
                }
            };

            context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        });
    }
    public void showtoastTop(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);

        // Mendapatkan view dari Snackbar
        View snackbarView = snackbar.getView();

        // Mengubah posisi Snackbar menjadi di atas
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
        params.width = FrameLayout.LayoutParams.WRAP_CONTENT; // Lebar sesuai konten
        params.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL; // Set Snackbar muncul di atas
        snackbarView.setLayoutParams(params);

        // Menampilkan Snackbar
        snackbar.show();
    }


}
