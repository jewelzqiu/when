package com.jewelzqiu.when;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jewelzqiu on 8/9/13.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "data";
    public static final String TABLE_NAME_PREFIX = "t_";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MINUTE = "minute";
    public static final String COLUMN_HOUR = "hour";
    public static final String COLUMN_REPEAT = "repeat";
    public static final String COLUMN_ACTION = "action";
    public static final String COLUMN_ENABLED = "enabled";

    private Context mContext;

    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String[] trigger_values = mContext.getResources().getStringArray(R.array.triggers_values);
        for (int i = 0; i < trigger_values.length; i++) {
            int trigger = Integer.parseInt(trigger_values[i]);
            System.out.println(trigger);
            if (trigger == 0) { // time events
                System.out.println("time");
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME_PREFIX + trigger + "(" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_HOUR + " INTEGER, " +
                        COLUMN_MINUTE + " INTEGER, " +
                        COLUMN_ACTION + " INTEGER, " +
                        COLUMN_REPEAT + " INTEGER, " +
                        COLUMN_ENABLED + " INTEGER)"
                );
            } else {
                System.out.println("event");
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME_PREFIX + trigger + "(" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_ACTION + " INTEGER, " +
                        COLUMN_ENABLED + " INTEGER)"
                );
            }
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addTimeEvent(int hour, int minute, int action, int repeat_mask) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO " + TABLE_NAME_PREFIX + 0 +
                " (" + COLUMN_HOUR + ", " + COLUMN_MINUTE + ", " + COLUMN_ACTION + ", " +
                        COLUMN_REPEAT + ", " + COLUMN_ENABLED + ") VALUES " +
                "(" + hour + ", " + minute + ", " + action + ", " + repeat_mask + ", 1)"
        );
    }

    public void addEvent(int event_type, int action) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO " + TABLE_NAME_PREFIX + event_type +
                " (" + COLUMN_ACTION + ", " + COLUMN_ENABLED + ") VALUES " +
                "(" + action + ", 1)"
        );
    }

    public Cursor query(int event_type) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME_PREFIX + event_type, null);
    }

    public void setEnabled(int event_type, int id, boolean enabled) {
        SQLiteDatabase db = getWritableDatabase();
        int enable = enabled ? 1 : 0;
        db.execSQL("UPDATE " + TABLE_NAME_PREFIX + event_type +
                " SET " + COLUMN_ENABLED + " = " + enable +
                " WHERE " + COLUMN_ID + " = " + id);
    }

    public void updateEvent(int event_type, int id, int action) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_NAME_PREFIX + event_type +
                " SET " + COLUMN_ACTION + " = " + action +
                " WHERE " + COLUMN_ID + " = " + id);
    }

    public void updateTimeEvent(
            int event_type, int id, int action, int hour, int minute, int repeat) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_NAME_PREFIX + event_type +
                " SET " + COLUMN_ACTION + " = " + action + ", " +
                COLUMN_HOUR + " = " + hour + ", " +
                COLUMN_MINUTE + " = " + minute + ", " +
                COLUMN_REPEAT + " = " + repeat +
                " WHERE " + COLUMN_ID + " = " + id);
    }

    public void deleteEvent(int event_type, int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME_PREFIX + event_type +
                " WHERE " + COLUMN_ID + " = " + id);
    }

    public Cursor queryByID(int event_type, int id) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME_PREFIX + event_type +
                " WHERE " + COLUMN_ID + " = " + id, null);
    }
}
