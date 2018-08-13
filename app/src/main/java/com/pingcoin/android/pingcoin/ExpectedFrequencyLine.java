package com.pingcoin.android.pingcoin;

import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.utils.Utils;

/**
 * Created by jmscdch on 27/05/18.
 */

public class ExpectedFrequencyLine extends LimitLine {
    private float mLineWidth = 22f;

    public ExpectedFrequencyLine(float limit, String label) {
        super(limit, label);

    }

    @Override
    public void setLineWidth(float width) {
        if (width < 0.2f) {
            width = 0.2f;
        }
        mLineWidth = Utils.convertDpToPixel(width);

    }

    @Override
    public float getLineWidth() {
        return mLineWidth;
    }
}
