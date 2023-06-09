package com.pingcoin.android.pingcoin;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;

import java.util.List;

public class CoinViewModel extends AndroidViewModel {

    private CoinRepository mRepository;
    private LiveData<List<Coin>> mAllCoins;

    public CoinViewModel(@NonNull Application application) {
        super(application);
        mRepository = new CoinRepository(application);
        mAllCoins = mRepository.getAllCoins();
    }

    LiveData<List<Coin>> getAllCoins() { return mAllCoins; }

    public void insert(Coin coin) { mRepository.insert(coin); }
}
