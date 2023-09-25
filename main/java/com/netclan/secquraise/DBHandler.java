package com.netclan.secquraise;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "user";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "data";

    private final String CONNECTIVITY_COL = "connectivity";
    private final String BATTERY_CHARGING_COL = "batteryCharging";
    private  final String BATTERY_CHARGE_COL = "batteryCharge";
    private final String LOCATION_COL = "location";
    private final String TIME_STAMP_COL = "timestamp";


    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + CONNECTIVITY_COL + " TEXT,"
                + BATTERY_CHARGING_COL + " TEXT,"
                + BATTERY_CHARGE_COL + " TEXT,"
                + LOCATION_COL + " TEXT,"
                + TIME_STAMP_COL + " TEXT)";

        db.execSQL(query);
    }

    public void addNewData(String connectivity, String batteryCharging, String batteryCharge, String location, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(CONNECTIVITY_COL, connectivity);
        values.put(BATTERY_CHARGING_COL, batteryCharging);
        values.put(BATTERY_CHARGE_COL, batteryCharge);
        values.put(LOCATION_COL, location);
        values.put(TIME_STAMP_COL, timestamp);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public ArrayList<Data> readData() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor
                = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        ArrayList<Data> arrayList = new ArrayList<>();

        if(cursor.moveToFirst()) {
            do {
                arrayList.add(new Data(
                        cursor.getString(cursor.getColumnIndex(CONNECTIVITY_COL)),
                        cursor.getString(cursor.getColumnIndex(BATTERY_CHARGING_COL)),
                        cursor.getString(cursor.getColumnIndex(BATTERY_CHARGE_COL)),
                        cursor.getString(cursor.getColumnIndex(LOCATION_COL)),
                        cursor.getString(cursor.getColumnIndex(TIME_STAMP_COL))));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return arrayList;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void removeData() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }
}

