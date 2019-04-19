package com.pingcoin.android.pingcoin;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
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
                            .fallbackToDestructiveMigration()
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
                    "Krugerrand",
                    1,
                    "Gold",
                    "gold_south_african_krugerrand_1oz",
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
                    "Maple Leaf",
                    1,
                    "Gold",
                    "gold_canadian_maple_leaf_1oz",
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
                    "Eagle",
                    1,
                    "Gold",
                    "gold_american_eagle_1oz",
                    "USA",
                    "American",
                    4506,
                    0,
                    0,
                    10385,
                    0,
                    0,
                    0,
                    18079,
                    0,
                    0.02f
            ));
            mDao.insert(new Coin(
                    "Eagle",
                    "Eagle",
                    1,
                    "Silver",
                    "silver_american_eagle_1oz",
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
                    "Buffalo",
                    1,
                    "Silver",
                    "silver_american_buffalo_1oz",
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
                    "Lunar Rooster",
                    1,
                    "Silver",
                    "silver_australian_lunar_rooster_1oz",
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
                    "Maple Leaf",
                    1,
                    "Silver",
                    "silver_canadian_maple_leaf_1oz",
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

//            mDao.insert(new Coin(
//                    "Britania",
//                    "Britania",
//                    1,
//                    "Gold",
//                    "gold_british_britania_1oz",
//                    "United Kingdom",
//                    "British",
//                    4824,
//                    4824,
//                    0,
//                    10988,
//                    10988,
//                    0,
//                    0,
//                    18841,
//                    18841,
//                    0.02f
//            ));

            mDao.insert(new Coin(
                    "Kangaroo",
                    "Kangaroo",
                    1,
                    "Gold",
                    "gold_australian_kangaroo_1oz",
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
                    "Panda",
                    1,
                    "Silver",
                    "silver_chinese_panda_1oz",
                    "China",
                    "Chinese",
                    3766,
                    0,
                    0,
                    8564,
                    0,
                    0,
                    0,
                    14852,
                    0,
                    0.02f
            ));

            mDao.insert(new Coin(
                    "Kookaburra",
                    "Kookaburra",
                    1,
                    "Silver",
                    "silver_australian_kookaburra_1oz",
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
                    "Philharmonic",
                    "Philharmonic",
                    1,
                    "Silver",
                    "silver_austrian_philharmonic_1oz",
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
            mDao.insert(new Coin(
                    "Cook Islands",
                    "Bounty",
                    1,
                    "Silver",
                    "silver_cook_islands_bounty_1oz",
                    "Cook Islands",
                    "Cook Island",
                    4345,
                    0,
                    0,
                    10384,
                    0,
                    0,
                    0,
                    17830,
                    0,
                    0.02f
            ));
            mDao.insert(new Coin(
                    "Corona (100)",
                    "Corona",
                    0.9802f,
                    "Gold",
                    "gold_austrian_100_corona",
                    "Austria",
                    "Austria",
                    2938,
                    0,
                    0,
                    6909,
                    0,
                    0,
                    0,
                    12039,
                    0,
                    0.02f
            ));
            mDao.insert(new Coin(
                    "Krugerrand",
                    "Krugerrand",
                    1,
                    "Silver",
                    "silver_south_african_krugerrand_1oz",
                    "South Africa",
                    "South African",
                    4510,
                    0,
                    0,
                    10302,
                    0,
                    0,
                    0,
                    17582,
                    0,
                    0.02f
            ));
            mDao.insert(new Coin(
                    "Panda",
                    "Panda",
                    1,
                    "Gold",
                    "gold_chinese_panda_1oz",
                    "China",
                    "Chinese",
                    3170,
                    0,
                    0,
                    8205,
                    0,
                    0,
                    0,
                    14633,
                    0,
                    0.02f
            ));

            mDao.insert(new Coin(
                    "Elephant",
                    "Elephant",
                    1,
                    "Silver",
                    "silver_somalian_elephant_1oz",
                    "Somalia",
                    "Somalian",
                    4593,
                    0,
                    0,
                    10219,
                    0,
                    0,
                    0,
                    17499,
                    0,
                    0.02f
            ));

            mDao.insert(new Coin(
                    "Britannia",
                    "Britannia",
                    1,
                    "Silver",
                    "silver_great_britain_britannia_1oz",
                    "Great Britain",
                    "British",
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0.02f
            ));






            return null;
        }
    }
}
