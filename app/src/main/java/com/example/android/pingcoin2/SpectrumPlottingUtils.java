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

    public static void addEmptyData(LineChart chartView, int FFTSize) {
        float[] spectrumData = new float[FFTSize/4];

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

    public static void configureSpectrumChart(LineChart chart, int FFTSize, int samplingFrequency) {
        addEmptyData(chart, FFTSize);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setEnabled(false);


        YAxis rightYAxis = chart.getAxisRight();
        rightYAxis.setEnabled(false);

        chart.getAxisLeft().setDrawGridLines(false);

        leftAxis.setAxisMinimum(0);

        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new LargeValueFormatter());
        xAxis.setAxisLineColor(android.R.color.white);
        xAxis.setTextColor(Color.WHITE);
//        xAxis.setTextColor(android.R.color.white);

//        xAxis.setGranularityEnabled(true);
//        xAxis.setGranularity((5000/samplingFrequency)*FFTSize);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

    }

    public static void plotNaturalFrequency(LineChart chart, String naturalFrequencyLabel, float naturalFrequencyValue, int sampleRate, int windowSize) {
        // Initialize the limit line
        LimitLine cxdx = new LimitLine(0);


        XAxis bottomAxis = chart.getXAxis();
        float convertedXValue;

        // Determine which natural frequency we're plotting
        switch (naturalFrequencyLabel) {
            case "c0d2":
                convertedXValue = (naturalFrequencyValue/sampleRate)*windowSize/2;
                cxdx = new LimitLine(convertedXValue, naturalFrequencyLabel);
                cxdx.setLineWidth(0.02f * convertedXValue);
            case "c0d3":
                convertedXValue = (naturalFrequencyValue/sampleRate) * windowSize / 2;
                cxdx = new LimitLine(convertedXValue, naturalFrequencyLabel);
                cxdx.setLineWidth(0.02f * convertedXValue);
            case "c0d4":
                convertedXValue = (naturalFrequencyValue/sampleRate)*windowSize/2;
                cxdx = new LimitLine(convertedXValue, naturalFrequencyLabel);
                cxdx.setLineWidth(0.02f * convertedXValue);
            default:
                break;
        }

        cxdx.setLineColor( argb(40, 190, 190, 190));
//        cxdx.enableDashedLine(10f, 10f, 0f);
        cxdx.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        cxdx.setTextSize(10f);
        cxdx.setTextColor(Color.DKGRAY);

        bottomAxis.addLimitLine(cxdx);

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
