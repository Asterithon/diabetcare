package com.example.diabetcare;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class ScheduleActivity extends AppCompatActivity {

    TextView time1, time2;

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
        time1 = findViewById(R.id.time1);
        time2 = findViewById(R.id.time2);

        time1.setOnClickListener(v -> showTimePicker(time1));
        time2.setOnClickListener(v -> showTimePicker(time2));

    }
    private void showTimePicker(TextView targetView) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(ScheduleActivity.this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                (view, selectedHour, selectedMinute) -> {
                    String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                    targetView.setText(time);
                },
                hour, minute, true); // true = 24 jam

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // transparan seperti alarm
        dialog.show();
    }

}