package com.dkr.kumbarastore.pembeli;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dkr.kumbarastore.R;

import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {
    private Context context;
    private List<Product> pembelianList;

    public PaymentAdapter(Context context, List<Product> pembelianList) {
        this.context = context;
        this.pembelianList = pembelianList;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pembelian, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Product product = pembelianList.get(position);

        holder.tvProductName.setText(product.getName());

        // Set the formatted price directly
        holder.tvProductPrice.setText(formatRupiah(product.getPrice()));

        holder.tvProductQuantity.setText("x " + product.getQuantity());

        // Calculate and format total harga per item
        double pricePerItem = Utils.cleanAndParsePrice(product.getPrice());
        double totalHargaItem = pricePerItem * product.getQuantity();

        Glide.with(context).load(product.getImageUrl()).into(holder.ivProductImage);
    }


    @Override
    public int getItemCount() {
        return pembelianList.size();
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductPrice, tvProductQuantity;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);

        }
    }

    // Metode untuk mengubah format ke Rupiah
    private String formatRupiah(String harga) {
        try {
            double hargaDouble = Double.parseDouble(harga);
            java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance();
            formatter.setMaximumFractionDigits(0);
            formatter.setCurrency(java.util.Currency.getInstance("IDR"));
            return formatter.format(hargaDouble);
        } catch (NumberFormatException e) {
            return ""; // or handle the error as per your application's requirement
        }
    }
}