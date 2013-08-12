package com.jewelzqiu.when;

import android.content.Context;
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
    public static final String COLUMN_SECOND = "second";
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
        int[] trigger_values = mContext.getResources().getIntArray(R.array.triggers_values);
        for (int i = 0; i < trigger_values.length; i++) {
            int trigger = trigger_values[i];
            if (trigger == 0) { // time events
                db.execSQL("CREATE TABLE " + TABLE_NAME_PREFIX + trigger + "(" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_MINUTE + " INTEGER, " +
                        COLUMN_SECOND + " INTEGER, " +
                        COLUMN_ACTION + " INTEGER, " +
                        COLUMN_REPEAT + " INTEGER, " +
                        COLUMN_ENABLED + " INTEGER)"
                );
            } else {
                db.execSQL("CREATE TABLE " + TABLE_NAME_PREFIX + trigger + "(" +
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
}
