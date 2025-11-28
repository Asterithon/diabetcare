package com.example.diabetcare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

        private Button btnSchedule, btnLog;
        private RecyclerView recyclerCheck;
        private DbHelper dbHelper;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_main);

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            btnSchedule = findViewById(R.id.btn_schedule);
            btnLog = findViewById(R.id.btn_log);
            recyclerCheck = findViewById(R.id.recyclerCheck);
            dbHelper = new DbHelper(this);
            dbHelper.createDailyRiwayat();
            btnSchedule.setOnClickListener(v -> schedule());
            btnLog.setOnClickListener(v -> medicineLog());

            // ✅ tampilkan daftar alarm di RecyclerView
            List<AlarmModel> alarms = dbHelper.getAllAlarms();
            recyclerCheck.setLayoutManager(new LinearLayoutManager(this));
            recyclerCheck.setAdapter(new CheckAlarmAdapter(this, alarms, dbHelper));

            String lastDate = dbHelper.getLastRiwayatDate();
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            if (lastDate != null) {
                Calendar cal = Calendar.getInstance();
                try {
                    cal.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(lastDate));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                cal.add(Calendar.DATE, 1);

                while (true) {
                    try {
                        if (!cal.getTime().before(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(today)))
                            break;
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    String gapDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
                    dbHelper.createRiwayatForDate(gapDate);
                    cal.add(Calendar.DATE, 1);
                }
            } else {
                // kalau belum ada riwayat sama sekali, buat untuk hari ini
                dbHelper.createRiwayatForDate(today);
            }

        }

        @Override
        protected void onResume() {
            super.onResume();
            List<AlarmModel> alarms = dbHelper.getAllAlarms();
            recyclerCheck.setAdapter(new CheckAlarmAdapter(this, alarms, dbHelper));
        }

        private void medicineLog() {
            Intent goLog = new Intent(this, LogActivity.class);
            startActivity(goLog);
        }

        private void schedule() {
            Intent goSchedule = new Intent(this, ScheduleActivity.class);
            startActivity(goSchedule);
        }
    }