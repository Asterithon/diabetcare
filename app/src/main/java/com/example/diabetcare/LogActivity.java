package com.example.diabetcare;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.diabetcare.databinding.ActivityLogBinding;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LogActivity extends AppCompatActivity {

    private ActivityLogBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        DbHelper dbHelper = new DbHelper(this);

        TextView compliance7Days = findViewById(R.id.compliance7Days);
        int percent7Days = dbHelper.getComplianceLast7Days();
        compliance7Days.setText("7 hari terakhir: " + percent7Days + "%");

        // ✅ ambil data kepatuhan untuk chart
        List<BarEntry> barEntries = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        int totalAlarms = dbHelper.getAllAlarms().size();
        if (totalAlarms == 0) {
            Toast.makeText(this, "Tidak ada alarm, chart kosong", Toast.LENGTH_SHORT).show();
        }

        for (int i = 0; i < 7; i++) {
            String tanggal = sdf.format(cal.getTime());
            int rate = dbHelper.getComplianceRate(tanggal);
            barEntries.add(new BarEntry(i, rate));
            cal.add(Calendar.DATE, -1);
        }

        // ✅ panggil dengan parameter
        setupBarChart(barEntries);

        // ✅ setup list history
        ListView listView = findViewById(R.id.list_of_history);
        List<HistoryModel> history = dbHelper.getHistoryGroupedByDate();
        HistoryAdapter adapter = new HistoryAdapter(this, history);
        listView.setAdapter(adapter);
    }

    private void setupBarChart(List<BarEntry> barEntries) {
        BarDataSet dataSet = new BarDataSet(barEntries, "Kepatuhan Harian (%)");
        dataSet.setColor(getResources().getColor(android.R.color.holo_red_dark));
        BarData data = new BarData(dataSet);
        binding.barChart.setData(data);
        binding.barChart.invalidate();
    }
}