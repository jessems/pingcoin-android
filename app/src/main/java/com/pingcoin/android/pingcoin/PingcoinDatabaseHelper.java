package com.pingcoin.android.pingcoin;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
                CoinContract.CoinEntry.COLUMN_SERIES + " TEXT NOT NULL, " +
                CoinContract.CoinEntry.COLUMN_MATERIAL_CLASS + " TEXT NOT NULL, " +
                CoinContract.CoinEntry.COLUMN_WEIGHT_CLASS + " TEXT NOT NULL, " +
                CoinContract.CoinEntry.COLUMN_WEIGHT + " REAL, " +
                CoinContract.CoinEntry.COLUMN_DIAMETER + " REAL, " +
                CoinContract.CoinEntry.COLUMN_C0D2A + " REAL, " +
                CoinContract.CoinEntry.COLUMN_C0D2B + " REAL, " +
                CoinContract.CoinEntry.COLUMN_C1D0 + " REAL, " +
                CoinContract.CoinEntry.COLUMN_C0D3A + " REAL, " +
                CoinContract.CoinEntry.COLUMN_C0D3B + " REAL, " +
                CoinContract.CoinEntry.COLUMN_C0D4A + " REAL, " +
                CoinContract.CoinEntry.COLUMN_C0D4B + " REAL, " +
                CoinContract.CoinEntry.COLUMN_C1D1A + " REAL, " +
                CoinContract.CoinEntry.COLUMN_C1D1B + " REAL, " +
                CoinContract.CoinEntry.COLUMN_CD_ERROR + " REAL " +
                ");";

        Log.i("asd", "Before table creation");
        db.execSQL(SQL_CREATE_COINS_TABLE);
        Log.i("asd", "After table creation");

        // Works
        addCoins(db,
                "1 oz Silver American Eagle",
                "American Eagle",
                "Silver",
                "1 oz",
                31,
                36,
                3753,
                0,
                6451,
                8653,
                0,
                15180,
                0,
                13955,
                14377,
                2
        );

        // Works
        addCoins(db,
                "1 oz Silver American Buffalo",
                "American Buffalo",
                "Silver",
                "1 oz",
                31,
                36,
                4233,
                4402,
                7526,
                9890,
                0,
                16984,
                0,
                16224,
                16392,
                2
        );



        // Works
        addCoins(db,
                "1 oz Silver Australian Rooster",
                "Australian Rooster",
                "Silver",
                "1 oz",
                31,
                36,
                2460,
                0,
                3730,
                5753,
                0,
                10228,
                0,
                0,
                0,
                2
        );


        // Working
        addCoins(db,
                "1 oz Silver Canadian Maple Leaf",
                "Canadian Maple Leaf",
                "Silver",
                "1 oz",
                31,
                36,
                4655,
                0,
                7881,
                10988,
                0,
                19010,
                0,
                0,
                0,
                2
        );

        // Not tested
        addCoins(db,
                "1 oz Gold Britania",
                "Britannia",
                "Gold",
                "1 oz",
                31,
                36,
                4824,
                0,
                0,
                10988,
                0,
                18841,
                0,
                0,
                0,
                2
        );


        // Not tested
        addCoins(db,
                "1 oz Gold Australian Kangaroo",
                "Australian Kangaroo",
                "Gold",
                "1 oz",
                31,
                36,
                3557,
                0,
                0,
                8370,
                0,
                14872,
                0,
                0,
                0,
                2
        );

        // Not tested
        addCoins(db,
                "1 oz Gold Chinese Panda",
                "Chinese Panda",
                "Gold",
                "1 oz",
                31,
                36,
                3634,
                3735,
                0,
                8531,
                0,
                14806,
                14959,
                0,
                0,
                2
        );

        // Works
        addCoins(db,
                "1 oz Silver Chinese Panda",
                "Chinese Panda",
                "Silver",
                "1 oz",
                31,
                36,
                3796,
                3892,
                0,
                8922,
                0,
                15623,
                0,
                0,
                0,
                2
        );

        // Works
        addCoins(db,
                "1 oz Silver Australian Kookaburra",
                "Australian Kangaroo",
                "Silver",
                "1 oz",
                31,
                36,
                3628,
                3864,
                0,
                8630,
                0,
                15053,
                0,
                0,
                0,
                2
        );

        // Works
        addCoins(db,
                "1 oz Gold Krugerrand",
                "South African Krugerrand",
                "Gold",
                "1 oz",
                31,
                36,
                4792,
                0,
                8652,
                10856,
                0,
                18629,
                0,
                0,
                0,
                2
        );

        // Not tested
        addCoins(db,
                "1 oz Gold Canadian Maple Leaf",
                "Canadian Maple Leaf",
                "Gold",
                "1 oz",
                31,
                36,
                4658,
                4945,
                0,
                10839,
                11030,
                18648,
                18936,
                0,
                0,
                2
        );

        // Works
        addCoins(db,
                "1 oz Silver Philharmoniker",
                "Canadian Maple Leaf",
                "Silver",
                "1 oz",
                31,
                36,
                5202,
                5355,
                0,
                11870,
                12023,
                20421,
                20472,
                0,
                0,
                2
        );


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



    private long addCoins(
            SQLiteDatabase db,
            String fullName,
            String series,
            String materialClass,
            String weightClass,
            float weight,
            float diameter,
            float c0d2a,
            float c0d2b,
            float c1d0,
            float c0d3a,
            float c0d3b,
            float c0d4a,
            float c0d4b,
            float c1d1a,
            float c1d1b,
            float cd_error
    ) {
        ContentValues cv = new ContentValues();

        cv.put(CoinContract.CoinEntry.COLUMN_FULL_NAME, fullName);
        cv.put(CoinContract.CoinEntry.COLUMN_SERIES, series);
        cv.put(CoinContract.CoinEntry.COLUMN_MATERIAL_CLASS, materialClass);
        cv.put(CoinContract.CoinEntry.COLUMN_WEIGHT_CLASS, weightClass);
        cv.put(CoinContract.CoinEntry.COLUMN_WEIGHT, weight);
        cv.put(CoinContract.CoinEntry.COLUMN_DIAMETER, diameter);
        cv.put(CoinContract.CoinEntry.COLUMN_C0D2A, c0d2a);
        cv.put(CoinContract.CoinEntry.COLUMN_C0D2B, c0d2b);
        cv.put(CoinContract.CoinEntry.COLUMN_C1D0, c1d0);
        cv.put(CoinContract.CoinEntry.COLUMN_C0D3A, c0d3a);
        cv.put(CoinContract.CoinEntry.COLUMN_C0D3B, c0d3b);
        cv.put(CoinContract.CoinEntry.COLUMN_C0D4A, c0d4a);
        cv.put(CoinContract.CoinEntry.COLUMN_C0D4B, c0d4b);
        cv.put(CoinContract.CoinEntry.COLUMN_C1D1A, c1d1a);
        cv.put(CoinContract.CoinEntry.COLUMN_C1D1B, c1d1b);
        cv.put(CoinContract.CoinEntry.COLUMN_CD_ERROR, cd_error);

        Log.i("addCoins", fullName + " added.");
        return db.insert(CoinContract.CoinEntry.TABLE_NAME, null, cv);


    }

}
