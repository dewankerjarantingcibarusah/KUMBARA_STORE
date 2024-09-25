package com.dkr.kumbarastore.pembeli;

import android.content.Context;
import android.content.Intent;
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

public class RiwayatPesanandibatalkanAdapter extends RecyclerView.Adapter<RiwayatPesanandibatalkanAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orderList;

    public RiwayatPesanandibatalkanAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public RiwayatPesanandibatalkanAdapter.OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rincianpesananpembeli2, parent, false);
        return new RiwayatPesanandibatalkanAdapter.OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RiwayatPesanandibatalkanAdapter.OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        // Set order number
        holder.txtOrderNumber.setText(order.getOrderNumber());

        // Set total price
        holder.txtTotalPrice.setText("Total Harga : " + order.getTotalPrice());

        holder.txtDikemas.setText("Dibatalkan");

        // Clear previous product views
        holder.productsLayout.removeAllViews();

        // Loop through products and add them to productsLayout
        for (Product product : order.getProducts()) {
            View productView = LayoutInflater.from(context).inflate(R.layout.item_rincianpesananpembeli, holder.productsLayout, false);

            // Set product details
            ImageView ivProductImage = productView.findViewById(R.id.ivProductImage);
            TextView tvProductName = productView.findViewById(R.id.tvProductName);
            TextView txtPrice = productView.findViewById(R.id.txtPrice);
            TextView tvProductQuantity = productView.findViewById(R.id.tvProductQuantity);

            tvProductName.setText(product.getName());
            txtPrice.setText(Utils.formatRupiah1(product.getPrice()));
            tvProductQuantity.setText("x" + product.getQuantity());

            // Load product image
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(ivProductImage);

            // Add product view to productsLayout
            holder.productsLayout.addView(productView);

            holder.productsLayout1.setOnClickListener(v -> {
                Intent intent = new Intent(context, RincianpesanandibatalkanActivity.class);
                intent.putExtra("orderNumber", order.getOrderNumber());
                intent.putExtra("kta", order.getKta());
                intent.putExtra("from", "riwayatdibatalkan");

                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderNumber, txtDikemas, txtTotalPrice;
        ViewGroup productsLayout, productsLayout1;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtOrderNumber = itemView.findViewById(R.id.txtOrderNumber);
            txtDikemas = itemView.findViewById(R.id.txtDikemas);
            txtTotalPrice = itemView.findViewById(R.id.txtTotalPrice); // Ini adalah TextView untuk menampilkan total harga
            productsLayout1 = itemView.findViewById(R.id.productsLayout1);
            productsLayout = itemView.findViewById(R.id.productsLayout);
        }
    }

}


