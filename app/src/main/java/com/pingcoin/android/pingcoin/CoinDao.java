package com.pingcoin.android.pingcoin;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface CoinDao {

    @Insert
    void insert(Coin coin);

    @Query("DELETE FROM coin_table")
    void deleteAll();

    @Query("SELECT * from coin_table ORDER BY material_class, popular_name")
    LiveData<List<Coin>> getAllCoins();
}
