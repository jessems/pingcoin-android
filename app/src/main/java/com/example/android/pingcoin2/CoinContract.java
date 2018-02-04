package com.example.android.pingcoin2;

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
        public static final String COLUMN_FAMILY_NAME = "familyName";
        public static final String COLUMN_MATERIAL_CLASS = "materialClass";
        public static final String COLUMN_WEIGHT_CLASS = "weightClass";
        public static final String COLUMN_WEIGHT = "weight";
        public static final String COLUMN_DIAMETER = "diameter";
        public static final String COLUMN_C0D2 = "c0d2";
        public static final String COLUMN_C0D3 = "c0d3";
        public static final String COLUMN_C0D4 = "c0d4";
        public static final String COLUMN_CD_ERROR = "cdError";
    }
}
