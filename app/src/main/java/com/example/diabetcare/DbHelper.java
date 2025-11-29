package com.example.diabetcare;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "alarm.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_NAME = "alarms";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_HOUR = "hour";
    public static final String COLUMN_MINUTE = "minute";
    public static final String COLUMN_KETERANGAN = "keterangan";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_RIWAYAT_TABLE = "CREATE TABLE riwayat (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_alarm INTEGER," +
                "tanggal_jadwal TEXT," +
                "tanggal TEXT," +
                "status TEXT," +
                "waktu_konfirmasi TEXT," +
                "FOREIGN KEY(id_alarm) REFERENCES alarms(id))";
        db.execSQL(CREATE_RIWAYAT_TABLE);

        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY," +
                COLUMN_HOUR + " INTEGER," +
                COLUMN_MINUTE + " INTEGER," +
                "keterangan TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS riwayat");
        onCreate(db);
    }


    public void insertAlarm(AlarmModel alarm) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, alarm.id);
        values.put(COLUMN_HOUR, alarm.hour);
        values.put(COLUMN_MINUTE, alarm.minute);
        values.put("keterangan", alarm.keterangan);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<AlarmModel> getAllAlarms() {
        List<AlarmModel> alarms = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                int hour = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HOUR));
                int minute = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MINUTE));
                String keterangan = cursor.getString(cursor.getColumnIndexOrThrow("keterangan"));
                alarms.add(new AlarmModel(id, hour, minute, keterangan));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return alarms;
    }

    public List<Integer> getAllAlarmIds() {
        List<Integer> ids = new ArrayList<>();
        for (AlarmModel alarm : getAllAlarms()) {
            ids.add(alarm.id);
        }
        return ids;
    }

    public long getAlarmTimeMillis(int alarmId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT hour, minute FROM alarms WHERE id = ?", new String[]{String.valueOf(alarmId)});
        long millis = 0;

        if (cursor.moveToFirst()) {
            int hour = cursor.getInt(cursor.getColumnIndexOrThrow("hour"));
            int minute = cursor.getInt(cursor.getColumnIndexOrThrow("minute"));

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            millis = cal.getTimeInMillis();
        }

        cursor.close();
        db.close();
        return millis;
    }

    public List<Integer> getRiwayatIdsForDate(String tanggal) {
        List<Integer> riwayatIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id_alarm FROM riwayat WHERE tanggal_jadwal = ?", new String[]{tanggal});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_alarm"));
                riwayatIds.add(id);
            }
            cursor.close();
        }

        return riwayatIds;
    }

    public void updateAlarm(AlarmModel alarm) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HOUR, alarm.hour);
        values.put(COLUMN_MINUTE, alarm.minute);
        values.put("keterangan", alarm.keterangan);
        db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(alarm.id)});
        db.close();
    }

    public void deleteAlarm(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public boolean hasRespondedToday(int alarmId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM riwayat WHERE id_alarm = ? AND tanggal_jadwal = ? AND status = ?",
                new String[]{String.valueOf(alarmId), today, "Sudah"}
        );

        boolean responded = false;
        if (cursor.moveToFirst()) {
            responded = cursor.getInt(0) > 0;
        }

        cursor.close();
        db.close();
        return responded;
    }

    public List<HistoryModel> getHistoryGroupedByDate() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<HistoryModel> result = new ArrayList<>();

        Cursor cursor = db.rawQuery(
                "SELECT tanggal, id_alarm, status, waktu_konfirmasi FROM riwayat ORDER BY tanggal DESC, waktu_konfirmasi ASC",
                null
        );

        Map<String, HistoryModel> map = new LinkedHashMap<>();

        if (cursor.moveToFirst()) {
            do {
                String tanggal = cursor.getString(0);
                int idAlarm = cursor.getInt(1);
                String status = cursor.getString(2);
                String waktu = cursor.getString(3);

                if (!map.containsKey(tanggal)) {
                    map.put(tanggal, new HistoryModel(tanggal));
                }

                String detail = "Jadwal " + idAlarm + " → " + status + ", " + waktu;
                map.get(tanggal).responList.add(detail);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        result.addAll(map.values());
        return result;
    }

    public int getTotalAlarmsForDate(String tanggalJadwal) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM riwayat WHERE tanggal_jadwal = ?",
                new String[]{tanggalJadwal}
        );
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    public int getRespondedForDate(String tanggalJadwal) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM riwayat WHERE tanggal_jadwal = ? AND status = ?",
                new String[]{tanggalJadwal, "Sudah"}
        );
        int responded = 0;
        if (cursor.moveToFirst()) {
            responded = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return responded;
    }

    public int getComplianceRate(String tanggalJadwal) {
        int totalAlarms = getTotalAlarmsForDate(tanggalJadwal);
        int responded = getRespondedForDate(tanggalJadwal);

        if (totalAlarms == 0) return 0;
        float rate = (responded * 100f) / totalAlarms;
        return Math.round(rate); // pembulatan ke persen terdekat
    }

    public void insertOrUpdateRiwayat(int alarmId, String tanggalJadwal, long jadwalMillis, String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        long konfirmasiMillis = System.currentTimeMillis();
        long startWindow = jadwalMillis - (60 * 60 * 1000); // 1 jam sebelum
        long endWindow   = jadwalMillis + (60 * 60 * 1000); // 1 jam sesudah

        String waktuKonfirmasi = new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                .format(new Date(konfirmasiMillis));
        String tanggal = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        ContentValues values = new ContentValues();
        values.put("id_alarm", alarmId);
        values.put("tanggal_jadwal", tanggalJadwal);
        values.put("tanggal", tanggal);
        values.put("waktu_konfirmasi", waktuKonfirmasi);

        if (konfirmasiMillis >= startWindow && konfirmasiMillis <= endWindow) {
            values.put("status", status); // "Sudah" atau "Belum"
        } else {
            values.put("status", "Diluar jadwal");
        }

        // cek apakah sudah ada row
        Cursor cursor = db.rawQuery("SELECT id FROM riwayat WHERE id_alarm=? AND tanggal_jadwal=?",
                new String[]{String.valueOf(alarmId), tanggalJadwal});

        if (cursor.moveToFirst()) {
            // update
            db.update("riwayat", values, "id_alarm=? AND tanggal_jadwal=?",
                    new String[]{String.valueOf(alarmId), tanggalJadwal});
        } else {
            // insert baru
            db.insert("riwayat", null, values);
        }
        cursor.close();
        db.close();
    }

    public void createDailyRiwayat() {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        List<AlarmModel> alarms = getAllAlarms();
        for (AlarmModel alarm : alarms) {
            Cursor cursor = db.rawQuery(
                    "SELECT id FROM riwayat WHERE id_alarm=? AND tanggal_jadwal=?",
                    new String[]{String.valueOf(alarm.id), today}
            );
            boolean exists = cursor.moveToFirst();
            cursor.close();

            if (!exists) {
                ContentValues values = new ContentValues();
                values.put("id_alarm", alarm.id);
                values.put("tanggal_jadwal", today);
                values.put("tanggal", today);
                values.put("status", "Belum");
                values.put("waktu_konfirmasi", "");
                db.insert("riwayat", null, values);
            }
        }
        db.close();
    }

    public String getLastRiwayatDate() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT MAX(tanggal_jadwal) FROM riwayat", null
        );
        String lastDate = null;
        if (cursor.moveToFirst()) {
            lastDate = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return lastDate;
    }

    public void createRiwayatForDate(String tanggal) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<AlarmModel> alarms = getAllAlarms();
        for (AlarmModel alarm : alarms) {
            Cursor cursor = db.rawQuery(
                    "SELECT id FROM riwayat WHERE id_alarm=? AND tanggal_jadwal=?",
                    new String[]{String.valueOf(alarm.id), tanggal}
            );
            boolean exists = cursor.moveToFirst();
            cursor.close();

            if (!exists) {
                ContentValues values = new ContentValues();
                values.put("id_alarm", alarm.id);
                values.put("tanggal_jadwal", tanggal);
                values.put("tanggal", tanggal);
                values.put("status", "Belum");
                values.put("waktu_konfirmasi", "");
                db.insert("riwayat", null, values);
            }
        }
        db.close();
    }

    public int getComplianceLast7Days() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) as total, " +
                        "SUM(CASE WHEN status = 'Sudah' THEN 1 ELSE 0 END) as sudah " +
                        "FROM riwayat " +
                        "WHERE tanggal_jadwal BETWEEN date('now', '-6 days') AND date('now')",
                null
        );

        int percent = 0;
        if (cursor.moveToFirst()) {
            int total = cursor.getInt(cursor.getColumnIndexOrThrow("total"));
            int sudah = cursor.getInt(cursor.getColumnIndexOrThrow("sudah"));
            if (total > 0) {
                percent = (int) ((sudah * 100.0) / total);
            }
        }

        cursor.close();
        db.close();
        return percent;
    }

    public void insertRiwayat(int alarmId, String tanggalJadwal, String status, long jadwalMillis) {
        SQLiteDatabase db = this.getWritableDatabase();

        String waktuKonfirmasi = ""; // kosong karena belum dikonfirmasi
        String tanggal = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        ContentValues values = new ContentValues();
        values.put("id_alarm", alarmId);
        values.put("tanggal_jadwal", tanggalJadwal);
        values.put("tanggal", tanggal);
        values.put("status", status); // biasanya "Belum"
        values.put("waktu_konfirmasi", waktuKonfirmasi);

        db.insert("riwayat", null, values);
        db.close();
    }

    public void deleteRiwayat(int alarmId, String tanggalJadwal) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("riwayat", "id_alarm=? AND tanggal_jadwal=?", new String[]{
                String.valueOf(alarmId), tanggalJadwal
        });
        db.close();
    }


    public void syncRiwayatForToday() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        List<Integer> activeAlarmIds = getAllAlarmIds(); // ambil semua alarm aktif
        List<Integer> riwayatIds = getRiwayatIdsForDate(today); // ambil semua id_alarm di riwayat hari ini

        // Tambahkan entri baru untuk alarm yang belum tercatat
        for (int id : activeAlarmIds) {
            if (!riwayatIds.contains(id)) {
                insertRiwayat(id, today, "Belum", getAlarmTimeMillis(id));
            }
        }

        // Hapus entri riwayat yang tidak lagi relevan
        for (int id : riwayatIds) {
            if (!activeAlarmIds.contains(id)) {
                deleteRiwayat(id, today);
            }
        }
    }

    public void fillMissingRiwayatUntilToday() {
        String lastDate = getLastRiwayatDate();
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date today = sdf.parse(todayStr);
            Calendar cal = Calendar.getInstance();

            if (lastDate != null) {
                cal.setTime(sdf.parse(lastDate));
                cal.add(Calendar.DATE, 1);

                while (!cal.getTime().after(today)) { // berhenti kalau sudah lewat hari ini
                    String gapDate = sdf.format(cal.getTime());
                    createRiwayatForDate(gapDate);
                    cal.add(Calendar.DATE, 1);
                }
            } else {
                // kalau belum ada riwayat sama sekali
                createRiwayatForDate(todayStr);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
