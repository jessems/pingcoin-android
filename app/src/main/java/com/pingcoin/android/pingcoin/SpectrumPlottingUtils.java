package com.pingcoin.android.pingcoin;

import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.charts.LineChart;


import org.apache.commons.math3.analysis.function.Exp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static android.graphics.Color.argb;


/**
 * Created by jmscdch on 04/02/18.
 */

public class SpectrumPlottingUtils {
    static String TAG = "SpectrumPlottingUtil";

    public static List<Entry> floatArrayToList(float[] floatArray) {
        List<Entry> entries = new ArrayList<Entry>();
        for(int i = 0; i < floatArray.length; i++) {
            entries.add(new Entry(i,floatArray[i]));
        }
        return entries;
    }

    public static void addData(LineChart chartView, float[] spectrumData, String label) {

        // Create entries List
        List<Entry> entries = new ArrayList<Entry>();
        for(int i = 0; i < spectrumData.length; i++) {
            entries.add(new Entry(i,spectrumData[i]));
        }

        LineDataSet dataSet = new LineDataSet(entries, label); // add entries to dataset
        dataSet.setDrawCircles(false);
        dataSet.setDrawFilled(true);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        lineData.setValueTextColor(android.R.color.white);
        chartView.setData(lineData);

        chartView.setDrawBorders(true);
        chartView.getData().setHighlightEnabled(false);
        chartView.setScaleEnabled(false);




    }

    public static void addEmptyData(LineChart chartView, int sampleRate, int windowSize) {
        float[] spectrumData = new float[windowSize];

        // Create entries List
        List<Entry> entries = new ArrayList<Entry>();
        for(int i = 0; i < spectrumData.length; i++) {
            entries.add(new Entry(i,0));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Coin Spectrum"); // add entries to dataset
        dataSet.setDrawCircles(false);
        dataSet.setDrawFilled(true);
        dataSet.setDrawValues(false);


        LineData lineData = new LineData(dataSet);
        chartView.setData(lineData);

        chartView.setDrawBorders(true);
//        chartView.setBorderColor(Color.rgb(40,40,40));
        chartView.setDrawBorders(false);
        chartView.getData().setHighlightEnabled(false);
        chartView.setScaleEnabled(false);
        chartView.setDrawGridBackground(true);
//        chartView.setGridBackgroundColor(Color.rgb(60,60,60));
        chartView.setGridBackgroundColor(Color.rgb(255,255,255));

    }



    public static void configureSpectrumChart(LineChart chart, int windowSize, int sampleRate) {



        addEmptyData(chart, sampleRate, windowSize);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setEnabled(false);


        YAxis rightYAxis = chart.getAxisRight();
        rightYAxis.setEnabled(false);

        chart.getAxisLeft().setDrawGridLines(false);

        leftAxis.setAxisMinimum(0);

        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(5);
        xAxis.setAxisMinimum(0);


        // The windowSize pertains to the blockSize that the PercussionOnsetDetector looks at.
        // If it looks at 512 samples, the frequency resolution will be half: 256.
        xAxis.setAxisMaximum(windowSize/2);


//        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(1000,true);

        xAxis.setValueFormatter(new LargeValueFormatter(windowSize, sampleRate));

//        xAxis.setLabelCount(442);

//        xAxis.set

        xAxis.setAxisLineColor(android.R.color.white);
        xAxis.setTextColor(Color.WHITE);
//        xAxis.setTextColor(android.R.color.white);

//        xAxis.setGranularityEnabled(true);
//        xAxis.setGranularity((5000/samplingFrequency)*FFTSize);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        chart.invalidate();

    }

    public static void plotNaturalFrequency(LineChart chart, String naturalFrequencyLabel, float naturalFrequencyValue, float naturalFrequencyError, int sampleRate, int windowSize) {
        Log.d("BLA",Float.toString(naturalFrequencyError));
        naturalFrequencyError = naturalFrequencyError / 2f;

        // Initialize the limit line
        ExpectedFrequencyLine cxdx = new ExpectedFrequencyLine(0,"");
        LimitLine cxdxBottomThreshold = new LimitLine(0);
        LimitLine cxdxTopThreshold = new LimitLine(0);


        XAxis bottomAxis = chart.getXAxis();
        float convertedXValue, convertedXValueBottomThreshold, convertedXValueTopThreshold;
        float lineWidth = 22f;

        // Determine which natural frequency we're plotting
        switch (naturalFrequencyLabel) {
            case "c0d2":
                convertedXValue = (naturalFrequencyValue / (sampleRate)) * windowSize;
                convertedXValueBottomThreshold = convertedXValue * (1 - naturalFrequencyError/2);
                convertedXValueTopThreshold = convertedXValue * (1 + naturalFrequencyError/2);
                cxdx = new ExpectedFrequencyLine(convertedXValue, naturalFrequencyLabel);
                lineWidth = convertedXValue * naturalFrequencyError;
                cxdx.setLineWidth(lineWidth);


                Log.i(TAG, "The " + naturalFrequencyLabel + " value loaded is: " + Float.toString(naturalFrequencyValue));
                Log.i(TAG, "The " + naturalFrequencyLabel + " value calculated is: " + Float.toString(convertedXValue));
                Log.i(TAG, "The " + naturalFrequencyLabel + " bottom threshold is : " + Float.toString(convertedXValueBottomThreshold));
                Log.i(TAG, "The " + naturalFrequencyLabel + " top threshold is : " + Float.toString(convertedXValueTopThreshold));

                Log.i("WHAT WHAT", Float.toString(lineWidth));


            case "c0d3":
                convertedXValue = (naturalFrequencyValue / (sampleRate)) * windowSize;
                convertedXValueBottomThreshold = convertedXValue * (1 - naturalFrequencyError);
                convertedXValueTopThreshold = convertedXValue * (1 + naturalFrequencyError);
                cxdx = new ExpectedFrequencyLine(convertedXValue, naturalFrequencyLabel);
                lineWidth = convertedXValue * naturalFrequencyError;
                cxdx.setLineWidth(lineWidth);



                Log.i(TAG, "The " + naturalFrequencyLabel + " value loaded is: " + Float.toString(naturalFrequencyValue));
                Log.i(TAG, "The " + naturalFrequencyLabel + " value calculated is: " + Float.toString(convertedXValue));
                Log.i(TAG, "The " + naturalFrequencyLabel + " bottom threshold is : " + Float.toString(convertedXValueBottomThreshold));
                Log.i(TAG, "The " + naturalFrequencyLabel + " top threshold is : " + Float.toString(convertedXValueTopThreshold));

                Log.i("WHAT WHAT", Float.toString(lineWidth));


            case "c0d4":
                convertedXValue = (naturalFrequencyValue / (sampleRate)) * windowSize;
                convertedXValueBottomThreshold = convertedXValue * (1 - naturalFrequencyError);
                convertedXValueTopThreshold = convertedXValue * (1 + naturalFrequencyError);
                cxdx = new ExpectedFrequencyLine(convertedXValue, naturalFrequencyLabel);
                lineWidth = convertedXValue * naturalFrequencyError;
                cxdx.setLineWidth(lineWidth);


                Log.i("WHAT WHAT", Float.toString(lineWidth));

                Log.i(TAG, "The " + naturalFrequencyLabel + " value loaded is: " + Float.toString(naturalFrequencyValue));
                Log.i(TAG, "The " + naturalFrequencyLabel + " value calculated is: " + Float.toString(convertedXValue));
                Log.i(TAG, "The " + naturalFrequencyLabel + " bottom threshold is : " + Float.toString(convertedXValueBottomThreshold));
                Log.i(TAG, "The " + naturalFrequencyLabel + " top threshold is : " + Float.toString(convertedXValueTopThreshold));


            default:
                break;
        }

//        cxdx.setLineWidth(22f);
        cxdx.setLineColor( argb(40, 240, 240, 240));
        cxdxBottomThreshold.setLineColor( argb(40, 240, 240, 240));
        cxdxTopThreshold.setLineColor( argb(40, 240, 240, 240));
//        cxdx.enableDashedLine(10f, 10f, 0f);
        cxdx.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        cxdx.setTextSize(10f);
        cxdx.setTextColor(Color.rgb(150,150,150));

        bottomAxis.setDrawLimitLinesBehindData(true);

        bottomAxis.addLimitLine(cxdx);

    }

    public static void detectedNaturalFrequency(LineChart chart, String frequencyLabel, boolean detected) {
        XAxis bottomAxis = chart.getXAxis();
        List<LimitLine> existingLimitLines = bottomAxis.getLimitLines();
        int detectedColor = Color.argb(80,0,153,51);
        int notDetectedColor = Color.argb(80,255,80,80);

            for(LimitLine limitline : existingLimitLines) {
                if (limitline.getLabel() == frequencyLabel) {
                    if (detected) {
                        limitline.setLineColor(detectedColor);
                    } else {
                        limitline.setLineColor(notDetectedColor);
                    }
                }
            }





//        chart.invalidate();

    }


    public static void plotDetectedFrequencies(LineChart chart, LinkedList<Integer> detectedFrequencies) {
        XAxis bottomAxis = chart.getXAxis();

        List<LimitLine> existingLimitLines = bottomAxis.getLimitLines();
        for (int p = 0; p < existingLimitLines.size(); p++) {
            LimitLine currentLimitLine = existingLimitLines.get(p);
            Log.i(TAG, "Current LimitLineLabel is :" + currentLimitLine.getLabel());
            if (currentLimitLine.getLabel().equals("")) {
                bottomAxis.removeLimitLine(currentLimitLine);
            }
        }





        List<LimitLine> limitLineList = new ArrayList<>();
        for (int k = 0; k < detectedFrequencies.size(); k++) {
            Log.i(TAG,"Iterating through detected frequency: " + detectedFrequencies.get(k));

            LimitLine detectedFreqLine = new LimitLine(detectedFrequencies.get(k));
            detectedFreqLine.setLineColor( argb(40, 255, 255, 0));
            limitLineList.add(detectedFreqLine);

            limitLineList.get(limitLineList.size() - 1).setLineWidth(0.5f);
        }


        for (int l = 0; l < limitLineList.size(); l++) {
            bottomAxis.addLimitLine(limitLineList.get(l));
//            Log.i(TAG, "plotting " + l);
        }
        chart.invalidate();
    }

}
