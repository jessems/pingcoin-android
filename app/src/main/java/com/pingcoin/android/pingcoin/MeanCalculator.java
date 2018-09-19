package com.pingcoin.android.pingcoin;

import android.util.Log;

class MeanCalculator {
    private MeanCalculator() {}

    public static void main(String[] args) {
        double[] data = new double[]{2, 4, 8};
        double gm = geometricMean(data);
        double am = arithmeticMean(data);
        System.out.println("Geometric mean of 2, 4 and 8: " + gm );
    }

    public static double geometricMean(double[] x) {
        int n = x.length;
        double GM_log = 0.0d;
        double bias = 0.01;
        for (int i = 0; i < n; ++i) {
            if (x[i] == 0L) {
                GM_log += Math.log(bias);
            }
            else {
                GM_log += Math.log(x[i]);
            }
        }
        return Math.exp(GM_log / n);
    }

    public static double arithmeticMean(double[] m) {
        double sum = 0;
        for (int i = 0; i < m.length; i++) {
            sum += m[i];
        }
        return sum / m.length;
    }

    public static double spectralCentroid(double[] n) {
        double sumOfWeightedMagnitudes = 0;
        double sumOfMagnitudes = 0;
        for (int i = 0; i < n.length; i++) {
            sumOfWeightedMagnitudes += n[i] * i;
            sumOfMagnitudes += n[i];
        }
        return sumOfWeightedMagnitudes / sumOfMagnitudes;
    }
}
