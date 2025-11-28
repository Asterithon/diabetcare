package com.example.diabetcare;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@RequiresApi(Build.VERSION_CODES.O)

public class ScheduleActivity extends AppCompatActivity {

    private RecyclerView recyclerAlarms;
    private AlarmAdapter adapter;
    private List<AlarmModel> alarmList = new ArrayList<>();
    private DbHelper dbHelper;


    @RequiresApi(33)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DbHelper(this);

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> {
            if (alarmList.size() >= 5) {
                Toast.makeText(this, "Maksimal 5 jadwal", Toast.LENGTH_SHORT).show();
                return;
            }
            showAddDialog();
        });

        recyclerAlarms = findViewById(R.id.recyclerAlarms);
        alarmList = dbHelper.getAllAlarms();

        adapter = new AlarmAdapter(this, alarmList, new AlarmAdapter.OnAlarmActionListener() {
            @Override
            public void onEdit(AlarmModel alarm) {
                showEditDialog(alarm);
            }

            @Override
            public void onDelete(AlarmModel alarm) {
                if (alarmList.size() <= 1) {
                    Toast.makeText(ScheduleActivity.this, "Minimal 1 jadwal harus ada", Toast.LENGTH_SHORT).show();
                    return;
                }

                dbHelper.deleteAlarm(alarm.id);
                cancelAlarm(alarm.id);
                loadAlarms();
            }
        });

        recyclerAlarms.setLayoutManager(new LinearLayoutManager(this));
        recyclerAlarms.setAdapter(adapter);
    }

    private boolean isDuplicateTime(int hour, int minute, @Nullable Integer excludeId) {
        for (AlarmModel alarm : alarmList) {
            if (excludeId != null && alarm.id == excludeId) continue;
            if (alarm.hour == hour && alarm.minute == minute) return true;
        }
        return false;
    }

    private void showEditDialog(AlarmModel alarm) {
        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    showKeteranganDialog(selectedHour, selectedMinute, alarm);
                },
                alarm.hour,
                alarm.minute,
                true
        );
        timePicker.show();
    }

    private int generateNewId() {
        int maxId = 0;
        for (AlarmModel alarm : alarmList) {
            if (alarm.id > maxId) maxId = alarm.id;
        }
        return maxId + 1;
    }

    private void showKeteranganDialog(int hour, int minute, @Nullable AlarmModel toEdit) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_input, null);
        EditText input = dialogView.findViewById(R.id.input_edit_text);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(toEdit == null ? "Tambah Jadwal" : "Edit Jadwal")
                .setView(dialogView)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String keterangan = input.getText().toString().trim();
                    if (keterangan.isEmpty()) {
                        Toast.makeText(this, "Keterangan tidak boleh kosong", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (isDuplicateTime(hour, minute, toEdit != null ? toEdit.id : null)) {
                        Toast.makeText(this, "Jadwal dengan jam yang sama sudah ada", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (toEdit == null) {
                        int newId = generateNewId();
                        AlarmModel newAlarm = new AlarmModel(newId, hour, minute, keterangan);
                        dbHelper.insertAlarm(newAlarm);
                        setTimer(hour, minute, newId, keterangan);
                        dbHelper.createDailyRiwayat();
                    } else {
                        AlarmModel updated = new AlarmModel(toEdit.id, hour, minute, keterangan);
                        dbHelper.updateAlarm(updated);
                        setTimer(hour, minute, updated.id, keterangan);
                        dbHelper.createDailyRiwayat();
                    }
                    loadAlarms();
                })
                .setNegativeButton("Batal", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.primary));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.primary));
    }



    private void showAddDialog() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    showKeteranganDialog(selectedHour, selectedMinute, null); // null artinya tambah baru
                },
                hour,
                minute,
                true
        );
        timePicker.show();
    }

    private void loadAlarms() {
        alarmList.clear();
        alarmList.addAll(dbHelper.getAllAlarms());
        adapter.notifyDataSetChanged();
    }

    private void cancelAlarm(int alarmId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction("com.example.diabetcare.ALARM_" + alarmId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                alarmId,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        alarmManager.cancel(pendingIntent);
    }



    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private void setTimer(int jam, int menit, int requestCode, String keterangan) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar cal_alarm = Calendar.getInstance();
        cal_alarm.set(Calendar.HOUR_OF_DAY, jam);
        cal_alarm.set(Calendar.MINUTE, menit);
        cal_alarm.set(Calendar.SECOND, 0);
        cal_alarm.set(Calendar.MILLISECOND, 0);

        if (cal_alarm.before(Calendar.getInstance())) {
            cal_alarm.add(Calendar.DATE, 1);
        }

        Intent i = new Intent(ScheduleActivity.this, AlarmReceiver.class);
        i.putExtra("alarm_id", requestCode);
        i.putExtra("jadwal_jam", jam);
        i.putExtra("jadwal_menit", menit);
        i.putExtra("keterangan", keterangan);
        i.setAction("com.example.diabetcare.ALARM_" + requestCode);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                ScheduleActivity.this,
                requestCode,
                i,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                cal_alarm.getTimeInMillis(),
                pendingIntent
        );
    }
}
