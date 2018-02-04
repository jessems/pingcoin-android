package com.example.android.pingcoin2;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.pingcoin2.CoinContract;

/**
 * Created by jmscdch on 03/02/18.
 */

public class PingcoinDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "coins.db";
    private static final int DB_VERSION = 1;

    PingcoinDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_COINS_TABLE = "CREATE TABLE " +
                CoinContract.CoinEntry.TABLE_NAME + " (" +
                CoinContract.CoinEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CoinContract.CoinEntry.COLUMN_FULL_NAME + " TEXT NOT NULL, " +
                CoinContract.CoinEntry.COLUMN_FAMILY_NAME + " TEXT NOT NULL, " +
                CoinContract.CoinEntry.COLUMN_MATERIAL_CLASS + " TEXT NOT NULL, " +
                CoinContract.CoinEntry.COLUMN_WEIGHT_CLASS + " TEXT NOT NULL, " +
                CoinContract.CoinEntry.COLUMN_WEIGHT + " REAL, " +
                CoinContract.CoinEntry.COLUMN_DIAMETER + " REAL, " +
                CoinContract.CoinEntry.COLUMN_C0D2 + " REAL, " +
                CoinContract.CoinEntry.COLUMN_C0D3 + " REAL, " +
                CoinContract.CoinEntry.COLUMN_C0D4 + " REAL, " +
                CoinContract.CoinEntry.COLUMN_CD_ERROR + " REAL " +
                ");";

        Log.i("asd", "Before table creation");
        db.execSQL(SQL_CREATE_COINS_TABLE);
        Log.i("asd", "After table creation");

        addCoins(db, "1 oz Gold Krugerrand", "Krugerrand", "Gold", "1 oz", 31,
                36, 4403, 9798, 16675, 100);
        addCoins(db, "1 oz Gold American Eagle", "American Eagle", "Gold", "1 oz", 31,
                36, 4100, 9478, 16360, 100);
        addCoins(db, "1 oz Gold Maple Leaf", "Maple Leaf", "Gold", "1 oz", 31,
                36, 4410, 9950, 0, 100);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CoinContract.CoinEntry.TABLE_NAME);
        onCreate(db);
    }

//    private static void insertCoin(SQLiteDatabase db, String fullName, String familyName, String materialClass, String weightClass,
//                                   float weight, float diameter, float c0d2, float c0d3, float c0d4, float cd_error) {
//        ContentValues coinValues = new ContentValues();
//        coinValues.put("FULL_NAME", fullName);
//        coinValues.put("FAMILY_NAME", familyName);
//        coinValues.put("MATERIAL_CLASS", materialClass);
//        coinValues.put("WEIGHT_CLASS", weightClass);
//        coinValues.put("WEIGHT", weight);
//        coinValues.put("DIAMETER", diameter);
//        coinValues.put("C0D2", c0d2);
//        coinValues.put("C0D3", c0d3);
//        coinValues.put("C0D4", c0d4);
//        coinValues.put("CD_ERROR", cd_error);
//    }

    private long addCoins(SQLiteDatabase db, String fullName, String familyName, String materialClass, String weightClass,
                          float weight, float diameter, float c0d2, float c0d3, float c0d4, float cd_error) {
        ContentValues cv = new ContentValues();

        cv.put(CoinContract.CoinEntry.COLUMN_FULL_NAME, fullName);
        cv.put(CoinContract.CoinEntry.COLUMN_FAMILY_NAME, familyName);
        cv.put(CoinContract.CoinEntry.COLUMN_MATERIAL_CLASS, materialClass);
        cv.put(CoinContract.CoinEntry.COLUMN_WEIGHT_CLASS, weightClass);
        cv.put(CoinContract.CoinEntry.COLUMN_WEIGHT, weight);
        cv.put(CoinContract.CoinEntry.COLUMN_DIAMETER, diameter);
        cv.put(CoinContract.CoinEntry.COLUMN_C0D2, c0d2);
        cv.put(CoinContract.CoinEntry.COLUMN_C0D3, c0d3);
        cv.put(CoinContract.CoinEntry.COLUMN_C0D4, c0d4);
        cv.put(CoinContract.CoinEntry.COLUMN_CD_ERROR, cd_error);

        Log.i("addCoins", fullName + " added.");
        return db.insert(CoinContract.CoinEntry.TABLE_NAME, null, cv);


    }

}
