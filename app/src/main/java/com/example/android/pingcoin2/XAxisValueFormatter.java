package com.example.android.pingcoin2;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

/**
 * Created by jmscdch on 05/02/18.
 */

public class XAxisValueFormatter implements IAxisValueFormatter {

    public XAxisValueFormatter() {


    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // "value" represents the position of the label on the axis (x or y)

        int bufferSize = 1024;
        int samplingFrequency = 44100;

        return Float.toString(value * (samplingFrequency/bufferSize));
    }

    /** this is only needed if numbers are returned, else return 0 */
//    @Override
//    public int getDecimalDigits() { return 1; }
}