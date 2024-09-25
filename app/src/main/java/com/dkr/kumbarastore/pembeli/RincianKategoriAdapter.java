package com.dkr.kumbarastore.pembeli;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dkr.kumbarastore.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RincianKategoriAdapter extends RecyclerView.Adapter<RincianKategoriAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private FirebaseFirestore firestore;

    private SharedPreferences sharedPreferences;
    public RincianKategoriAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.firestore = FirebaseFirestore.getInstance();
        this.sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rincian_kategori, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvProductName.setText(product.getName());
        holder.tvProductStok.setText("Stok: " + product.getStock());
        holder.tvProductPrice.setText(formatRupiah(product.getPrice()));
        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.ivProductImage);

        holder.ivProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePopup(product.getImageUrl());
            }
        });

        holder.ivAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToCart(product);
            }
        });
    }

    private void addToCart(Product product) {
        String kta = sharedPreferences.getString("kta", "");
        if (!kta.isEmpty()) {
            firestore.collection("carts")
                    .document(kta)
                    .collection("items")
                    .document(product.getName())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // If the product is already in the cart, update the quantity
                            int existingQuantity = documentSnapshot.getLong("quantity").intValue();
                            int newQuantity = existingQuantity + 1; // Increment quantity by 1

                            // Update the quantity in Firestore
                            firestore.collection("carts")
                                    .document(kta)
                                    .collection("items")
                                    .document(product.getName())
                                    .update("quantity", newQuantity)
                                    .addOnSuccessListener(aVoid -> showToast("Produk berhasil ditambahkan ke keranjang"))
                                    .addOnFailureListener(e -> Toast.makeText(context, "Gagal memperbarui jumlah barang di keranjang", Toast.LENGTH_SHORT).show());
                        } else {
                            // If the product is not in the cart, add it as a new entry
                            product.setQuantity(1); // Set initial quantity to 1
                            firestore.collection("carts")
                                    .document(kta)
                                    .collection("items")
                                    .document(product.getName())
                                    .set(product)
                                    .addOnSuccessListener(aVoid -> showToast("Produk berhasil ditambahkan ke keranjang"))
                                    .addOnFailureListener(e -> Toast.makeText(context, "Gagal menambahkan produk ke keranjang", Toast.LENGTH_SHORT).show());
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Gagal memeriksa keranjang", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(context, "Silahkan login terlebih dahulu", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage, ivAddToCart;
        TextView tvProductName, tvProductPrice, tvProductStok;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            ivAddToCart = itemView.findViewById(R.id.ivAddToCart);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductStok = itemView.findViewById(R.id.tvProductStok); // tambahkan inisialisasi TextView untuk stok
        }
    }


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

    private void showImagePopup(String imageUrl) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_popup, null);
        PhotoView photoView = view.findViewById(R.id.photoView);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        Glide.with(context).load(imageUrl).into(photoView);

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(view);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Gambar Produk");

        toolbar.setNavigationIcon(R.drawable.ic_kembali);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

        Button btnDownload = view.findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStoragePermission(imageUrl, progressBar);
            }
        });
    }

    private void checkStoragePermission(String imageUrl, ProgressBar progressBar) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        } else {
            downloadImage(imageUrl, progressBar);
        }
    }

    private void downloadImage(String imageUrl, ProgressBar progressBar) {
        String fileName = Uri.parse(imageUrl).getLastPathSegment();
        String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
        Uri uri = Uri.parse("file://" + destination + fileName);

        android.app.DownloadManager downloadManager = (android.app.DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        android.app.DownloadManager.Request request = new android.app.DownloadManager.Request(Uri.parse(imageUrl))
                .setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(uri);

        downloadManager.enqueue(request);

        progressBar.setVisibility(View.VISIBLE);

        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                progressBar.setVisibility(View.GONE);
                showToast("File Telah Didownload");
            }
        };

        context.registerReceiver(onComplete, new android.content.IntentFilter(android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
