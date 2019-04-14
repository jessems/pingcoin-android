package com.pingcoin.android.pingcoin;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "coin_table")
public class Coin {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "coin_id") // e.g. gold_south_african_krugerrand_1oz
    public String coinId;

    @ColumnInfo(name  = "popular_name") // e.g. Krugerrand
    public String popularName;

    @ColumnInfo(name  = "material_class") // e.g. Gold, Silver, ...
    public String materialClass;

    // TODO: implement hashmap for coin composition

    @ColumnInfo(name = "series") // e.g. Krugerrand
    public String series;

    @ColumnInfo(name = "nationality")
    public String nationality;

    @ColumnInfo(name = "country")
    public String country;

    @ColumnInfo(name = "weight")
    public float weight;

    @ColumnInfo(name = "weight_in_oz")
    public float weightInOz;

    @ColumnInfo(name = "c0d2a")
    public float c0d2a;

    @ColumnInfo(name = "c0d2b")
    public float c0d2b;

    @ColumnInfo(name = "c1d0") // There is no a, b for c1d0 because this resonance doesn't split
    public float c1d0;

    @ColumnInfo(name = "c0d3a")
    public float c0d3a;

    @ColumnInfo(name = "c0d3b")
    public float c0d3b;

    @ColumnInfo(name = "c0d4a")
    public float c0d4a;

    @ColumnInfo(name = "c0d4b")
    public float c0d4b;

    @ColumnInfo(name = "c1d1a")
    public float c1d1a;

    @ColumnInfo(name = "c1d1b")
    public float c1d1b;

    @ColumnInfo(name = "error")
    public float error;

    public Coin(
            String popularName,
            String series,
            float weightInOz,
            String materialClass,
            String coinId,
            String country,
            String nationality,
            float c0d2a,
            float c0d2b,
            float c1d0,
            float c0d3a,
            float c0d3b,
            float c1d1a,
            float c1d1b,
            float c0d4a,
            float c0d4b,
            float error) {
        this.popularName = popularName;
        this.series = series;
        this.weightInOz = weightInOz;
        this.materialClass = materialClass;
        this.country = country;
        this.nationality = nationality;
        this.coinId = coinId;
        this.c0d2a = c0d2a;
        this.c0d2b = c0d2b;
        this.c1d0 = c1d0;
        this.c0d3a = c0d3a;
        this.c0d3b = c0d3b;
        this.c1d1a = c1d1a;
        this.c1d1b = c1d1b;
        this.c0d4a = c0d4a;
        this.c0d4b = c0d4b;
        this.error = error;

    }

    public String getId(){
        return this.coinId;
    }

    public float getC0D2a() {
        return this.c0d2a;
    }

    public float getC0D3a() {
        return this.c0d3a;
    }

    public float getC0D4a() {
        return this.c0d4a;
    }

    public float getError() {
        return this.error;
    }

    public String getPopularName(){
        return this.popularName;
    }

    public String getCountry(){
        return this.country;
    }

    public float getWeightInOz(){
        return this.weightInOz;
    }

    public String getMaterialClass() {
        return this.materialClass;
    }
}