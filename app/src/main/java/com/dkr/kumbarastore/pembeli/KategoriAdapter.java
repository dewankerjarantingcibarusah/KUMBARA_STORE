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

import com.dkr.kumbarastore.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class KategoriAdapter extends RecyclerView.Adapter<KategoriAdapter.ViewHolder> {

    private Context context;
    private List<Kategori> kategoriList;

    public KategoriAdapter(Context context, List<Kategori> kategoriList) {
        this.context = context;
        this.kategoriList = kategoriList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_kategori, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Kategori kategori = kategoriList.get(position);

        // Set data to views
        holder.tvKategoriName.setText(kategori.getNama());
        Picasso.get().load(kategori.getImageUrl()).placeholder(R.drawable.placeholder_image).into(holder.ivKategoriImage);

        // Handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement intent to RincianKategoriActivity with relevant data
                Intent intent = new Intent(context, RincianKategoriActivity.class);
                intent.putExtra("kategori", kategori.getNama());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return kategoriList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView ivKategoriImage;
        public TextView tvKategoriName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivKategoriImage = itemView.findViewById(R.id.ivKategoriImage);
            tvKategoriName = itemView.findViewById(R.id.tvKategoriName);
        }
    }
}
