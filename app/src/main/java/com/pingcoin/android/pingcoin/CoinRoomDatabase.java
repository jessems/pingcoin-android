package com.pingcoin.android.pingcoin;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

@Database(entities = {Coin.class}, version = 1)
public abstract class CoinRoomDatabase extends RoomDatabase {

    public abstract CoinDao coinDao();

    private static CoinRoomDatabase INSTANCE;

    static CoinRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (CoinRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            CoinRoomDatabase.class, "coin_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    @NonNull
    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
        return null;
    }

    @NonNull
    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }

    @Override
    public void clearAllTables() {

    }

    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){

                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db){
                    super.onOpen(db);
                    new PopulateDbAsync(INSTANCE).execute();
                    Log.d("CoinRoomDatabase", "Db INSTANCE executed");
                }
            };

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final CoinDao mDao;

        PopulateDbAsync(CoinRoomDatabase db) {
            mDao = db.coinDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mDao.deleteAll();
            // TODO: Put these coins into (separate) json files
            mDao.insert(new Coin(
                    "Krugerrand",
                    1,
                    "Gold",
                    "1oz_gold_south_african_krugerrand",
                    "South Africa",
                    "South African",
                    4792,
                    0,
                    8652,
                    10856,
                    0,
                    0,
                    0,
                    18629,
                    0,
                    0.02f
            ));
            mDao.insert(new Coin(
                    "Maple Leaf",
                    1,
                    "Gold",
                    "1oz_gold_canadian_maple_leaf",
                    "Canada",
                    "Canadian",
                    4655,
                    0,
                    7881,
                    10988,
                    0,
                    0,
                    0,
                    19010,
                    0,
                    0.02f
            ));
            mDao.insert(new Coin(
                    "Eagle",
                    1,
                    "Silver",
                    "1oz_silver_american_eagle",
                    "U.S.A.",
                    "American",
                    3753,
                    0,
                    6451,
                    8653,
                    0,
                    13955,
                    14377,
                    15180,
                    0,
                    0.02f
            ));
            mDao.insert(new Coin(
                    "Buffalo",
                    1,
                    "Silver",
                    "1oz_silver_american_buffalo",
                    "U.S.A.",
                    "American",
                    4233,
                    4402,
                    7526,
                    9890,
                    0,
                    16224,
                    16392,
                    16984,
                    0,
                    0.02f
            ));

            mDao.insert(new Coin(
                    "Rooster",
                    1,
                    "Silver",
                    "1oz_silver_australian_rooster",
                    "Australia",
                    "Australian",
                    2460,
                    0,
                    3730,
                    5753,
                    0,
                    0,
                    0,
                    10228,
                    0,
                    0.02f
            ));

            mDao.insert(new Coin(
                    "Maple Leaf",
                    1,
                    "Silver",
                    "1oz_silver_canadian_maple_leaf",
                    "Canada",
                    "Canadian",
                    4655,
                    4655,
                    7881,
                    10988,
                    10988,
                    0,
                    0,
                    19010,
                    19010,
                    0.02f
            ));

            mDao.insert(new Coin(
                    "Brittania",
                    1,
                    "Gold",
                    "1oz_gold_british_brittania",
                    "United Kingdom",
                    "British",
                    4824,
                    4824,
                    0,
                    10988,
                    10988,
                    0,
                    0,
                    18841,
                    18841,
                    0.02f
            ));

            mDao.insert(new Coin(
                    "Kangaroo",
                    1,
                    "Gold",
                    "1oz_gold_australian_kangaroo",
                    "Australia",
                    "Australian",
                    3557,
                    4824,
                    0,
                    8370,
                    8370,
                    0,
                    0,
                    14872,
                    14872,
                    0.02f
            ));


            mDao.insert(new Coin(
                    "Panda",
                    1,
                    "Silver",
                    "1oz_silver_chinese_panda",
                    "China",
                    "Chinese",
                    3796,
                    3892,
                    0,
                    8922,
                    8531,
                    0,
                    0,
                    15623,
                    15623,
                    0.02f
            ));

            mDao.insert(new Coin(
                    "Kookaburra",
                    1,
                    "Silver",
                    "1oz_silver_australian_kookaburra",
                    "Australia",
                    "Australian",
                    3628,
                    3864,
                    0,
                    8630,
                    8630,
                    0,
                    0,
                    15053,
                    15053,
                    0.02f
            ));

            mDao.insert(new Coin(
                    "Philharmonikker",
                    1,
                    "Silver",
                    "1oz_silver_austrian_philharmonikker",
                    "Austria",
                    "Austrian",
                    5202,
                    5355,
                    0,
                    11870,
                    12023,
                    0,
                    0,
                    20421,
                    20472,
                    0.02f
            ));






            return null;
        }
    }
}
