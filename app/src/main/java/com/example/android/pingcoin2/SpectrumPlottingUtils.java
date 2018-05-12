package com.example.android.pingcoin2;

import android.graphics.Color;
import android.util.Log;
import android.view.View;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.charts.LineChart;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.github.mikephil.charting.data.LineData;

import static android.graphics.Color.argb;


/**
 * Created by jmscdch on 04/02/18.
 */

public class SpectrumPlottingUtils {
    static String TAG = "SpectrumPlottingUtil";

    public static void addData(LineChart chartView, float[] spectrumData) {

        // Create entries List
        List<Entry> entries = new ArrayList<Entry>();
        for(int i = 0; i < spectrumData.length; i++) {
            entries.add(new Entry(i,spectrumData[i]));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Coin Spectrum"); // add entries to dataset
        dataSet.setDrawCircles(false);
        dataSet.setDrawFilled(true);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        lineData.setValueTextColor(android.R.color.white);
        chartView.setData(lineData);




    }

    public static void addEmptyData(LineChart chartView, int sampleRate, int windowSize) {
        float[] spectrumData = new float[windowSize/4];

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
        xAxis.setAxisMaximum(windowSize/4);
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new LargeValueFormatter(windowSize, sampleRate));
//        xAxis.set
        xAxis.setGranularity(5000/ ((2*sampleRate)/windowSize));

        xAxis.setAxisLineColor(android.R.color.white);
        xAxis.setTextColor(Color.WHITE);
//        xAxis.setTextColor(android.R.color.white);

//        xAxis.setGranularityEnabled(true);
//        xAxis.setGranularity((5000/samplingFrequency)*FFTSize);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

    }

    public static void plotNaturalFrequency(LineChart chart, String naturalFrequencyLabel, float naturalFrequencyValue, float naturalFrequencyError, int sampleRate, int windowSize) {
        // Initialize the limit line
        LimitLine cxdx = new LimitLine(0);


        XAxis bottomAxis = chart.getXAxis();
        float convertedXValue;

        // Determine which natural frequency we're plotting
        switch (naturalFrequencyLabel) {
            case "c0d2":
                convertedXValue = naturalFrequencyValue / ((2*sampleRate)/windowSize);
                cxdx = new LimitLine(convertedXValue, naturalFrequencyLabel);
                cxdx.setLineWidth(naturalFrequencyError/100 * convertedXValue);
            case "c0d3":
                convertedXValue = naturalFrequencyValue / ((2*sampleRate)/windowSize);
                cxdx = new LimitLine(convertedXValue, naturalFrequencyLabel);
                cxdx.setLineWidth(naturalFrequencyError/100 * convertedXValue);
            case "c0d4":
                convertedXValue = naturalFrequencyValue / ((2*sampleRate)/windowSize);
                cxdx = new LimitLine(convertedXValue, naturalFrequencyLabel);
                cxdx.setLineWidth(naturalFrequencyError/100 * convertedXValue);
            default:
                break;
        }

        cxdx.setLineColor( argb(40, 190, 190, 190));
//        cxdx.enableDashedLine(10f, 10f, 0f);
        cxdx.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        cxdx.setTextSize(10f);
        cxdx.setTextColor(Color.DKGRAY);

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





        chart.invalidate();

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
            limitLineList.add(new LimitLine(detectedFrequencies.get(k)));
            limitLineList.get(limitLineList.size() - 1).setLineWidth(0.5f);
        }


        for (int l = 0; l < limitLineList.size(); l++) {
            bottomAxis.addLimitLine(limitLineList.get(l));
//            Log.i(TAG, "plotting " + l);
        }
        chart.invalidate();
    }

}
