package com.pingcoin.android.pingcoin;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

/**
 * Created by jmscdch on 05/02/18.
 */

public class XAxisValueFormatter implements IAxisValueFormatter {

    private int sampleRate = 44100; // Default
    private int windowSize = 4096; // Default

    public XAxisValueFormatter(int sampleRate, int windowSize) {
        this.sampleRate = sampleRate;
        this.windowSize = windowSize;

    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // "value" represents the position of the label on the axis (x or y)

        return Float.toString(value / windowSize * (sampleRate/2));
    }

    /** this is only needed if numbers are returned, else return 0 */
//    @Override
//    public int getDecimalDigits() { return 1; }
}