package com.pingcoin.android.pingcoin;

import android.provider.BaseColumns;

/**
 * Created by jmscdch on 03/02/18.
 */

public final class CoinContract {
    // prevent accidental instantiation:
    private CoinContract() {}

    public static class CoinEntry implements BaseColumns {
        public static final String TABLE_NAME = "coins";
        public static final String COLUMN_FULL_NAME = "fullName";
        public static final String COLUMN_SERIES = "series";
        public static final String COLUMN_MATERIAL_CLASS = "materialClass";
        public static final String COLUMN_WEIGHT_CLASS = "weightClass";
        public static final String COLUMN_WEIGHT = "weight";
        public static final String COLUMN_DIAMETER = "diameter";
        public static final String COLUMN_C0D2A = "c0d2a";
        public static final String COLUMN_C0D2B = "c0d2b";
        public static final String COLUMN_C1D0 = "c1d0";
        public static final String COLUMN_C0D3A = "c0d3a";
        public static final String COLUMN_C0D3B = "c0d3b";
        public static final String COLUMN_C0D4A = "c0d4a";
        public static final String COLUMN_C0D4B = "c0d4b";
        public static final String COLUMN_C1D1A = "c1d1a";
        public static final String COLUMN_C1D1B = "c1d1b";
        public static final String COLUMN_CD_ERROR = "cdError";
    }
}
