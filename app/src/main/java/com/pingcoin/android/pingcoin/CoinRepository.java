package com.pingcoin.android.pingcoin;

import android.app.Application;
import androidx.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class CoinRepository {

    private CoinDao mCoinDao;
    private LiveData<List<Coin>> mAllCoins;

    CoinRepository(Application application) {
        CoinRoomDatabase db = CoinRoomDatabase.getDatabase(application);
        mCoinDao = db.coinDao();
        mAllCoins = mCoinDao.getAllCoins();
    }

    LiveData<List<Coin>> getAllCoins() {
        return mAllCoins;
    }

    public void insert (Coin coin) {
        new insertAsyncTask(mCoinDao).execute(coin);
    }

    private static class insertAsyncTask extends AsyncTask<Coin, Void, Void> {

        private CoinDao mAsyncTaskDao;

        insertAsyncTask(CoinDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Coin... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

}
