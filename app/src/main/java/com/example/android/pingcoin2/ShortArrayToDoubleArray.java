package com.example.android.pingcoin2;

class ShortArrayToDoubleArray {
    public static double[] convertFromShortArrayToDoubleArray(Short[] shortData) {
        int size = shortData.length;
        double[] doubleData = new double[size];
        for (int i = 0; i < size; i++) {
            doubleData[i] = shortData[i] / 32768.0;
        }
        return doubleData;
    }

    public static double[] convertFromShortArrayToDoubleArray(short[] shortData) {
        int size = shortData.length;
        double[] doubleData = new double[size];
        for (int i = 0; i < size; i++) {
            doubleData[i] = shortData[i] / 32768.0;
        }
        return doubleData;
    }

}
