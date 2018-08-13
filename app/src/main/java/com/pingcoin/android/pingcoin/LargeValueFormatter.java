package com.pingcoin.android.pingcoin;

import android.util.Log;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

/**
 * Predefined value-formatter that formats large numbers in a pretty way.
 * Outputs: 856 = 856; 1000 = 1k; 5821 = 5.8k; 10500 = 10k; 101800 = 102k;
 * 2000000 = 2m; 7800000 = 7.8m; 92150000 = 92m; 123200000 = 123m; 9999999 =
 * 10m; 1000000000 = 1b; Special thanks to Roman Gromov
 * (https://github.com/romangromov) for this piece of code.
 *
 * @author Philipp Jahoda
 * @author Oleksandr Tyshkovets <olexandr.tyshkovets@gmail.com>
 */
public class LargeValueFormatter implements IValueFormatter, IAxisValueFormatter
{

    private static String[] SUFFIX = new String[]{
            "", "k", "m", "b", "t"
    };
    private static final int MAX_LENGTH = 5;
    private DecimalFormat mFormat;
    private String mText = "";
    private int sampleRate = 44100; // Default
    private int windowSize = 4096; // Default


    public LargeValueFormatter() {

        mFormat = new DecimalFormat("###E00");
//        mFormat.setRoundingMode(RoundingMode.CEILING);
    }

    public LargeValueFormatter(int windowSize, int sampleRate) {
        mFormat = new DecimalFormat("###E00");
        this.sampleRate = sampleRate;
        this.windowSize = windowSize;
    }

    /**
     * Creates a formatter that appends a specified text to the result string
     *
     * @param appendix a text that will be appended
     */
    public LargeValueFormatter(String appendix) {
        this();
        mText = appendix;
    }

    public float convertToHz(float binNr) {
        return (binNr / windowSize * (sampleRate));

    }

    public float convertToBin(float Hz) {
        return Hz / (sampleRate) * windowSize;

    }

    // So the spectrum is plotted as just arbitrary bins. The way to map it to Hz is to modify the way the Hz are displayed, not to generate
    // a larger range of bins up until the values that you want to reach in Hz. The transformation calculation SHOULD take place in the formatter.


    // IValueFormatter
    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
//        return makePretty(value * ((2*sampleRate)/(windowSize))) + mText;
        return makePretty(convertToHz(value)) + mText;

    }

    // IAxisValueFormatter
    // The valueformatter goes through the x values as defined by the dataset. These values are the bin numbers. In order to display Hz on the x-axis
    // we convert them to Hz values and when they fall close to 5, 10, 15 or 20k mark, we apply those respective labels.
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        float valueInHz = convertToHz(value);
//        Log.i("LargeValueFormatter", Float.toString(valueInHz));
        if ((valueInHz > 4990) && (valueInHz < 5010)) {
            return "5kHz";
        } else {
            return "";
        }

    }

    /**
     * Set an appendix text to be added at the end of the formatted value.
     *
     * @param appendix
     */
    public void setAppendix(String appendix) {
        this.mText = appendix;
    }

    /**
     * Set custom suffix to be appended after the values.
     * Default suffix: ["", "k", "m", "b", "t"]
     *
     * @param suff new suffix
     */
    public void setSuffix(String[] suff) {
        SUFFIX = suff;
    }

    /**
     * Formats each number properly. Special thanks to Roman Gromov
     * (https://github.com/romangromov) for this piece of code.
     */
    private String makePretty(double number) {

        String r = mFormat.format(number);

        int numericValue1 = Character.getNumericValue(r.charAt(r.length() - 1));
        int numericValue2 = Character.getNumericValue(r.charAt(r.length() - 2));
        int combined = Integer.valueOf(numericValue2 + "" + numericValue1);

        r = r.replaceAll("E[0-9][0-9]", SUFFIX[combined / 3]);

        while (r.length() > MAX_LENGTH || r.matches("[0-9]+\\.[a-z]")) {
            r = r.substring(0, r.length() - 2) + r.substring(r.length() - 1);
        }

        return r;
    }

    public int getDecimalDigits() {
        return 0;
    }
}