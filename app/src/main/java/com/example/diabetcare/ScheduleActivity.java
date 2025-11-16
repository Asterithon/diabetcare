package com.example.diabetcare;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;
import java.util.List;

@RequiresApi(Build.VERSION_CODES.O)

public class ScheduleActivity extends AppCompatActivity {

     Button setReminder;

    private int jam1 = -1, menit1 = -1;
    private int jam2 = -1, menit2 = -1;


    TextView waktu1,waktu2;

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
        DbHelper dbHelper = new DbHelper(this);
        List<AlarmModel> savedAlarms = dbHelper.getAllAlarms();
        waktu1 = findViewById(R.id.waktu1);
        waktu2 = findViewById(R.id.waktu2);
        for (AlarmModel alarm : savedAlarms) {
            if (alarm.id == 1) {
                jam1 = alarm.hour;
                menit1 = alarm.minute;
                waktu1.setText(String.format("%02d:%02d", jam1, menit1));
            } else if (alarm.id == 2) {
                jam2 = alarm.hour;
                menit2 = alarm.minute;
                waktu2.setText(String.format("%02d:%02d", jam2, menit2));
            }
        }


        setReminder = findViewById(R.id.set_reminder);

        waktu1.setOnClickListener(v -> showTimePicker(1));
        waktu2.setOnClickListener(v -> showTimePicker(2));

        setReminder.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Izin notifikasi belum diberikan. Jadwal tidak bisa diatur.", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            if (jam1 != -1 && menit1 != -1) {
                setTimer(jam1, menit1, 1);
                dbHelper.updateAlarm(new AlarmModel(1, jam1, menit1));
            }
            if (jam2 != -1 && menit2 != -1) {
                setTimer(jam2, menit2, 2);
                dbHelper.updateAlarm(new AlarmModel(2, jam2, menit2));
            }
            dbHelper.initializeDefaultAlarms();
            loadAlarmsFromDatabase(); // perbarui tampilan
            Toast.makeText(this, "Alarm diperbarui", Toast.LENGTH_SHORT).show();
        });
    }


    private void loadAlarmsFromDatabase() {
        DbHelper dbHelper = new DbHelper(this);
        List<AlarmModel> savedAlarms = dbHelper.getAllAlarms();

        for (AlarmModel alarm : savedAlarms) {
            if (alarm.id == 1) {
                jam1 = alarm.hour;
                menit1 = alarm.minute;
                waktu1.setText(String.format("%02d:%02d", jam1, menit1));
            } else if (alarm.id == 2) {
                jam2 = alarm.hour;
                menit2 = alarm.minute;
                waktu2.setText(String.format("%02d:%02d", jam2, menit2));
            }
        }
    }

    private void showTimePicker(int id) {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth,
                (view, selectedHour, selectedMinute) -> {
                    if (id == 1) {
                        jam1 = selectedHour;
                        menit1 = selectedMinute;
                        waktu1.setText(String.format("%02d:%02d", jam1, menit1));
                    } else {
                        jam2 = selectedHour;
                        menit2 = selectedMinute;
                        waktu2.setText(String.format("%02d:%02d", jam2, menit2));
                    }
                },
                hour,
                minute,
                true
        );
        dialog.show();
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private void setTimer(int jam, int menit, int requestCode) {
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
        i.setAction("com.example.diabetcare.ALARM_" + requestCode); // ini kunci penting

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                ScheduleActivity.this,
                requestCode,
                i,
                PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                cal_alarm.getTimeInMillis(),
                pendingIntent
        );
    }
}
