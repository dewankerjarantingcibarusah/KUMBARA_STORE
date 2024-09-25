package com.dkr.kumbarastore;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.DateValidatorPointForward;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PerformanceHintManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.dkr.kumbarastore.pembeli.AkunActivity;
import com.dkr.kumbarastore.pembeli.FCMSender;
import com.dkr.kumbarastore.pembeli.NotificationUtils;
import com.dkr.kumbarastore.pembeli.PaymentActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SignupActivity extends AppCompatActivity {

    private EditText etNama, etKta, etTtl, etAlamat, etPangkalan, etGudep, etAmbalan, etNotelp, etTanggalLahir, etEmail, etVerificationCode;
    private Spinner spinnerJenisKelamin;
    private Button btnSignup, btnSendVerification, btnVerify;

    private TextView textViewLogin, ttTanggal;
    private ImageView imgKalender,imgVerifikasi;
    private FirebaseFirestore db;
    private AlertDialog alertDialog;
    private static final String KTA_PREFIX = "09.16.22.";

    private static final String[] Notelp = {"628", "08"};

    private String verificationCode;

    private boolean isEmailVerified = false;

    private ProgressBar progressBar;
    private CountDownTimer countDownTimer;
    private static final long COUNTDOWN_IN_MILLIS = 60000; // 1 menit
    private static final long COUNTDOWN_INTERVAL = 1000;   // 1 detik
    private boolean isTimerRunning = false;


    private long countdownTimeLeft; // Waktu yang tersisa untuk countdown dalam milidetik
    private long countdownEndTime; // Waktu kapan countdown akan selesai

    private long codeSentTime;

    private List<Long> requestTimestamps = new ArrayList<>();
    private long disabledUntil = 0;

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        db = FirebaseFirestore.getInstance();

        etNama = findViewById(R.id.etNama);
        etKta = findViewById(R.id.etKta);
        etTtl = findViewById(R.id.etTempatLahir);
        etAlamat = findViewById(R.id.etAlamat);
        etPangkalan = findViewById(R.id.etPangkalan);
        etGudep = findViewById(R.id.etGudep);
        etAmbalan = findViewById(R.id.etAmbalan);
        etNotelp = findViewById(R.id.etNotelp);
        ttTanggal = findViewById(R.id.textViewTanggal);
        spinnerJenisKelamin = findViewById(R.id.spinnerJeniskelamin);
        btnSignup = findViewById(R.id.btnSignup);
        textViewLogin = findViewById(R.id.textViewLogin);
        imgKalender = findViewById(R.id.imageViewCalendar);
        imgVerifikasi=findViewById(R.id.imgVerifikasi);
        btnSendVerification = findViewById(R.id.btnSendVerification);
        btnVerify = findViewById(R.id.btnVerify);
        etEmail = findViewById(R.id.etEmail);
        etVerificationCode = findViewById(R.id.etVerificationCode);
        progressBar = findViewById(R.id.progressBar);

        // Setup spinner for gender
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.jenis_kelamin_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerJenisKelamin.setAdapter(adapter);

        // Tambahkan item "Pilih jenis kategori" ke array
        String[] genderArray = getResources().getStringArray(R.array.jenis_kelamin_array);
        List<String> categories = new ArrayList<>(Arrays.asList(genderArray));
        categories.add(0, "Pilih jenis kelamin");

        // Set adapter spinner dengan array yang telah dimodifikasi
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerJenisKelamin.setAdapter(genderAdapter);

        // Set onClickListener untuk etTanggalLahir agar memunculkan DatePickerDialog
        imgKalender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        btnSendVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();

                // Hapus permintaan yang lebih dari 1 jam yang lalu
                requestTimestamps.removeIf(timestamp -> (currentTime - timestamp) > 3600000); // 3600000 ms = 1 jam

                // Cek apakah sudah ada 3 permintaan dalam 1 jam terakhir
                if (requestTimestamps.size() >= 4) {
                    // Blokir tombol dan set waktu unblock menjadi 2 jam dari sekarang
                    btnSendVerification.setEnabled(false);
                    disabledUntil = currentTime + 7200000; // 7200000 ms = 2 jam

                    // Informasikan pengguna
                    Toast.makeText(SignupActivity.this, "Terlalu banyak permintaan. Silahkan coba lagi dalam waktu 2 jam.", Toast.LENGTH_LONG).show();

                    // Gunakan Handler untuk mengaktifkan tombol kembali setelah 2 jam
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btnSendVerification.setEnabled(true);
                        }
                    }, 7200000); // 2 jam dalam milidetik
                } else {
                    // Tambahkan waktu permintaan ke dalam daftar
                    requestTimestamps.add(currentTime);


                    // Kirim kode verifikasi dan lanjutkan proses
                    checkemail();
                }
            }
        });


        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCode();
            }
        });
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                registerUser();
            }
        });


        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(SignupActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

    }
    private class SendMailTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String recipientEmail = params[0];
            String subject = params[1];
            String body = params[2];

            MailSender mailSender = new MailSender();
            mailSender.sendEmail(recipientEmail, subject, body, new MailSender.MailCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(SignupActivity.this, "Kode verifikasi telah dikirim ke email Anda", Toast.LENGTH_SHORT).show();
                            imgVerifikasi.setVisibility(View.GONE);
                            etVerificationCode.setText("");
                            codeSentTime = System.currentTimeMillis();
                            isEmailVerified = false;
                            startCountdown();
                        }
                    });
                }

                @Override
                public void onFailure(final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(SignupActivity.this, "Gagal mengirim email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            return null;
        }
    }


    private void sendVerificationCode() {
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Masukkan email", Toast.LENGTH_SHORT).show();
            return;
        }

        verificationCode = generateVerificationCode();
        String subject = "Kode Verifikasi Email";
        String message = "Kode verifikasi Anda adalah: " + verificationCode +
                "\n\nKode ini berlaku selama 5 menit dari waktu pengiriman.";

        new SendMailTask().execute(email, subject, message);

    }


    private void verifyCode() {
        progressBar.setVisibility(View.VISIBLE);
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - codeSentTime;

        if (elapsedTime > 5 * 60 * 1000) { // 2 minutes in milliseconds
            Toast.makeText(this, "Kode verifikasi telah kedaluwarsa", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        String inputCode = etVerificationCode.getText().toString().trim();
        if (TextUtils.isEmpty(inputCode)) {
            Toast.makeText(this, "Masukkan kode verifikasi", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (inputCode.equals(verificationCode)) {
            Toast.makeText(this, "Email terverifikasi", Toast.LENGTH_SHORT).show();
            isEmailVerified = true; // Update verification status to true
            progressBar.setVisibility(View.GONE);
            imgVerifikasi.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Kode verifikasi salah", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }
    private void startCountdown() {
        if (isTimerRunning) {
            return; // Jangan mulai timer jika sudah berjalan
        }

        countDownTimer = new CountDownTimer(COUNTDOWN_IN_MILLIS, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                btnSendVerification.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10); // Ukuran teks dalam SP (scale-independent pixels)
                btnSendVerification.setText("Kirim Ulang Kode (" + millisUntilFinished / 1000 + ")");
            }

            @Override
            public void onFinish() {
                btnSendVerification.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                btnSendVerification.setText("Kirim Kode");
                isTimerRunning = false;
                btnSendVerification.setEnabled(true);
            }
        }.start();

        isTimerRunning = true;
        btnSendVerification.setEnabled(false); // Nonaktifkan tombol saat timer berjalan
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Generate a random 6-digit code
        return String.valueOf(code);
    }
    private void showAlertDialog(String Phonenumber) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Peringatan");
        builder.setMessage("Apakah Anda bukan dari wilayah Kecamatan Cibarusah? Jika bukan, silahkan 'hubungi admin' untuk melakukan pendaftaran manual.");

        builder.setPositiveButton("Hubungi Admin", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                contactAdminViaWhatsApp(Phonenumber);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void contactAdminViaWhatsApp(String Phonenumber) {
        // Query ke Firestore untuk mencari dokumen dengan kata kunci "penjual"
        db.collection("users")
                .whereEqualTo("posisi", "penjual") // Ganti dengan field yang sesuai di Firestore
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Ambil dokumen pertama yang sesuai
                            DocumentSnapshot sellerDoc = task.getResult().getDocuments().get(0);

                            // Ambil nomor telepon penjual dari dokumen
                            String sellerPhoneNumber = sellerDoc.getString("notelp");

                            // Buat pesan WhatsApp dengan informasi pengguna
                            String message = "Hallo, saya ingin mendaftarkan di aplikasi Anda berikut biodata diri saya.\n"
                                    + "Nama: " + etNama.getText().toString().trim()
                                    + "\nKTA: " + etKta.getText().toString().trim()
                                    + "\nTempat Lahir: " + etTtl.getText().toString().trim()
                                    + "\nTanggal Lahir: " + ttTanggal.getText().toString().trim()
                                    + "\nJenis Kelamin: " + spinnerJenisKelamin.getSelectedItem().toString()
                                    + "\nAlamat Lengkap: " + etAlamat.getText().toString().trim()
                                    + "\nPangkalan: " + etPangkalan.getText().toString().trim()
                                    + "\nGugus Depan: " + etGudep.getText().toString().trim()
                                    + "\nAmbalan: " + etAmbalan.getText().toString().trim()
                                    + "\nNomor Telpon/WhatsApp: " + Phonenumber;

                            showWhatsAppChoiceDialog(sellerPhoneNumber, message);
                        } else {
                            Toast.makeText(SignupActivity.this, "Gagal menemukan penjual.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showWhatsAppChoiceDialog(String sellerPhoneNumber, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Aplikasi WhatsApp")
                .setItems(new String[]{"WhatsApp Biasa", "WhatsApp Bisnis"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String packageName;
                        if (which == 0) {
                            packageName = "com.whatsapp";
                        } else {
                            packageName = "com.whatsapp.w4b";
                        }
                        sendWhatsAppMessage(sellerPhoneNumber, message, packageName);
                    }
                })
                .show();
    }

    private void sendWhatsAppMessage(String sellerPhoneNumber, String message, String packageName) {
        try {
            String encodedMessage = URLEncoder.encode(message, "UTF-8");
            String whatsappUrl = "https://api.whatsapp.com/send?phone=" + sellerPhoneNumber + "&text=" + encodedMessage;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(whatsappUrl));
            intent.setPackage(packageName);
            startActivity(intent);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(SignupActivity.this, "Gagal membuka WhatsApp. Pastikan WhatsApp terinstal di perangkat Anda.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(SignupActivity.this, "Gagal membuka WhatsApp. Pastikan WhatsApp terinstal di perangkat Anda.", Toast.LENGTH_SHORT).show();
        }
    }


    private void registerUser() {
        progressBar.setVisibility(View.VISIBLE);

        final String nama = etNama.getText().toString().trim();
        final String kta = etKta.getText().toString().trim();
        final String ttl = etTtl.getText().toString().trim();
        final String alamat = etAlamat.getText().toString().trim();
        final String pangkalan = etPangkalan.getText().toString().trim();
        final String gudep = etGudep.getText().toString().trim();
        final String ambalan = etAmbalan.getText().toString().trim();
        final String notelp = etNotelp.getText().toString().trim();
        final String jenisKelamin = spinnerJenisKelamin.getSelectedItem().toString();
        final String tanggalLahir = ttTanggal.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();


        // Ubah nomor telepon sesuai format internasional
        String phoneNumber = normalizePhoneNumber(notelp);

        if (TextUtils.isEmpty(kta)) {
            Toast.makeText(this, "Masukkan nomor KTA", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(nama)) {
            Toast.makeText(this, "Masukkan nama", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(ttl)) {
            Toast.makeText(this, "Masukkan tempat lahir", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(tanggalLahir) || tanggalLahir.equals("Tanggal belum dipilih")) {
            Toast.makeText(this, "Pilih tanggal lahir", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (spinnerJenisKelamin.getSelectedItemPosition() == 0) {
            Toast.makeText(SignupActivity.this, "Pilih jenis kelamin", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(alamat)) {
            Toast.makeText(this, "Masukkan alamat", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (TextUtils.isEmpty(pangkalan)) {
            Toast.makeText(this, "Masukkan pangkalan", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (TextUtils.isEmpty(gudep)) {
            Toast.makeText(this, "Masukkan gugus depan", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (TextUtils.isEmpty(ambalan)) {
            Toast.makeText(this, "Masukkan ambalan", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (TextUtils.isEmpty(notelp)) {
            Toast.makeText(this, "Masukkan nomor telepon", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        // Validasi nomor telepon menggunakan prefix yang diizinkan
        if (!isValidPhoneNumber(notelp)) {
            Toast.makeText(this, "Nomor telepon harus dimulai dengan " + Arrays.toString(Notelp), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Masukkan email", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(verificationCode)) {
            Toast.makeText(this, "Masukkan kode verifikasi", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (!isEmailVerified) {
            Toast.makeText(this, "Silakan verifikasi email Anda terlebih dahulu", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (!kta.startsWith(KTA_PREFIX)) {
            showAlertDialog(phoneNumber);
            progressBar.setVisibility(View.GONE);
            return;
        }

        // Periksa apakah KTA sudah ada sebelum menyimpan
        checkKtaAvailability(kta , phoneNumber);
    }

    private String normalizePhoneNumber(String phoneNumber) {
        // Ubah nomor telepon ke format internasional jika dimulai dengan "08" atau "62"
        if (phoneNumber.startsWith("08")) {
            return "+62" + phoneNumber.substring(1); // Ubah 08 menjadi +62
        } else if (phoneNumber.startsWith("62")) {
            return "+" + phoneNumber; // Tambahkan + jika hanya dimulai dengan 62
        }
        return phoneNumber; // Kembalikan nomor telepon asli jika sudah dalam format internasional
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Validasi nomor telepon dengan prefix yang diizinkan
        for (String prefix : Notelp) {
            if (phoneNumber.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }


    private void checkKtaAvailability(final String kta, String Phonenumber) {
        db.collection("users")
                .whereEqualTo("kta", kta)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                // KTA sudah ada dalam database
                                Toast.makeText(SignupActivity.this, "Nomor KTA sudah terdaftar", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            } else {

                                saveUserToFirestore(
                                        etNama.getText().toString().trim(),
                                        etKta.getText().toString().trim(),
                                        spinnerJenisKelamin.getSelectedItem().toString(),
                                        "pembeli",
                                        etTtl.getText().toString().trim(),
                                        etAlamat.getText().toString().trim(),
                                        etPangkalan.getText().toString().trim(),
                                        etGudep.getText().toString().trim(),
                                        etAmbalan.getText().toString().trim(),
                                        Phonenumber,
                                        ttTanggal.getText().toString().trim(),
                                        etEmail.getText().toString().trim(),
                                        etVerificationCode.getText().toString().trim()
                                );
                            }
                        } else {
                           }
                    }
                });
    }

    private void checkemail() {
        progressBar.setVisibility(View.VISIBLE);

        final String nama = etNama.getText().toString().trim();
        final String kta = etKta.getText().toString().trim();
        final String ttl = etTtl.getText().toString().trim();
        final String alamat = etAlamat.getText().toString().trim();
        final String pangkalan = etPangkalan.getText().toString().trim();
        final String gudep = etGudep.getText().toString().trim();
        final String ambalan = etAmbalan.getText().toString().trim();
        final String notelp = etNotelp.getText().toString().trim();
        final String jenisKelamin = spinnerJenisKelamin.getSelectedItem().toString();
        final String tanggalLahir = ttTanggal.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();


        // Ubah nomor telepon sesuai format internasional
        String phoneNumber = normalizePhoneNumber(notelp);

        if (TextUtils.isEmpty(kta)) {
            Toast.makeText(this, "Masukkan nomor KTA", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(nama)) {
            Toast.makeText(this, "Masukkan nama", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(ttl)) {
            Toast.makeText(this, "Masukkan tempat lahir", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(tanggalLahir) || tanggalLahir.equals("Tanggal belum dipilih")) {
            Toast.makeText(this, "Pilih tanggal lahir", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (spinnerJenisKelamin.getSelectedItemPosition() == 0) {
            Toast.makeText(SignupActivity.this, "Pilih jenis kelamin", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(alamat)) {
            Toast.makeText(this, "Masukkan alamat", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (TextUtils.isEmpty(pangkalan)) {
            Toast.makeText(this, "Masukkan pangkalan", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (TextUtils.isEmpty(gudep)) {
            Toast.makeText(this, "Masukkan gugus depan", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(ambalan)) {
            Toast.makeText(this, "Masukkan ambalan", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (TextUtils.isEmpty(notelp)) {
            Toast.makeText(this, "Masukkan nomor telepon", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        // Validasi nomor telepon menggunakan prefix yang diizinkan
        if (!isValidPhoneNumber(notelp)) {
            Toast.makeText(this, "Nomor telepon harus dimulai dengan " + Arrays.toString(Notelp), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Masukkan email", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }



        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                // KTA sudah ada dalam database
                                Toast.makeText(SignupActivity.this, "Email sudah terdaftar", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            } else {
                                sendVerificationCode();
                                // KTA belum ada dalam database, simpan data pengguna
                            }
                        } else {
                        }
                    }
                });
    }
    private void saveUserToFirestore(String nama, String kta, String jenisKelamin, String posisi,
                                     String ttl, String alamat, String pangkalan, String gudep, String ambalan, String notelp,
                                     String tanggalLahir, String email, String verificationCode) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("nama", nama);
        userMap.put("kta", kta);
        userMap.put("jenis_kelamin", jenisKelamin);
        userMap.put("posisi", posisi);
        userMap.put("ttl", ttl);
        userMap.put("pangkalan", pangkalan);
        userMap.put("gudep", gudep);
        userMap.put("ambalan", ambalan);
        userMap.put("notelp", notelp);
        userMap.put("tanggal_lahir", tanggalLahir);
        userMap.put("email", email);
        userMap.put("email_verified", isEmailVerified);


        String documentId = kta + " " + nama; // Menggunakan kta + nama sebagai identifier dokumen

        db.collection("users")
                .document(documentId) // Gunakan kta sebagai identifier dokumen
                .set(userMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Data pengguna berhasil disimpan, sekarang simpan alamat
                            Map<String, Object> addressMap = new HashMap<>();
                            addressMap.put("nama", nama);
                            addressMap.put("alamat", alamat);
                            addressMap.put("notelp", notelp);
                            addressMap.put("alamatutama", "yes");

                            db.collection("users")
                                    .document(documentId)
                                    .collection("alamat pengiriman")
                                    .document("alamat1")
                                    .set(addressMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                // Delay hiding the progress bar
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(SignupActivity.this, "Berhasil membuat akun", Toast.LENGTH_SHORT).show();
                                                        Caritokenpenjual(nama, kta);
                                                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                        finish(); // Kembali ke activity sebelumnya
                                                    }
                                                }, 2000); // Delay for 2 seconds (2000 milliseconds)

                                            } else {
                                            }
                                        }
                                    });
                        } else {
                        }
                    }
                });
    }




    private void Caritokenpenjual(String Nama, String Kta) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // First, search for the 'penjual' in the 'users' collection
        db.collection("users")
                .whereEqualTo("posisi", "penjual")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (DocumentSnapshot userDocument : querySnapshot.getDocuments()) {
                                String kta = userDocument.getString("kta"); // Assuming 'kta' is the document ID
                                String role = userDocument.getString("posisi"); // Get the 'role' field
                                // Now search in the 'tokens' collection for this 'kta'
                                db.collection("tokens")
                                        .document(role)
                                        .collection(kta)
                                        .whereEqualTo("role", "penjual")
                                        .get()
                                        .addOnCompleteListener(deviceTask -> {
                                            if (deviceTask.isSuccessful()) {
                                                QuerySnapshot deviceQuerySnapshot = deviceTask.getResult();
                                                if (deviceQuerySnapshot != null && !deviceQuerySnapshot.isEmpty()) {
                                                    for (DocumentSnapshot deviceDocument : deviceQuerySnapshot.getDocuments()) {
                                                        String token = deviceDocument.getString("token");
                                                        sendNotification(token, Nama, Kta);
                                                    }
                                                } else {
                                                }
                                            } else {
                                            }
                                        });
                            }
                        } else {
                        }
                    } else {
                    }
                });
    }
    private void sendNotification(String token, String Nama, String Kta) {
        String title = "Ada Pengguna baru daftar";
        String teks = "Anda telah menerima pesanan baru dengan nomor ";
        String body = "Pengguna baru dengan nomor KTA: " + Kta + " bernama: " + Nama; // Sesuaikan dengan pesan notifikasi yang sesuai

        FCMSender.sendNotificationSignup(SignupActivity.this, token, title, teks, body);
    }

    private void showDatePickerDialog() {
        // Create a MaterialDatePicker instance with date constraints
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();

        // Set the title of the picker dialog
        builder.setTitleText("Pilih Tanggal");

        // Set the initial date to the current date
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());

        // Create the MaterialDatePicker instance
        final MaterialDatePicker<Long> datePicker = builder.build();

        // Set the listener to handle the date selection
        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Convert the selected date from milliseconds to Calendar
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Format and set the selected date
            String selectedDate = day + "/" + (month + 1) + "/" + year;
            ttTanggal.setText(selectedDate);
        });

        // Show the date picker dialog
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            // Jika tombol back sudah ditekan sebelumnya, langsung kembali ke LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            // Jika tombol back baru ditekan sekali, tampilkan pesan toast
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Jika Anda keluar dari halaman pendaftaran, maka secara otomatis proses pendaftaran gagal.", Toast.LENGTH_SHORT).show();

            // Reset status setelah 2 detik
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        resetForm();
    }
    private void resetForm() {

        verificationCode = null;

        etNama.setText("");
        etKta.setText("");
        etTtl.setText("");
        etAlamat.setText("");
        etPangkalan.setText("");
        etGudep.setText("");
        etAmbalan.setText("");
        etNotelp.setText("");
        etEmail.setText("");
        etVerificationCode.setText("");
        spinnerJenisKelamin.setSelection(0);
        ttTanggal.setText("Tanggal belum dipilih");
    }

}

