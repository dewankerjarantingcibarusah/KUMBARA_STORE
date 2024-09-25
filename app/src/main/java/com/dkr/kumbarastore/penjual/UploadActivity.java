package com.dkr.kumbarastore.penjual;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dkr.kumbarastore.R;
import com.dkr.kumbarastore.pembeli.AkunActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private EditText editTextImageName, editTextPrice, editTextStock;
    private Spinner spinnerKategori, spinnerBarang;
    private Button buttonUpload, buttonUpdateProduk,buttonDelete;
    private Uri imageUri;
    private StorageReference storageReference;
    private FirebaseFirestore firestore;
    private boolean isUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        imageView = findViewById(R.id.imageView);
        editTextImageName = findViewById(R.id.editTextImageName);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextStock = findViewById(R.id.editTextStock);
        spinnerKategori = findViewById(R.id.spinnerKategori);
        spinnerBarang = findViewById(R.id.spinnerBarang);
        buttonUpload = findViewById(R.id.buttonUpload);
        buttonUpdateProduk = findViewById(R.id.buttonUpdateProduk);
        buttonDelete = findViewById(R.id.buttonDelete);
        // Initialize Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference("Kumbara Store");

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Load categories into Spinner
        loadCategories();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUpdating) {
                    resetUI(); // Jika sedang dalam mode edit, reset UI
                } else {
                    if (spinnerKategori.getSelectedItemPosition() == 0) {
                        Toast.makeText(UploadActivity.this, "Pilih jenis kategori", Toast.LENGTH_SHORT).show();
                    } else {
                        uploadImage(); // Set isUpdating menjadi false setelah selesai update
                    }

                }
            }
        });


        buttonUpdateProduk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonUpdateProduk.getText().toString().equals("Pilih Produk")) {
                    loadItems();
                    spinnerBarang.setVisibility(View.VISIBLE);
                    buttonUpdateProduk.setText("Perbarui Produk");
                    editTextImageName.setVisibility(View.GONE);
                    buttonUpload.setText("Tambah Produk");
                    editTextStock.setText("");
                    editTextPrice.setText("");
                    isUpdating = true; // Set isUpdating menjadi true saat masuk ke mode edit

                } else {
                    // Pengecekan apakah item telah dipilih dari SpinnerBarang
                    if (spinnerBarang.getSelectedItemPosition() == 0) {
                        Toast.makeText(UploadActivity.this, "Pilih barang untuk diupdate", Toast.LENGTH_SHORT).show();
                    } else {
                        updateItem();
                        isUpdating = false; // Set isUpdating menjadi false setelah selesai update

                    }
                }
            }
        });
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if an item is selected from spinnerBarang
                if (spinnerBarang.getSelectedItemPosition() > 0) {
                    String selectedItem = spinnerBarang.getSelectedItem().toString();

                    deleteItem(selectedItem); // Panggil deleteItem dengan nama barang yang dipilih
                } else {
                    Toast.makeText(UploadActivity.this, "Pilih barang untuk dihapus", Toast.LENGTH_SHORT).show();
                }
            }
        });
        spinnerBarang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) { // Skip the first item as it's the default prompt
                    String selectedItem = parent.getItemAtPosition(position).toString();
                    loadItemData(selectedItem);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    private void deleteItem(final String itemName) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("Kumbara Store/" + itemName + ".jpg");

        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Jika penghapusan gambar berhasil, lanjutkan dengan menghapus data dari Firestore
                firestore.collection("items").document(itemName)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(UploadActivity.this, "Data barang berhasil dihapus", Toast.LENGTH_SHORT).show();
                                resetUI();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof StorageException && ((StorageException) e).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {

                    // Hapus data dari Firestore tanpa mencoba menghapus objek dari Firebase Storage lagi
                    firestore.collection("items").document(itemName)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(UploadActivity.this, "Data barang berhasil dihapus", Toast.LENGTH_SHORT).show();
                                    resetUI();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                } else {
                    // Kesalahan lainnya
                }
            }
        });
    }



    private void loadCategories() {
        firestore.collection("kategori").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<String> categories = new ArrayList<>();
                        categories.add("Pilih jenis kategori");
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            categories.add(documentSnapshot.getString("nama"));
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(UploadActivity.this, android.R.layout.simple_spinner_item, categories);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerKategori.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadActivity.this, "Gagal memuat kategori", Toast.LENGTH_SHORT).show();
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

    private void uploadImage() {
        if (imageUri != null) {
            final String imageName = editTextImageName.getText().toString().trim();
            final String price = editTextPrice.getText().toString().trim();
            final int stock = Integer.parseInt(editTextStock.getText().toString().trim());

            final String category = spinnerKategori.getSelectedItem().toString();

            // Menyimpan gambar dengan nama barang sebagai nama file di Firebase Storage
            final StorageReference fileReference = storageReference.child(imageName + ".jpg" );

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

                                    // Simpan data (termasuk URL gambar, nama, harga, dan stok) ke Firestore
                                    saveDataToFirestore(imageUrl, imageName, price, stock, category,0);

                                    // Tampilkan pesan sukses
                                    Toast.makeText(UploadActivity.this, "Berhasil menambahkan produk " + imageName + "" + " dengan harga Rp " + price + " berjumlah " + stock + " di kategori " + category, Toast.LENGTH_SHORT).show();
                                    resetUI();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Tampilkan pesan error jika upload gagal
                            Toast.makeText(UploadActivity.this, "Upload gagal", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateItem() {
        final String selectedItem = spinnerBarang.getSelectedItem().toString();
        final String price = editTextPrice.getText().toString().trim();
        final int stock = Integer.parseInt(editTextStock.getText().toString().trim());
        final String category = spinnerKategori.getSelectedItem().toString();

        DocumentReference itemRef = firestore.collection("items").document(selectedItem);

        // Jika ada gambar yang dipilih, update juga gambar di Firebase Storage
        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(selectedItem + ".jpg" );
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Mendapatkan URL download file setelah berhasil diupload
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();

                                    // Update data barang di Firestore termasuk URL gambar baru
                                    itemRef.update("imageUrl", imageUrl, "price", price, "stock", stock, "kategori", category)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(UploadActivity.this, "Update berhasil", Toast.LENGTH_SHORT).show();
                                                    resetUI();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(UploadActivity.this, "Update gagal", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Tampilkan pesan error jika upload gambar gagal
                            Toast.makeText(UploadActivity.this, "Upload gambar gagal", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Jika tidak ada gambar yang dipilih, hanya update data lainnya
            itemRef.update("price", price, "stock", stock, "kategori", category)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(UploadActivity.this, "Update berhasil", Toast.LENGTH_SHORT).show();
                            resetUI();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UploadActivity.this, "Update gagal", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void resetUI() {
        editTextImageName.setText("");
        editTextPrice.setText("");
        editTextStock.setText("");
        imageView.setImageResource(R.drawable.placeholder_image);
        spinnerKategori.setSelection(0);
        buttonUpload.setText("Tambah Produk");
        buttonUpdateProduk.setText("Pilih Produk");
        spinnerBarang.setVisibility(View.GONE);
        editTextImageName.setVisibility(View.VISIBLE);
        isUpdating = false;
        imageUri = null;
        buttonDelete.setVisibility(View.GONE);
    }

    // Mendapatkan ekstensi file dari URI
    private String getFileExtension(Uri uri) {
        if (uri == null) {
            return null;
        }
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(getContentResolver().getType(uri));
    }


    // Simpan data (nama gambar, harga, stok, dan kategori) ke Firestore
    private void saveDataToFirestore(String imageUrl, String imageName, String price, int stock, String kategori, int terjual) {
        Map<String, Object> data = new HashMap<>();
        data.put("imageUrl", imageUrl);
        data.put("imageName", imageName);
        data.put("price", price);
        data.put("stock", stock);
        data.put("kategori", kategori);
        data.put("terjual", terjual);

        // Simpan data ke Firestore
        firestore.collection("items")
                .document(imageName)
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Handle success
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle error
                    }
                });
    }


    private void loadItems() {
        firestore.collection("items").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<String> items = new ArrayList<>();
                        items.add("Pilih barang untuk diupdate");
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            items.add(documentSnapshot.getId());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(UploadActivity.this, android.R.layout.simple_spinner_item, items);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerBarang.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadActivity.this, "Gagal memuat barang", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void loadItemData(String itemName) {
        firestore.collection("items").document(itemName).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String imageUrl = documentSnapshot.getString("imageUrl");
                            String price = documentSnapshot.getString("price");
                            int stock = documentSnapshot.getLong("stock").intValue();
                            String kategori = documentSnapshot.getString("kategori");

                            // Load the image into ImageView using Picasso
                            Picasso.get().load(imageUrl).into(imageView);

                            // Set the values to the respective fields
                            editTextPrice.setText(price);
                            editTextStock.setText(String.valueOf(stock));

                            // Set the selected category
                            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerKategori.getAdapter();
                            int position = adapter.getPosition(kategori);
                            spinnerKategori.setSelection(position);

                            // Show delete button
                            buttonDelete.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadActivity.this, "Gagal memuat data barang", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
