package com.example.diabetcare;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "alarm.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "alarms";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_HOUR = "hour";
    public static final String COLUMN_MINUTE = "minute";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY," +
                COLUMN_HOUR + " INTEGER," +
                COLUMN_MINUTE + " INTEGER)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertAlarm(AlarmModel alarm) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, alarm.id);
        values.put(COLUMN_HOUR, alarm.hour);
        values.put(COLUMN_MINUTE, alarm.minute);
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
                alarms.add(new AlarmModel(id, hour, minute));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return alarms;
    }

    public void updateAlarm(AlarmModel alarm) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HOUR, alarm.hour);
        values.put(COLUMN_MINUTE, alarm.minute);
        db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(alarm.id)});
        db.close();
    }

    public void initializeDefaultAlarms() {
        List<AlarmModel> existing = getAllAlarms();
        if (existing.isEmpty()) {
            insertAlarm(new AlarmModel(1, 8, 0));  // default jam 08:00
            insertAlarm(new AlarmModel(2, 20, 0)); // default jam 20:00
        }
    }
}
