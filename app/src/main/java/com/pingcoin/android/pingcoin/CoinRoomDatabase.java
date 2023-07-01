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

@Database(entities = {Coin.class}, version = 2)
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
                    4862.6f,
                    0.04f,
                    10925.9f,
                    0.03f,
                    18596.6f,
                    0.03f

            ));
            mDao.insert(new Coin(
                    "Maple Leaf",
                    "Maple Leaf",
                    1,
                    "Gold",
                    "gold_canadian_maple_leaf_1oz",
                    "Canada",
                    "Canadian",
                    4759.7f,
                    0.14f,
                    10839.77f,
                    0.04f,
                    18593.9f,
                    0.04f
            ));
            mDao.insert(new Coin(
                    "Eagle",
                    "Eagle",
                    1,
                    "Gold",
                    "gold_american_eagle_1oz",
                    "USA",
                    "American",
                    4517.3f,
                    0.04f,
                    10453.76f,
                    0.04f,
                    18081.5f,
                    0.04f
            ));
            mDao.insert(new Coin(
                    "Eagle",
                    "Eagle",
                    1,
                    "Silver",
                    "silver_american_eagle_1oz",
                    "U.S.A.",
                    "American",
                    3740.2f,
                    0.04f,
                    8643.2f,
                    0.04f,
                    15056.2f,
                    0.04f

            ));
            mDao.insert(new Coin(
                    "Buffalo",
                    "Buffalo",
                    1,
                    "Silver",
                    "silver_american_buffalo_1oz",
                    "U.S.A.",
                    "American",
                    4354.5f,
                    0.08f,
                    9896f,
                    0.04f,
                    16914.0f,
                    0.04f
            ));

            mDao.insert(new Coin(
                    "Rooster",
                    "Lunar Rooster",
                    1,
                    "Silver",
                    "silver_australian_lunar_rooster_1oz",
                    "Australia",
                    "Australian",
                    2407f,
                    0.04f,
                    5745f,
                    0.04f,
                    10239f,
                    0.04f
            ));

            mDao.insert(new Coin(
                    "Maple Leaf",
                    "Maple Leaf",
                    1,
                    "Silver",
                    "silver_canadian_maple_leaf_1oz",
                    "Canada",
                    "Canadian",
                    4716.6f,
                    0.062f,
                    10913.5f,
                    0.02f,
                    18890.4f,
                    0.026f
            ));


            mDao.insert(new Coin(
                    "Kangaroo",
                    "Kangaroo",
                    1,
                    "Gold",
                    "gold_australian_kangaroo_1oz",
                    "Australia",
                    "Australian",
                    3615.0f,
                    0.0579f,
                    8465.0f,
                    0.02f,
                    14807.0f,
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
                    3848.0f,
                    0.04f,
                    8978f,
                    0.04f,
                    15597f,
                    0.04f
            ));

            mDao.insert(new Coin(
                    "Kookaburra",
                    "Kookaburra",
                    1,
                    "Silver",
                    "silver_australian_kookaburra_1oz",
                    "Australia",
                    "Australian",
                    3740.0f,
                    0.07f,
                    8631.7f,
                    0.02f,
                    15051.0f,
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
                    5283.1f,
                    0.0515f,
                    11980.8f,
                    0.02f,
                    20479.3f,
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
                    4802f,
                    0.04f,
                    10384f,
                    0.04f,
                    17830f,
                    0.04f
            ));
            mDao.insert(new Coin(
                    "Corona (100)",
                    "Corona",
                    0.9802f,
                    "Gold",
                    "gold_austrian_100_corona",
                    "Austria",
                    "Austria",
                    2938f,
                    0.04f,
                    6909f,
                    0.04f,
                    12039f,
                    0.04f
            ));
            mDao.insert(new Coin(
                    "Krugerrand",
                    "Krugerrand",
                    1,
                    "Silver",
                    "silver_south_african_krugerrand_1oz",
                    "South Africa",
                    "South African",
                    4862.6f,
                    0.127f,
                    10353f,
                    0.04f,
                    18027f,
                    0.04f
            ));
            mDao.insert(new Coin(
                    "Panda",
                    "Panda",
                    1,
                    "Gold",
                    "gold_chinese_panda_1oz",
                    "China",
                    "Chinese",
                    3766f,
                    0.04f,
                    8564f,
                    0.04f,
                    14852f,
                    0.04f
            ));

            mDao.insert(new Coin(
                    "Elephant",
                    "Elephant",
                    1,
                    "Silver",
                    "silver_somalian_elephant_1oz",
                    "Somalia",
                    "Somalian",
                    4593f,
                    0.04f,
                    10219f,
                    0.04f,
                    17499f,
                    0.04f
            ));

            mDao.insert(new Coin(
                    "Britannia (user contributed)",
                    "Britannia",
                    1,
                    "Silver",
                    "silver_great_britain_britannia_1oz",
                    "Great Britain",
                    "British",
                    4531.3f,
                    0.066f,
                    10405.3f,
                    0.04f,
                    17984.0f,
                    0.04f
            ));

            mDao.insert(new Coin(
                    "British Lunar",
                    "Lunar",
                    1,
                    "Silver",
                    "silver_british_lunar_1oz",
                    "Great Britain",
                    "British",
                    4466f,
                    0.069f,
                    10364f,
                    0.04f,
                    17954f,
                    0.04f
            ));

            mDao.insert(new Coin(
                    "Morgan Dollar (beta)",
                    "Dollar",
                    1,
                    "Silver",
                    "silver_morgan_dollar",
                    "U.S.A.",
                    "American",
                    4432.3f,
                    0.098f,
                    10204f,
                    0.095f,
                    17605.8f,
                    0.085f
            ));

            mDao.insert(new Coin(
                    "Peace Dollar (user contributed)",
                    "Dollar",
                    1,
                    "Silver",
                    "silver_american_peace_dollar",
                    "U.S.A.",
                    "U.S.A.",
                    4345.0f,
                    0.05f,
                    10054.0f,
                    0.05f,
                    17500.0f,
                    0.05f
            ));

            mDao.insert(new Coin(
                    "Bahar Azadi (user contributed)",
                    "Bahar Azadi",
                    0.26f,
                    "Gold",
                    "gold_iranian_azadi_1",
                    "Iran",
                    "Iranian",
                    5198.5f,
                    0.04f,
                    11724.5f,
                    0.04f,
                    20300f,
                    0.04f
            ));

            mDao.insert(new Coin(
                    "Australian Koala",
                    "Koala",
                    1,
                    "Silver",
                    "silver_australian_koala_1oz",
                    "Australia",
                    "Australian",
                    3675.5f,
                    0.082f,
                    8643f,
                    0.04f,
                    15160f,
                    0.04f
            ));

            mDao.insert(new Coin(
                    "Australian Kangaroo (beta)",
                    "Kangaroo",
                    1,
                    "Silver",
                    "silver_australian_kangaroo_1oz",
                    "Australia",
                    "Australian",
                    3673.2f,
                    0.132f,
                    8651.6f,
                    0.04f,
                    15230.9f,
                    0.04f
            ));

            // TODO: This coin is not in the recording folder. I need to re-add it somehow.
//            mDao.insert(new Coin(
//                    "Tuvalo (user contributed)",
//                    "Marvel",
//                    1,
//                    "Silver",
//                    "silver_tuvalu_coin_1oz",
//                    "Tuvalo",
//                    "Tuvalo",
//
//            ));

            mDao.insert(new Coin(
                    "5 Kronor (user contributed)",
                    "Kronor",
                    1,
                    "Silver",
                    "silver_swedish_5_kronor",
                    "Sweden",
                    "Swedish",
                    5064f,
                    0.04f,
                    11794f,
                    0.04f,
                    20394f,
                    0.04f
            ));

            mDao.insert(new Coin(
                    "Gold Sovereign (user contributed)",
                    "King Edward VII",
                    0.26f,
                    "Gold",
                    "gold_british_sovereign_king_edward_vii",
                    "Great Britain",
                    "British",
                    5651.3f,
                    0.04f,
                    12756f,
                    0.04f,
                    21715f,
                    0.04f

            ));
            mDao.insert(new Coin(
                    "APMEX Gold Round (user contributed)",
                    "Rounds",
                    0.25f,
                    "Gold",
                    "gold_apmex_round_1_4oz",
                    "APMEX",
                    "APMEX",
                    4278f,
                    0.153f,
                    9727f,
                    0.04f,
                    16949f,
                    0.04f
            ));
            mDao.insert(new Coin(
                    "Libertad (user contributed)",
                    "Libertad",
                    1,
                    "Silver",
                    "silver_mexican_libertad_1oz",
                    "Mexico",
                    "Mexican",
                    3858f,
                    0.04f,
                    9069f,
                    0.04f,
                    15862f,
                    0.04f
            ));
            mDao.insert(new Coin(
                    "Double Eagle Saint-Gaudens",
                    "Saint-Gaudens (1907-1933)",
                    1,
                    "Gold",
                    "gold_american_double_eagle_saint_gaudens_1907_1933_1oz",
                    "U.S.A.",
                    "American",
                    4091f,
                    0.05f,
                    9488f,
                    0.05f,
                    16513f,
                    0.05f
            ));
            mDao.insert(new Coin(
                    "Half Dollar (Franklin) (user c.)",
                    "Half Dollar (1955-1963)",
                    0.362f,
                    "Silver",
                    "silver_franklin_half_dollar",
                    "U.S.A.",
                    "American",
                    4980f,
                    0.145f,
                    11422f,
                    0.05f,
                    19911f,
            0.05f
            ));
            mDao.insert(new Coin(
                    "Half Dollar (Walking Liberty) (user c.)",
                    "Half Dollar (1916-1947)",
                    0.362f,
                    "Silver",
                    "silver_american_walking_liberty_half_dollar",
                    "U.S.A.",
                    "American",
                    5032f,
                    0.04f,
                    11394f,
                    0.05f,
                    19755f,
                    0.05f
            ));
            mDao.insert(new Coin(
                    "Washington Quarter (user c.)",
                    "Washington Quarter",
                    0.18f,
                    "Silver",
                    "silver_american_washington_quarter",
                    "U.S.A.",
                    "American",
                    6199f,
                    0.159f,
                    14609f,
                    0.05f,
                    21947f,
                    0.05f
            ));
            mDao.insert(new Coin(
                    "Kangaroo (user c.)",
                    "Kangaroo",
                    0.5f,
                    "Gold",
                    "gold_australian_kangaroo_1_2oz",
                    "Australia",
                    "Australian",
                    3615f,
                    0.058f,
                    8465.5f,
                    0.04f,
                    14807f,
                    0.04f
            ));
            mDao.insert(new Coin(
                    "500 Lira (user contributed)",
                    "Italian Lira",
                    0.3f,
                    "Silver",
                    "silver_italian_500_lira",
                    "Italy",
                    "Italian",
                    5402.5f,
                    0.082f,
                    12367f,
                    0.05f,
                    21453f,
                    0.05f
            ));
            mDao.insert(new Coin(
                    "Mexican 1/2 oz Libertad (user c.)",
                    "Mexican Libertad",
                    0.5f,
                    "Gold",
                    "gold_mexican_libertad_1_2oz",
                    "Mexico",
                    "Mexican",
                    2766f,
                    0.04f,
                    6724f,
                    0.04f,
                    12074f,
                    0.04f
            ));
            mDao.insert(new Coin(
                    "APMEX Fine Silver Round 1 oz",
                    "APMEX Silver Round",
                    1f,
                    "Silver",
                    "silver_apmex_round_1oz",
                    "APMEX",
                    "APMEX",
                    4398f,
                    0.03f,
                    10035f,
                    0.03f,
                    17290f,
                    0.03f
            ));
            mDao.insert(new Coin(
                    "American Buffalo",
                    "Buffalo",
                    1,
                    "Gold",
                    "gold_american_buffalo_1oz",
                    "USA",
                    "American",
                    3386.75f,
                    0.067f,
                    7870.75f,
                    0.04f,
                    13808.75f,
                    0.04f
            ));





            return null;
        }
    }
}
