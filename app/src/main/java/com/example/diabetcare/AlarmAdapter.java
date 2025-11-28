package com.example.diabetcare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private List<AlarmModel> alarms;
    private Context context;
    private OnAlarmActionListener listener;

    public interface OnAlarmActionListener {
        void onEdit(AlarmModel alarm);
        void onDelete(AlarmModel alarm);
    }

    public AlarmAdapter(Context context, List<AlarmModel> alarms, OnAlarmActionListener listener) {
        this.context = context;
        this.alarms = alarms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        AlarmModel alarm = alarms.get(position);
        holder.txtJadwal.setText(String.format("%02d:%02d", alarm.hour, alarm.minute));
        holder.txtKeterangan.setText(alarm.keterangan);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(alarm));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(alarm));
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView txtJadwal, txtKeterangan;
        Button btnEdit, btnDelete;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            txtJadwal = itemView.findViewById(R.id.txtJadwal);
            txtKeterangan = itemView.findViewById(R.id.txtKeterangan);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
