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
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;


import org.apache.commons.math3.analysis.function.Exp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.graphics.Color.argb;


/**
 * Created by jmscdch on 04/02/18.
 */

public class SpectrumPlottingUtils {
    static String TAG = "SpectrumPlottingUtil";

    public static List<Entry> floatArrayToList(float[] floatArray) {
        List<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < floatArray.length; i++) {
            entries.add(new Entry(i, floatArray[i]));
        }
        return entries;
    }

    public static void addData(LineChart chartView, float[] spectrumData, String label) {

        // Create entries List
        List<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < spectrumData.length; i++) {
            entries.add(new Entry(i, spectrumData[i]));
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
        for (int i = 0; i < spectrumData.length; i++) {
            entries.add(new Entry(i, 0));
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
        chartView.setGridBackgroundColor(Color.rgb(255, 255, 255));

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
        xAxis.setAxisMaximum(windowSize / 2);


//        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(1000, true);

        xAxis.setValueFormatter(new LargeValueFormatter(windowSize, sampleRate));

//        xAxis.setLabelCount(442);

//        xAxis.set

        xAxis.setAxisLineColor(android.R.color.white);
        xAxis.setTextColor(Color.WHITE);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        chart.invalidate();

    }

    public static void plotExpectedNaturalFrequencyToleranceBar(LineChart chart, String naturalFrequencyLabel, float naturalFrequencyValue, float naturalFrequencyError, int sampleRate, int windowSize) {
        Log.d("BLA", Float.toString(naturalFrequencyError));

        // Set default error value (width of the tolerance bar)
        naturalFrequencyError = naturalFrequencyError / 2f;

        // Initialize the limit line
        ExpectedFrequencyLine cxdx;
        ExpectedFrequencyLine cxdxBottomThreshold;
        ExpectedFrequencyLine cxdxTopThreshold;


        XAxis bottomAxis = chart.getXAxis();
        float convertedXValue, convertedXValueBottomThreshold, convertedXValueTopThreshold;
        float lineWidth = 22f;
        float convertedLinewidth;

        // Determine which natural frequency we're plotting
        // TODO: The threshold width is still not 100% accurate, especially for C0D4. There seems to be 1 pixel discrepancy.
        convertedXValue = (naturalFrequencyValue / (sampleRate)) * windowSize;
        convertedXValueBottomThreshold = convertedXValue * (1 - naturalFrequencyError);
        convertedXValueTopThreshold = convertedXValue * (1 + naturalFrequencyError);

        cxdx = new ExpectedFrequencyLine(convertedXValue, naturalFrequencyLabel);
        cxdxBottomThreshold = new ExpectedFrequencyLine(convertedXValueBottomThreshold, naturalFrequencyLabel);
        cxdxTopThreshold = new ExpectedFrequencyLine(convertedXValueTopThreshold, naturalFrequencyLabel + " top");

        // Translate lineWidth into Dp (because that's what setLineWidth() asks for
        lineWidth = convertedXValue * naturalFrequencyError;
        chart.getTransformer(YAxis.AxisDependency.LEFT).getPixelForValues(lineWidth, 0);
        convertedLinewidth = Utils.convertPixelsToDp(lineWidth);

        cxdx.setLineWidth(convertedLinewidth);
//        cxdxBottomThreshold.setLineWidth(1);
//        cxdxTopThreshold.setLineWidth(1);

        Log.i(TAG, "Distance between threshold bottom and threshold top: " + Float.toString(convertedXValueTopThreshold - convertedXValueBottomThreshold));
        Log.i(TAG, "Line width: " + Float.toString(lineWidth));

        Log.i(TAG, "The " + naturalFrequencyLabel + " value loaded is: " + Float.toString(naturalFrequencyValue));
        Log.i(TAG, "The " + naturalFrequencyLabel + " value calculated is: " + Float.toString(convertedXValue));
        Log.i(TAG, "The " + naturalFrequencyLabel + " bottom threshold is : " + Float.toString(convertedXValueBottomThreshold));
        Log.i(TAG, "The " + naturalFrequencyLabel + " top threshold is : " + Float.toString(convertedXValueTopThreshold));


        cxdx.setLineColor(argb(40, 240, 240, 240));
        cxdxBottomThreshold.setLineColor(argb(100, 0, 0, 240));
        cxdxTopThreshold.setLineColor(argb(100, 0, 0, 240));
        cxdx.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        cxdx.setTextSize(10f);
        cxdx.setTextColor(Color.rgb(150, 150, 150));

        bottomAxis.setDrawLimitLinesBehindData(true);

        bottomAxis.addLimitLine(cxdx);
//        bottomAxis.addLimitLine(cxdxBottomThreshold);
//        bottomAxis.addLimitLine(cxdxTopThreshold);

    }

    public static void setExpectedNaturalFrequencyToleranceBarColor(LineChart chart, String frequencyLabel, boolean detected) {
        XAxis bottomAxis = chart.getXAxis();
        List<LimitLine> existingLimitLines = bottomAxis.getLimitLines();
        int detectedColor = Color.argb(80, 0, 153, 51);
        int notDetectedColor = Color.argb(80, 255, 80, 80);

        for (Iterator<LimitLine> iterator = existingLimitLines.iterator(); iterator.hasNext(); ) {
            LimitLine limitLine = iterator.next();
            if (limitLine.getLabel().equals(frequencyLabel)) {
                if (detected) {
                    limitLine.setLineColor(detectedColor);
                } else {
                    limitLine.setLineColor(notDetectedColor);
                }
            }
        }

        chart.invalidate();

    }


    public static void drawDetectedPeaks(LinkedList<Integer> detectedPeaksBins, LineChart chart) {
        XAxis bottomAxis = chart.getXAxis();

        // Remove existing LimitLines corresponding to the detected peaks of the previous frame (they have label "")
        List<LimitLine> existingLimitLines = bottomAxis.getLimitLines();

        List<LimitLine> limitLinesToRemove = new ArrayList<LimitLine>();

        for (LimitLine limitLine : existingLimitLines) {
            if (limitLine.getLabel().equals("")) {
                limitLinesToRemove.add(limitLine);
            }
        }

        existingLimitLines.removeAll(limitLinesToRemove);


//        Iterator<LimitLine> iterator = existingLimitLines.iterator();

//        while (iterator.hasNext()) {
//            LimitLine currentLimitLine = iterator.next();
//            if (!currentLimitLine.getLabel().equals("c0d2")
//                    || !currentLimitLine.getLabel() .equals("c0d3")
//                    || !currentLimitLine.getLabel().equals("c0d4")) {
//                // If the limitline has no label, it is a drawn detected peak
//                bottomAxis.removeLimitLine(currentLimitLine);
//            }
//        }



//        bottomAxis.removeAllLimitLines();


        if (detectedPeaksBins.size() < 10) {

            for (Iterator<Integer> iterator = detectedPeaksBins.iterator(); iterator.hasNext(); ) {
                float value = iterator.next();
                LimitLine detectedFreqLine = new LimitLine(value);
                detectedFreqLine.setLineColor(argb(255, 255, 0, 0));
                detectedFreqLine.setLineWidth(0.5f);
                detectedFreqLine.setLabel("");
                bottomAxis.addLimitLine(detectedFreqLine);
            }
        } else {
            Log.e("drawDetectedPeaks", "Too many LimitLines on the graph!");
        }


//        List<LimitLine> existingLimitLines = bottomAxis.getLimitLines();
//
//        for (Iterator<LimitLine> iterator = existingLimitLines.iterator(); ((Iterator) iterator).hasNext()) {
//            LimitLine currentLimitLine = iterator.next();
//            if (currentLimitLine.getLabel().equals("")) {
//                // If the limitline has no label, it is a drawn detected peak
//                bottomAxis.removeLimitLine(currentLimitLine);
//            }
//        }


//        for (int p = 0; p < existingLimitLines.size(); p++) {
//            LimitLine currentLimitLine = existingLimitLines.get(p);
//            Log.i(TAG, "Current LimitLineLabel is :" + currentLimitLine.getLabel());
//            if (currentLimitLine.getLabel().equals("")) {
//                bottomAxis.removeLimitLine(currentLimitLine);
//            }
//        }

//        List<LimitLine> limitLineList = new ArrayList<>();
//        for (Iterator<Integer> iterator = detectedPeaksBins.iterator(); iterator.hasNext(); ) {
//            float value = iterator.next();
//            LimitLine detectedFreqLine = new LimitLine(value);
//            detectedFreqLine.setLineColor( argb(255, 255, 0, 0));
//            detectedFreqLine.setLineWidth(0.5f);
////            limitLineList.add(detectedFreqLine);
//            bottomAxis.addLimitLine(detectedFreqLine);
//        }

//
//        List<LimitLine> limitLineList = new ArrayList<>();
//        for (int k = 0; k < detectedPeaksBins.size(); k++) {
//            Log.i(TAG,"Iterating through detected frequency: " + detectedPeaksBins.get(k));
//            LimitLine detectedFreqLine = new LimitLine((float) detectedPeaksBins.get(k));
//            detectedFreqLine.setLineColor( argb(255, 255, 0, 0));
//            limitLineList.add(detectedFreqLine);
//
//            limitLineList.get(limitLineList.size() - 1).setLineWidth(0.5f);
//        }


//        for (int l = 0; l < limitLineList.size(); l++) {
//            bottomAxis.addLimitLine(limitLineList.get(l));
////            Log.i(TAG, "plotting " + l);
//        }
    }

    public static void plotNoiseFloor(LineChart chart, LineData chartData, List<Entry> magnitudesList, List<Entry> noiseFloorList) {


        // Below code works to plot the real time spectrum and the noise floor.
        LineDataSet dataSet1 = new LineDataSet(magnitudesList, "Current Magnitudes"); // add entries to dataset
        LineDataSet dataSet2 = new LineDataSet(noiseFloorList, "Noise Floor"); // add entries to dataset

        dataSet1.setDrawCircles(false);
        dataSet1.setDrawFilled(true);
        dataSet1.setDrawValues(false);
        dataSet1.setColor(Color.BLACK);

        dataSet2.setDrawCircles(false);
        dataSet2.setDrawFilled(true);
        dataSet2.setDrawValues(false);
        dataSet2.setColor(Color.RED);


        chartData.addDataSet(dataSet1);
        chartData.addDataSet(dataSet2);

        chart.setData(chartData);
//        chart.invalidate();

    }

    public static void plotSpectrum(LineChart chart, LineData chartData, List<Entry> magnitudesList) {
        LineDataSet dataSet1 = new LineDataSet(magnitudesList, "Current Magnitudes"); // add entries to dataset

        dataSet1.setDrawCircles(false);
        dataSet1.setDrawFilled(true);
        dataSet1.setDrawValues(false);
        dataSet1.setColor(Color.BLACK);


        chartData.addDataSet(dataSet1);
        chart.setData(chartData);

        chart.invalidate();
    }

    public static void plotBinnedPeaks(LineChart chart, LineData chartData, List<Entry> binnedPeaksList) {
        LineDataSet dataSet3 = new LineDataSet(binnedPeaksList, "Binned Peaks");

        dataSet3.setDrawCircles(false);
        dataSet3.setDrawFilled(true);
        dataSet3.setDrawValues(false);
        dataSet3.setColor(R.color.colorAccent);


        chartData.addDataSet(dataSet3);

        chart.setData(chartData);
//        chart.invalidate();
    }

    public static Map<String, Boolean> getRecognizedResonanceFrequencies(LinkedList<Float> binnedPeaksExceedingThreshHz, Map<String, Float> resonanceFrequencies) {
        boolean C0D2Detected = false;
        boolean C0D3Detected = false;
        boolean C0D4Detected = false;
        Map<String, Boolean> recognizedResonanceFrequencies = new HashMap();

        for (float realPeakvalue : binnedPeaksExceedingThreshHz) {
            float peak = realPeakvalue;
            if ((peak > ((float) resonanceFrequencies.get("C0D2") * (1 - resonanceFrequencies.get("tolerance")))) && (peak < ((float) resonanceFrequencies.get("C0D2") * (1 + resonanceFrequencies.get("tolerance"))))) {
                C0D2Detected = true;
            } else if ((peak > ((float) resonanceFrequencies.get("C0D3") * (1 - resonanceFrequencies.get("tolerance")))) && (peak < ((float) resonanceFrequencies.get("C0D3") * (1 + resonanceFrequencies.get("tolerance"))))) {
                C0D3Detected = true;
            } else if ((peak > ((float) resonanceFrequencies.get("C0D4") * (1 - resonanceFrequencies.get("tolerance")))) && (peak < ((float) resonanceFrequencies.get("C0D4") * (1 + resonanceFrequencies.get("tolerance"))))) {
                C0D4Detected = true;
            }
        }
        recognizedResonanceFrequencies.put("C0D2", C0D2Detected);
        recognizedResonanceFrequencies.put("C0D3", C0D3Detected);
        recognizedResonanceFrequencies.put("C0D4", C0D4Detected);

        return recognizedResonanceFrequencies;
    }

    public static Map<String, Double> getSpectralFeatures(float[] currentMagnitudes) {

        Map<String, Double> spectralFeatures = new HashMap();

        double[] doubleArray = new double[currentMagnitudes.length];
        for (int i = 0; i < currentMagnitudes.length; i++) {
            doubleArray[i] = (double) Math.abs(currentMagnitudes[i]);
        }

        double gm = MeanCalculator.geometricMean(doubleArray);
        double am = MeanCalculator.arithmeticMean(doubleArray);
        final double spectralFlatness = convertToDesiredDecimals(gm / am, 2);
        final double inverseSpectralFlatness = 1/spectralFlatness;
        final double spectralCentroid = convertToDesiredDecimals(MeanCalculator.spectralCentroid(doubleArray), 2);
        final double spectralMedian = convertToDesiredDecimals(MeanCalculator.spectralMedian(doubleArray), 2);
        final double spectralBandwidth = convertToDesiredDecimals(MeanCalculator.spectralBandwidth(doubleArray), 2);
//        Log.i("Spectral Flatness: ", Double.toString(spectralFlatness));
//        Log.i("ISpectral Flatness: ", Double.toString(inverseSpectralFlatness));
//        Log.i("Spectral Median: ", Double.toString(spectralMedian));

        // Convert features to 2 decimals

        spectralFeatures.put("spectralCentroid", spectralCentroid);
        spectralFeatures.put("spectralFlatness", spectralFlatness);
        spectralFeatures.put("inverseSpectralFlatness", inverseSpectralFlatness);
        spectralFeatures.put("spectralMedian", spectralMedian);
        spectralFeatures.put("spectralBandwidth", spectralBandwidth);

        return spectralFeatures;

    }


    public static double convertToDesiredDecimals(double feature, double decimals) {
        double decimalsMultiplier = Math.pow(10, decimals);
        return (double) Math.round(feature * decimalsMultiplier) / decimalsMultiplier;
    }

}
