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
                    3757,
                    0,
                    0,
                    8695,
                    0,
                    0,
                    0,
                    15147,
                    0,
                    0.0317f
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
                    4667,
                    0,
                    7881,
                    10920,
                    0,
                    0,
                    0,
                    18891,
                    0,
                    0.043f
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
                    3848,
                    0,
                    0,
                    8978,
                    0,
                    0,
                    0,
                    15597,
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
                    5248,
                    0,
                    0,
                    11993,
                    0,
                    0,
                    0,
                    20480,
                    0,
                    0.025f
            ));
            mDao.insert(new Coin(
                    "Cook Islands",
                    "Bounty",
                    1,
                    "Silver",
                    "silver_cook_islands_bounty_1oz",
                    "Cook Islands",
                    "Cook Island",
                    4802,
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
                    4569,
                    0,
                    0,
                    10364,
                    0,
                    0,
                    0,
                    18060,
                    0,
                    0.03f
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
                    "Britannia (user contributed)",
                    "Britannia",
                    1,
                    "Silver",
                    "silver_great_britain_britannia_1oz",
                    "Great Britain",
                    "British",
                    4470,
                    0,
                    0,
                    10379,
                    0,
                    0,
                    0,
                    17956,
                    0,
                    0.039f
            ));

            mDao.insert(new Coin(
                    "British Lunar",
                    "Lunar",
                    1,
                    "Silver",
                    "silver_british_lunar_1oz",
                    "Great Britain",
                    "British",
                    4466,
                    0,
                    0,
                    10364,
                    0,
                    0,
                    0,
                    17954,
                    0,
                    0.03f
            ));

            mDao.insert(new Coin(
                    "Morgan Dollar (user contributed)",
                    "Dollar",
                    1,
                    "Silver",
                    "silver_morgan_dollar",
                    "U.S.A.",
                    "American",
                    4353,
                    0,
                    0,
                    10100,
                    0,
                    0,
                    0,
                    17544,
                    0,
                    0.03f
            ));

            mDao.insert(new Coin(
                    "Peace Dollar (user contributed)",
                    "Dollar",
                    1,
                    "Silver",
                    "silver_american_peace_dollar",
                    "U.S.A.",
                    "U.S.A.",
                    4345,
                    0,
                    0,
                    10054,
                    0,
                    0,
                    0,
                    17500,
                    0,
                    0.03f
            ));

            mDao.insert(new Coin(
                    "Bahar Azadi (user contributed)",
                    "Bahar Azadi",
                    0.26f,
                    "Gold",
                    "gold_iranian_azadi_1",
                    "Iran",
                    "Iranian",
                    5198,
                    0,
                    0,
                    11724,
                    0,
                    0,
                    0,
                    20300,
                    0,
                    0.02f
            ));

            mDao.insert(new Coin(
                    "Australian Koala",
                    "Koala",
                    1,
                    "Silver",
                    "silver_australian_koala_1oz",
                    "Australia",
                    "Australian",
                    3729,
                    0,
                    0,
                    8643,
                    0,
                    0,
                    0,
                    15160,
                    0,
                    0.02f
            ));

            mDao.insert(new Coin(
                    "Australian Kangaroo",
                    "Kangaroo",
                    1,
                    "Silver",
                    "silver_australian_kangaroo_1oz",
                    "Australia",
                    "Australian",
                    3600,
                    0,
                    0,
                    8565,
                    0,
                    0,
                    0,
                    15017,
                    0,
                    0.02f
            ));

            mDao.insert(new Coin(
                    "Tuvalo (user contributed)",
                    "Marvel",
                    1,
                    "Silver",
                    "silver_tuvalu_coin_1oz",
                    "Tuvalo",
                    "Tuvalo",
                    3729,
                    0,
                    0,
                    8643,
                    0,
                    0,
                    0,
                    15160,
                    0,
                    0.02f
            ));

            mDao.insert(new Coin(
                    "5 Kronor (user contributed)",
                    "Kronor",
                    1,
                    "Silver",
                    "silver_swedish_5_kronor",
                    "Sweden",
                    "Swedish",
                    5064,
                    0,
                    0,
                    11794,
                    0,
                    0,
                    0,
                    20394,
                    0,
                    0.02f
            ));

            mDao.insert(new Coin(
                    "Gold Sovereign (user contributed)",
                    "King Edward VII",
                    0.26f,
                    "Gold",
                    "gold_british_sovereign_king_edward_vii",
                    "Great Britain",
                    "British",
                    5651,
                    0,
                    0,
                    12756,
                    0,
                    0,
                    0,
                    21715,
                    0,
                    0.02f
            ));
            mDao.insert(new Coin(
                    "APMEX Gold Round (user contributed)",
                    "Rounds",
                    0.25f,
                    "Gold",
                    "gold_apmex_round_1_4oz",
                    "APMEX",
                    "APMEX",
                    4162,
                    0,
                    0,
                    9727,
                    0,
                    0,
                    0,
                    16949,
                    0,
                    0.02f
            ));
            mDao.insert(new Coin(
                    "Libertad (user contributed)",
                    "Libertad",
                    1,
                    "Silver",
                    "silver_mexican_libertad_1oz",
                    "Mexico",
                    "Mexican",
                    3858,
                    0,
                    0,
                    9069,
                    0,
                    0,
                    0,
                    15862,
                    0,
                    0.02f
            ));
            mDao.insert(new Coin(
                    "Double Eagle Saint-Gaudens",
                    "Saint-Gaudens (1907-1933)",
                    1,
                    "Gold",
                    "gold_american_double_eagle_saint_gaudens_1907_1933_1oz",
                    "U.S.A.",
                    "American",
                    4091,
                    0,
                    0,
                    9488,
                    0,
                    0,
                    0,
                    16513,
                    0,
                    0.02f
            ));
            mDao.insert(new Coin(
                    "Half Dollar (Franklin) (user c.)",
                    "Half Dollar (1955-1963)",
                    0.362f,
                    "Silver",
                    "silver_franklin_half_dollar",
                    "U.S.A.",
                    "American",
                    4888,
                    0,
                    0,
                    11422,
                    0,
                    0,
                    0,
                    19911,
                    0,
                    0.03f
            ));
            mDao.insert(new Coin(
                    "Half Dollar (Walking Liberty) (user c.)",
                    "Half Dollar (1916-1947)",
                    0.362f,
                    "Silver",
                    "silver_american_walking_liberty_half_dollar",
                    "U.S.A.",
                    "American",
                    5009,
                    5055,
                    0,
                    11394,
                    0,
                    0,
                    0,
                    19755,
                    0,
                    0.03f
            ));
            mDao.insert(new Coin(
                    "Washington Quarter (user c.)",
                    "Washington Quarter",
                    0.18f,
                    "Silver",
                    "silver_american_washington_quarter",
                    "U.S.A.",
                    "American",
                    6083,
                    6315,
                    0,
                    14609,
                    0,
                    0,
                    0,
                    21947,
                    0,
                    0.03f
            ));
            mDao.insert(new Coin(
                    "Kangaroo (user c.)",
                    "Washington Quarter",
                    0.5f,
                    "Gold",
                    "gold_australian_kangaroo_1_2oz",
                    "Australia",
                    "Australian",
                    4874,
                    4926,
                    0,
                    11522,
                    0,
                    0,
                    0,
                    20162,
                    0,
                    0.03f
            ));
            mDao.insert(new Coin(
                    "500 Lira (user contributed)",
                    "Italian Lira",
                    0.3f,
                    "Silver",
                    "silver_italian_500_lira",
                    "Italy",
                    "Italian",
                    5324,
                    5481,
                    0,
                    12367,
                    0,
                    0,
                    0,
                    21453,
                    0,
                    0.04f
            ));
            mDao.insert(new Coin(
                    "Mexican 1/2 oz Libertad (user c.)",
                    "Mexican Libertad",
                    0.5f,
                    "Gold",
                    "gold_mexican_libertad_1_2oz",
                    "Mexico",
                    "Mexican",
                    2766,
                    0,
                    0,
                    6724,
                    0,
                    0,
                    0,
                    12074,
                    0,
                    0.04f
            ));






            return null;
        }
    }
}
