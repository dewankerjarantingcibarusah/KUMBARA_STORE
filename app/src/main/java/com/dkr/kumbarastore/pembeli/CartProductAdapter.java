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

public class CartProductAdapter extends RecyclerView.Adapter<CartProductAdapter.CartProductViewHolder> {

    private Context context;
    private List<Product> cartProductList;

    public CartProductAdapter(Context context, List<Product> cartProductList) {
        this.context = context;
        this.cartProductList = cartProductList;
    }

    @NonNull
    @Override
    public CartProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_product, parent, false);
        return new CartProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartProductViewHolder holder, int position) {
        Product product = cartProductList.get(position);

        holder.tvCartProductName.setText(product.getName());
        holder.tvCartProductPrice.setText(product.getPrice());
        Glide.with(context).load(product.getImageUrl()).into(holder.ivCartProductImage);
    }

    @Override
    public int getItemCount() {
        return cartProductList.size();
    }

    class CartProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCartProductImage;
        TextView tvCartProductName, tvCartProductPrice;

        public CartProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCartProductImage = itemView.findViewById(R.id.ivCartProductImage);
            tvCartProductName = itemView.findViewById(R.id.tvCartProductName);
            tvCartProductPrice = itemView.findViewById(R.id.tvCartProductPrice);
        }
    }
}
