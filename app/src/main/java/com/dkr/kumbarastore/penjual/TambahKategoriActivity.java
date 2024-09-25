package com.dkr.kumbarastore.penjual;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dkr.kumbarastore.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class TambahKategoriActivity extends AppCompatActivity {

    private EditText editTextCategory;
    private ImageView imageView;
    private Button buttonSave;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri imageUri;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_kategori);

        editTextCategory = findViewById(R.id.editTextCategory);
        imageView = findViewById(R.id.imageView);
        buttonSave = findViewById(R.id.buttonSave);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("images");

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri == null) {
                    // Tampilkan pesan untuk memilih gambar jika belum dipilih
                    Toast.makeText(TambahKategoriActivity.this, "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show();
                } else {
                    simpanKategori();
                }
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    private void simpanKategori() {
        final String kategoriBaru = editTextCategory.getText().toString().trim();

        // Validasi input
        if (kategoriBaru.isEmpty()) {
            Toast.makeText(this, "Masukkan nama kategori", Toast.LENGTH_SHORT).show();
            return;
        }

        // Upload gambar ke Firebase Storage
        final StorageReference fileReference = storageReference.child(kategoriBaru + "." + getFileExtension(imageUri));

        // Memulai proses upload
        fileReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Mendapatkan URL download file setelah berhasil diupload
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();

                                // Simpan data (termasuk URL gambar, nama kategori) ke Firestore
                                simpanDataFirestore(kategoriBaru, imageUrl);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Tampilkan pesan error jika upload gambar gagal
                        Toast.makeText(TambahKategoriActivity.this, "Gagal mengupload gambar", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void simpanDataFirestore(final String kategoriBaru, final String imageUrl) {
        // Cek apakah kategori sudah ada dalam Firestore
        firestore.collection("kategori").document(kategoriBaru)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        // Jika dokumen kategori sudah ada, tampilkan pesan bahwa kategori sudah ada
                        if (documentSnapshot.exists()) {
                            Toast.makeText(TambahKategoriActivity.this, "Kategori sudah ada", Toast.LENGTH_SHORT).show();
                        } else {
                            // Jika kategori belum ada, tambahkan kategori baru ke Firestore
                            Map<String, Object> kategoriData = new HashMap<>();
                            kategoriData.put("nama", kategoriBaru);
                            kategoriData.put("imageUrl", imageUrl);

                            firestore.collection("kategori").document(kategoriBaru)
                                    .set(kategoriData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(TambahKategoriActivity.this, "Kategori berhasil disimpan", Toast.LENGTH_SHORT).show();
                                            editTextCategory.setText(""); // Kosongkan input setelah berhasil disimpan
                                            imageView.setImageResource(R.drawable.placeholder_image); // Kosongkan gambar setelah berhasil disimpan
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(TambahKategoriActivity.this, "Gagal menyimpan kategori", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(TambahKategoriActivity.this, "Gagal mengambil data kategori", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getFileExtension(Uri uri) {
        // Mendapatkan ekstensi file dari URI
        return getContentResolver().getType(uri).split("/")[1];
    }
}
