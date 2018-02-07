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
        dataSet.setColor(android.R.color.black);
        dataSet.setFillColor(android.R.color.black);
        dataSet.setDrawCircles(false);
        dataSet.setDrawFilled(true);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chartView.setData(lineData);

//        Description description = new Description();
//        description.setText("");
//        chartView.setDescription(description);



    }

    public static void addEmptyData(LineChart chartView) {
        float[] spectrumData = new float[512];

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

    public static void configureSpectrumChart(LineChart chart) {
        addEmptyData(chart);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setEnabled(false);


        YAxis rightYAxis = chart.getAxisRight();
        rightYAxis.setEnabled(false);

        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);

        leftAxis.setAxisMinimum(0);
//        leftAxis.setAxisMaximum(0.01f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new XAxisValueFormatter());

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

    }

    public static void plotNaturalFrequency(LineChart chart, String naturalFrequencyLabel, float naturalFrequencyValue) {
        // Initialize the limit line
        LimitLine cxdx = new LimitLine(0);

        XAxis bottomAxis = chart.getXAxis();

        // Determine which natural frequency we're plotting
        switch (naturalFrequencyLabel) {
            case "c0d2":
                cxdx = new LimitLine(naturalFrequencyValue / (44100/1024), naturalFrequencyLabel);
            case "c0d3":
                cxdx = new LimitLine(naturalFrequencyValue / (44100/1024), naturalFrequencyLabel);
            case "c0d4":
                cxdx = new LimitLine(naturalFrequencyValue / (44100/1024), naturalFrequencyLabel);
            default:
                break;
        }

        cxdx.setLineWidth(0.5f);
        cxdx.setLineColor(Color.GRAY);
        cxdx.enableDashedLine(10f, 10f, 0f);
        cxdx.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        cxdx.setTextSize(10f);

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
