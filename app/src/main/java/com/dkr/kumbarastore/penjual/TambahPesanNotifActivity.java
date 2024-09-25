package com.dkr.kumbarastore.penjual;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dkr.kumbarastore.R;
import com.dkr.kumbarastore.pembeli.KeranjangActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TambahPesanNotifActivity extends AppCompatActivity {

    private EditText editTextTitlePesan, editTextBodyPesan;
    private Spinner spinnerKategoriNotifikasi;
    private Button buttonPerbaruiPesan, buttonHapusPesan;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_pesan_notif);

        editTextTitlePesan = findViewById(R.id.editTextTitlePesan);
        editTextBodyPesan = findViewById(R.id.editTextBodyPesan);
        spinnerKategoriNotifikasi = findViewById(R.id.spinnerKategoriNotifikasi);
        buttonPerbaruiPesan = findViewById(R.id.buttonEditPesan);
        buttonHapusPesan = findViewById(R.id.buttonHapusPesan);

        firestore = FirebaseFirestore.getInstance();

        // Load categories when activity is created
        loadCategories();

        buttonPerbaruiPesan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedCategory = spinnerKategoriNotifikasi.getSelectedItem().toString();
                if (selectedCategory.equals("Pilih kategori")) {
                    Toast.makeText(TambahPesanNotifActivity.this, "Pilih kategori untuk diperbarui", Toast.LENGTH_SHORT).show();
                } else {
                    updateMessage(selectedCategory);
                }
            }
        });

        buttonHapusPesan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedCategory = spinnerKategoriNotifikasi.getSelectedItem().toString();
                if (selectedCategory.equals("Pilih kategori")) {
                    Toast.makeText(TambahPesanNotifActivity.this, "Pilih kategori untuk dihapus", Toast.LENGTH_SHORT).show();
                } else {
                    deleteMessage(selectedCategory);
                }
            }
        });

        spinnerKategoriNotifikasi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    String selectedCategory = parent.getItemAtPosition(position).toString();
                    loadMessageData(selectedCategory);
                    buttonHapusPesan.setVisibility(View.VISIBLE);
                } else {
                    buttonHapusPesan.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void loadCategories() {
        firestore.collection("messages").document("Kategori Notifikasi").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> categories = new ArrayList<>();
                        categories.add("Pilih kategori"); // Default item

                        // Iterasi untuk mendapatkan semua field dan nilainya
                        for (Map.Entry<String, Object> entry : documentSnapshot.getData().entrySet()) {
                            String fieldName = entry.getKey();
                            Object fieldValue = entry.getValue();

                            // Pastikan nilai field adalah String
                            if (fieldValue instanceof String) {
                                categories.add((String) fieldValue);
                            }
                        }

                        // Setup adapter untuk spinner
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(TambahPesanNotifActivity.this, android.R.layout.simple_spinner_item, categories);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerKategoriNotifikasi.setAdapter(adapter);
                    } else {
                        Toast.makeText(TambahPesanNotifActivity.this, "Kategori tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(TambahPesanNotifActivity.this, "Gagal memuat kategori", Toast.LENGTH_SHORT).show());
    }


    private void addMessage(String selectedCategory) {
        String titlePesan = editTextTitlePesan.getText().toString().trim();
        String bodyPesan = editTextBodyPesan.getText().toString().trim();

        Map<String, Object> message = new HashMap<>();
        message.put("title", titlePesan);
        message.put("body", bodyPesan);

        firestore.collection("messages").document(selectedCategory)
                .update(message)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TambahPesanNotifActivity.this, "Pesan berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    resetUI();
                })
                .addOnFailureListener(e -> Toast.makeText(TambahPesanNotifActivity.this, "Gagal menambahkan pesan", Toast.LENGTH_SHORT).show());
    }

    private void updateMessage(String selectedCategory) {
        String titlePesan = editTextTitlePesan.getText().toString().trim();
        String bodyPesan = editTextBodyPesan.getText().toString().trim();

        Map<String, Object> message = new HashMap<>();
        message.put("title", titlePesan);
        message.put("body", bodyPesan);

        firestore.collection("messages").document(selectedCategory)
                .update(message)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TambahPesanNotifActivity.this, "Pesan berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    spinnerKategoriNotifikasi.setSelection(0);
                    resetUI();
                })
                .addOnFailureListener(e -> Toast.makeText(TambahPesanNotifActivity.this, "Gagal memperbarui pesan", Toast.LENGTH_SHORT).show());
    }

    private void deleteMessage(String categoryName) {
        firestore.collection("messages").document(categoryName)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TambahPesanNotifActivity.this, "Pesan berhasil dihapus", Toast.LENGTH_SHORT).show();
                    spinnerKategoriNotifikasi.setSelection(0);
                    resetUI();
                })
                .addOnFailureListener(e -> Toast.makeText(TambahPesanNotifActivity.this, "Gagal menghapus pesan", Toast.LENGTH_SHORT).show());
    }

    private void loadMessageData(String categoryName) {
        firestore.collection("messages").document(categoryName).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String body = documentSnapshot.getString("body");

                        editTextTitlePesan.setText(title != null ? title : "");
                        editTextBodyPesan.setText(body != null ? body : "");
                    } else {
                        Toast.makeText(TambahPesanNotifActivity.this, "Data pesan tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(TambahPesanNotifActivity.this, "Gagal memuat data pesan", Toast.LENGTH_SHORT).show());
    }

    private void resetUI() {
        editTextTitlePesan.setText("");
        editTextBodyPesan.setText("");
        buttonHapusPesan.setVisibility(View.GONE);
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, TampilanpenjualActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
