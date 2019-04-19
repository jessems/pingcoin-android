package com.pingcoin.android.pingcoin;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

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
