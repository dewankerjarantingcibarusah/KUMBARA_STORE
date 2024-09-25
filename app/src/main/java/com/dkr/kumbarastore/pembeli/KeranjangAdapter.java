    package com.dkr.kumbarastore.pembeli;

    import android.content.Context;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.CheckBox;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;

    import com.bumptech.glide.Glide;
    import com.dkr.kumbarastore.MyApp;
    import com.dkr.kumbarastore.R;
    import com.google.android.gms.tasks.OnFailureListener;
    import com.google.android.gms.tasks.OnSuccessListener;
    import com.google.firebase.firestore.FirebaseFirestore;

    import java.util.ArrayList;
    import java.util.List;

    public class KeranjangAdapter extends RecyclerView.Adapter<KeranjangAdapter.KeranjangViewHolder> {
        // Deklarasi lainnya
        private TextView tvTotalHarga;
        private Context context;
        private List<Product> keranjangList;
        private List<Product> selectedProducts;
        private FirebaseFirestore firestore;
        private OnCartUpdatedListener onCartUpdatedListener;

        public KeranjangAdapter(Context context, List<Product> keranjangList, OnCartUpdatedListener onCartUpdatedListener, TextView tvTotalHarga) {
            this.context = context;
            this.keranjangList = keranjangList;
            this.selectedProducts = new ArrayList<>();
            this.firestore = FirebaseFirestore.getInstance();
            this.onCartUpdatedListener = onCartUpdatedListener;
            this.tvTotalHarga = tvTotalHarga;
        }

        @NonNull
        @Override
        public KeranjangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_keranjang, parent, false);
            return new KeranjangViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull KeranjangViewHolder holder, int position) {
            Product product = keranjangList.get(position);

            holder.tvProductName.setText(product.getName());
            holder.tvProductQuantity.setVisibility(View.VISIBLE);
            holder.tvProductQuantity.setText("x" + product.getQuantity());
            holder.tvProductPrice.setText(formatRupiah(product.getPrice()));
            holder.tvProductStock.setText("Stock: " + product.getStock());
            Glide.with(context).load(product.getImageUrl()).into(holder.ivProductImage);

            // Set checkbox berdasarkan isSelected dari produk
            holder.chkSelectItem.setChecked(product.isSelected());

            holder.chkSelectItem.setOnCheckedChangeListener(null); // Reset listener sebelum mengubahnya

            holder.chkSelectItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
                product.setSelected(isChecked);
                if (isChecked) {
                    if (!selectedProducts.contains(product)) {
                        selectedProducts.add(product); // Tambahkan produk jika belum ada
                    }
                } else {
                    selectedProducts.remove(product); // Hapus produk jika checkbox tidak dicentang
                }
                updateTotalHarga(); // Update total harga setelah checkbox berubah
            });

            holder.imgMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (product.getQuantity() > 1) {
                        product.decrementQuantity();
                        holder.tvProductQuantity.setText("x" + product.getQuantity()); // Update quantity text

                        updateQuantityInFirestore(product); // Memperbarui quantity di Firestore
                    } else {
                        removeFromCart(product); // Hapus produk dari keranjang jika quantity 1
                    }
                    updateTotalHarga(); // Update total harga setelah quantity berubah

                }
            });

            holder.imgPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                        if (product.getQuantity() < product.getStock()) {
                            product.incrementQuantity();
                            holder.tvProductQuantity.setText("x" + product.getQuantity());
                            updateQuantityInFirestore(product);
                        } else {
                            Toast.makeText(context, "Quantity tidak bisa melebihi stok", Toast.LENGTH_SHORT).show();
                        }
                        updateTotalHarga();

                }
            });
        }


        // Metode untuk menghitung dan mengupdate total harga
        private void updateTotalHarga() {
            double totalHarga = 0;
            for (Product product : selectedProducts) {
                try {
                    double harga = Double.parseDouble(product.getPrice());
                    int quantity = product.getQuantity();
                    totalHarga += harga * quantity;
                } catch (NumberFormatException e) {
                    // Handle error ketika harga tidak bisa di-parse ke double
                    e.printStackTrace();
                }
            }

            tvTotalHarga.setText("Total " + formatRupiah(String.valueOf(totalHarga)));
        }




        public void removeItem(Product product) {
            keranjangList.remove(product);
            notifyDataSetChanged();
        }
        public void selectAllItems(boolean selectAll) {
            selectedProducts.clear(); // Hapus semua item yang ada sebelumnya
            for (Product product : keranjangList) {
                product.setSelected(selectAll);
                if (selectAll) {
                    selectedProducts.add(product); // Tambahkan semua produk ke selectedProducts
                }
            }
            updateTotalHarga();
            notifyDataSetChanged(); // Refresh tampilan RecyclerView
        }



        private void updateQuantityInFirestore(Product product) {
            String kta = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getString("kta", "");
            if (!kta.isEmpty()) {
                firestore.collection("carts").document(kta)
                        .collection("items")
                        .document(product.getId())
                        .update("quantity", product.getQuantity())
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
            } else {
                 }
        }

        // Fungsi untuk mengurangi quantity atau menghapus item jika quantity 1
        public void removeOrDecrementProduct(Product product) {
            if (product.getQuantity() > 1) {
                // Kurangi quantity
                product.decrementQuantity();
                updateProductInCart(product);
            } else {
                // Hapus item
                removeFromCart(product);
            }
            notifyDataSetChanged(); // Perbarui tampilan
        }

        // Fungsi untuk memperbarui quantity di Firestore
        private void updateProductInCart(Product product) {
            String kta = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getString("kta", "");
            if (!kta.isEmpty()) {
                firestore.collection("carts").document(kta)
                        .collection("items")
                        .document(product.getId())
                        .update("quantity", product.getQuantity())
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
            } else {
                 }
        }

        // Fungsi untuk menghapus item dari Firestore
        private void removeFromCart(Product product) {
            String kta = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getString("kta", "");
            if (!kta.isEmpty()) {
                firestore.collection("carts").document(kta)
                        .collection("items")
                        .document(product.getId())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Produk berhasil dihapus dari keranjang", Toast.LENGTH_SHORT).show();
                                keranjangList.remove(product); // Hapus item dari daftar lokal
                                removeItem(product); // Hapus item dari tampilan

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


        public List<Product> getSelectedProducts() {
            return selectedProducts;
        }

        private String formatRupiah(String harga) {
            double hargaDouble = Double.parseDouble(harga);
            java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance();
            formatter.setMaximumFractionDigits(0);
            formatter.setCurrency(java.util.Currency.getInstance("IDR"));
            return formatter.format(hargaDouble);
        }
        public interface OnCartUpdatedListener {
            void onCartUpdated();
        }


        @Override
        public int getItemCount() {
            return keranjangList.size();
        }
        static class KeranjangViewHolder extends RecyclerView.ViewHolder {
            CheckBox chkSelectItem;
            ImageView ivProductImage,  imgPlus, imgMinus;
            TextView tvProductName, tvProductPrice, tvProductStock, tvProductQuantity;

            public KeranjangViewHolder(@NonNull View itemView) {
                super(itemView);
                chkSelectItem = itemView.findViewById(R.id.chkSelectItem);
                ivProductImage = itemView.findViewById(R.id.ivProductImage);

                imgPlus = itemView.findViewById(R.id.imgPlus);
                imgMinus = itemView.findViewById(R.id.imgMinus);
                tvProductName = itemView.findViewById(R.id.tvProductName);
                tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
                tvProductStock = itemView.findViewById(R.id.tvProductStock);
                tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
            }
        }


    }