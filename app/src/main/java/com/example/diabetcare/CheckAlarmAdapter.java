package com.example.diabetcare;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CheckAlarmAdapter extends RecyclerView.Adapter<CheckAlarmAdapter.CheckViewHolder> {
    private List<AlarmModel> alarms;
    private Context context;
    private DbHelper dbHelper;

    // konstanta window validasi (1 jam)
    private static final long WINDOW_MILLIS = 1 * 60 * 60 * 1000;

    public CheckAlarmAdapter(Context context, List<AlarmModel> alarms, DbHelper dbHelper) {
        this.context = context;
        this.alarms = alarms;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public CheckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_check_alarm, parent, false);
        return new CheckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckViewHolder holder, int position) {
        AlarmModel alarm = alarms.get(position);
        boolean inWindow = isWithinWindow(alarm.hour, alarm.minute);
        boolean hasResponded = dbHelper.hasRespondedToday(alarm.id);

        String jamText = String.format("%02d:%02d", alarm.hour, alarm.minute);

        if (hasResponded) {
            holder.btnCheck.setEnabled(false);
            holder.btnCheck.setText("Sudah dikonfirmasi (" + jamText + ")");
            holder.btnCheck.setOnClickListener(null);
            holder.btnCheck.setBackgroundResource(R.drawable.btn_confirmed);
            holder.btnCheck.setTextColor(ContextCompat.getColor(context, R.color.black));

        } else if (inWindow) {
            holder.btnCheck.setEnabled(true);
            holder.btnCheck.setText("konfirmasi Obat (" + jamText + ")");
            holder.btnCheck.setOnClickListener(v -> {
                Intent intent = new Intent(context, MedicineCheck.class);
                intent.putExtra("alarm_id", alarm.id);
                intent.putExtra("jadwal_jam", alarm.hour);
                intent.putExtra("jadwal_menit", alarm.minute);
                intent.putExtra("keterangan", alarm.keterangan);


                // kirim juga tanggal_jadwal sesuai jam alarm
                Calendar target = Calendar.getInstance();
                target.set(Calendar.HOUR_OF_DAY, alarm.hour);
                target.set(Calendar.MINUTE, alarm.minute);
                target.set(Calendar.SECOND, 0);
                target.set(Calendar.MILLISECOND, 0);

                String tanggalJadwal = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(target.getTime());
                intent.putExtra("tanggal_jadwal", tanggalJadwal);

                context.startActivity(intent);
            });
        } else {
            holder.btnCheck.setEnabled(false);
            holder.btnCheck.setText("Diluar waktu cek (" + jamText + ")");
            holder.btnCheck.setBackgroundResource(R.drawable.btn_disabled);
            holder.btnCheck.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    public static class CheckViewHolder extends RecyclerView.ViewHolder {
        Button btnCheck;

        public CheckViewHolder(@NonNull View itemView) {
            super(itemView);
            btnCheck = itemView.findViewById(R.id.btnCheck);
        }
    }

    private boolean isWithinWindow(int hour, int minute) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, hour);
        target.set(Calendar.MINUTE, minute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        long diff = Math.abs(now.getTimeInMillis() - target.getTimeInMillis());
        return diff <= WINDOW_MILLIS;
    }
}
