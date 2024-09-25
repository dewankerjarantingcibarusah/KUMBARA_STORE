package com.dkr.kumbarastore.pembeli;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dkr.kumbarastore.R;

import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCartProducts;
    private CartProductAdapter cartAdapter;
    private CartManager cartManager;
    private TextView tvTotalPrice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        rvCartProducts = findViewById(R.id.rvCartProducts);
        rvCartProducts.setLayoutManager(new LinearLayoutManager(this));

        cartManager = CartManager.getInstance();
        List<Product> cartProducts = cartManager.getCartProducts();

        cartAdapter = new CartProductAdapter(this, cartProducts);
        rvCartProducts.setAdapter(cartAdapter);

        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        double totalPrice = 0;
        for (Product product : cartManager.getCartProducts()) {
            totalPrice += Double.parseDouble(product.getPrice());
        }
        tvTotalPrice.setText("Total: $" + totalPrice);
    }
}
