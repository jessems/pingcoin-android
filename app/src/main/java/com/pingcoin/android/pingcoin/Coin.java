package com.pingcoin.android.pingcoin;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

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

    @ColumnInfo(name = "c0d2")
    public float c0d2;

    @ColumnInfo(name = "c0d2Error")
    public float c0d2Error;

    @ColumnInfo(name = "c1d0") // There is no a, b for c1d0 because this resonance doesn't split
    public float c1d0;

    @ColumnInfo(name = "c1d0Error")
    public float c1d0Error;

    @ColumnInfo(name = "c0d3")
    public float c0d3;

    @ColumnInfo(name = "c0d3Error")
    public float c0d3Error;

    @ColumnInfo(name = "c0d4")
    public float c0d4;

    @ColumnInfo(name = "c0d4Error")
    public float c0d4Error;

    @ColumnInfo(name = "c1d1")
    public float c1d1;

    @ColumnInfo(name = "c1d1Error")
    public float c1d1Error;

    public Coin(
            String popularName,
            String series,
            float weightInOz,
            String materialClass,
            String coinId,
            String country,
            String nationality,
            float c0d2,
            float c0d2Error,
            float c0d3,
            float c0d3Error,
            float c0d4,
            float c0d4Error
            ) {
        this.popularName = popularName;
        this.series = series;
        this.weightInOz = weightInOz;
        this.materialClass = materialClass;
        this.country = country;
        this.nationality = nationality;
        this.coinId = coinId;
        this.c0d2 = c0d2;
        this.c0d2Error = c0d2Error;
        this.c0d3 = c0d3;
        this.c0d3Error = c0d3Error;
        this.c0d4 = c0d4;
        this.c0d4Error = c0d4Error;

    }

    public String getId(){
        return this.coinId;
    }

    public float getC0D2() {
        return this.c0d2;
    }

    public float getC0D2Error() {
        return this.c0d2Error;
    }

    public float getC0D3() {
        return this.c0d3;
    }

    public float getC0D3Error() {
        return this.c0d3Error;
    }

    public float getC0D4() {
        return this.c0d4;
    }

    public float getC0D4Error() {
        return this.c0d4Error;
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