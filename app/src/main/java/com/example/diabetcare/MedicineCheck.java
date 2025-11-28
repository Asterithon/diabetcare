package com.example.diabetcare;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MedicineCheck extends AppCompatActivity {

    Button btnYes, btnNo;
    int alarmId, jadwalJam, jadwalMenit;
    TextView txtJadwal;
    DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medicine_check);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        alarmId = getIntent().getIntExtra("alarm_id", -1);
        jadwalJam = getIntent().getIntExtra("jadwal_jam", -1);
        jadwalMenit = getIntent().getIntExtra("jadwal_menit", -1);

        txtJadwal = findViewById(R.id.txt_jadwal);
        txtJadwal.setText("id :" + alarmId + " Jadwal: " + String.format("%02d:%02d", jadwalJam, jadwalMenit));

        btnYes = findViewById(R.id.btn_yes);
        btnNo = findViewById(R.id.btn_no);
        dbHelper = new DbHelper(this);

        btnYes.setOnClickListener(v -> showConfirmationDialog("Apakah yakin anda sudah minum obat?", "Sudah"));
        btnNo.setOnClickListener(v -> showConfirmationDialog("Apakah yakin anda memilih belum minum obat?", "Belum"));
    }

    private void showConfirmationDialog(String message, String status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi");
        builder.setMessage(message + "\n\n-anda tidak bisa merubah pilihan anda-");

        builder.setPositiveButton("Konfirmasi", (dialog, which) -> {
            simpanRiwayat(status);
        });

        builder.setNegativeButton("Batal", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }

    private void simpanRiwayat(String status) {
        // ambil tanggal jadwal dari intent
        String tanggalJadwal = getIntent().getStringExtra("tanggal_jadwal");

        // buat Calendar target dari jam alarm
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, jadwalJam);
        target.set(Calendar.MINUTE, jadwalMenit);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        long jadwalMillis = target.getTimeInMillis();

        // panggil DbHelper dengan validasi window
        dbHelper.insertOrUpdateRiwayat(alarmId, tanggalJadwal, jadwalMillis, status);

        Toast.makeText(this, "Riwayat disimpan", Toast.LENGTH_SHORT).show();
        finish();
    }
}
