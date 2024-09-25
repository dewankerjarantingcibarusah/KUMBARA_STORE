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
public class RincianpesanandibatalkanAdapter extends RecyclerView.Adapter<RincianpesanandibatalkanAdapter.ViewHolder> {
    private List<Product> productList;
    private Context context;

    public RincianpesanandibatalkanAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pembelian, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText(formatRupiah(product.getPrice()));
        holder.tvProductQuantity.setText("x " + product.getQuantity());
        Glide.with(context).load(product.getImageUrl()).into(holder.ivProductImage);
    }
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
    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductPrice, tvProductQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
        }
    }
}