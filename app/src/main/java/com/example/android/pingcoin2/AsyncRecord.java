package com.example.android.pingcoin2;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.example.android.fftpack.RealDoubleFFT;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.log10;


/**
 * Created by jmscdch on 24/01/18.
 */

public class AsyncRecord extends AsyncTask<Void, Void, Void> {

    String TAG = "AsyncRecord";
    private final Activity activity;

    private Context mContext;
    private View rootView;

    private RealDoubleFFT transformer;
//    private RealDoubleFFT transformer = new RealDoubleFFT(512);
    boolean heard;
    short[] audioData = new short[] {};
    double[] spectrumData = new double[] {};


    public AsyncRecord(Activity _activity) {
        this.activity = _activity;
    }


    @Override
    protected Void doInBackground(Void... voids) {

        Log.e("AsyncRecord", "Are we reaching here at least?");

        // Instantiate a clapper
        LoudNoiseDetector pingDetector = new LoudNoiseDetector();
//        ConsistentFrequencyDetector pingDetector = new ConsistentFrequencyDetector(100, 3000, 200);

        // Pass the clapper to a recorder
//        AudioClipRecorder pingRecorder = new AudioClipRecorder(pingDetector, this);
        PingRecorder pingRecorder = new PingRecorder(pingDetector, this);

        // Start recording
//        pingRecorder.startRecording();
        Log.e("AsyncRecord", "reached here");
        this.heard = false;
        PingRecording pingRecording;
        publishProgress();

        try {
            //start recording
            Log.e("AsyncRecord", "starting recording");
//            heard = pingRecorder.startRecording().heard;
//            this.heard = pingRecorder.startRecording().heard;
//            this.audioData = pingRecorder.startRecording().audioData;
            pingRecording = pingRecorder.startRecording();
            this.heard = pingRecording.heard;
            this.audioData = pingRecording.audioData;
            publishProgress();
        } catch (IllegalStateException se) {
            Log.e("AsyncRecord", "failed to record, recorder not setup properly", se);
            this.heard = false;
        } catch (RuntimeException se) {
            Log.e("AsyncRecord", "failed to record, recorder already being used.", se);
            this.heard = false;
        }

        return null;

    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);

        Log.e("AsyncRecord", "Reached here in onprogressupdate");
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        double[] toTransform = ShortArrayToDoubleArray.convertFromShortArrayToDoubleArray(this.audioData);

        RealDoubleFFT transformer = new RealDoubleFFT(toTransform.length);

        Log.i("AsyncRecord", Arrays.toString(toTransform));

        transformer.ft(toTransform);


        double [] spectrumAmpOutTmp;
        int fftlen = toTransform.length;
        spectrumAmpOutTmp= new double[fftlen/2+1];

        AudioUtil boris = new AudioUtil();
        boris.fftToAmp(spectrumAmpOutTmp, toTransform);

        this.spectrumData = spectrumAmpOutTmp;



        Log.i("AsyncRecord", Arrays.toString(spectrumAmpOutTmp));

//        float[] floatArray = new float[]{};
//        AudioUtil.doubleToFloat(floatArray, spectrumAmpOutTmp);

//        float[] floatArray = new float[spectrumAmpOutTmp.length];
//        for (int i = 0 ; i < spectrumAmpOutTmp.length; i++)
//        {
//            // Normal version
//            floatArray[i] = (float) (spectrumAmpOutTmp[i]);
//
//        }
        // Async should end here

        LineChart chart = this.activity.findViewById(R.id.chart);





        Log.i(TAG, "Generating float array");
        float[] floatArray = new float[this.spectrumData.length];
        for (int i = 0 ; i < this.spectrumData.length; i++)
        {
            floatArray[i] = (float) this.spectrumData[i];
        }
        Log.i(TAG, "floatArray: " + Arrays.toString(floatArray));

        // Plot the spectrum
        Log.i(TAG, "Plotting the spectrum");

        SpectrumPlottingUtils.addData(chart, floatArray);

        // Peak detection
        Log.i(TAG, "Starting peak detection with spectrumData: " + Arrays.toString(this.spectrumData));
        LinkedList<Integer> peaks = new LinkedList<Integer>();
        peaks = Peaks.findPeaks(this.spectrumData, 20, 0.0005, 0, false);

        Log.i(TAG, "Peaks :" + (peaks.toString()));

        // Plot detected frequencies
        Log.i(TAG, "Plotting detected frequencies");
        SpectrumPlottingUtils.plotDetectedFrequencies(chart, peaks);

        // Refresh the chart
        Log.i(TAG, "Refreshing the spectrum");
        chart.invalidate();


//        // Expected peaks plotting
//        Log.i(TAG, "Plotting expected peaks");
//        SpectrumPlottingUtils.plotNaturalFrequency(chart, "c0d2", SelectCoin.naturalFrequencyC0D2);
//        SpectrumPlottingUtils.plotNaturalFrequency(chart, "c0d3", SelectCoin.naturalFrequencyC0D3);
//        SpectrumPlottingUtils.plotNaturalFrequency(chart, "c0d4", SelectCoin.naturalFrequencyC0D4);


//        // Configure the spectrum chart
//        Log.i(TAG, "Configuring the spectrum chart");
//        SpectrumPlottingUtils.configureSpectrumChart(chart);





        // Refresh the chart
        Log.i(TAG, "Refreshing the chart");
        chart.invalidate();

        Log.i(TAG, "Finished postExecute in AsyncRecord");






    }
}
