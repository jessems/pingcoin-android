package com.example.android.pingcoin2;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.example.android.fftpack.RealDoubleFFT;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by jmscdch on 24/01/18.
 */

public class AsyncRecord extends AsyncTask<Void, Void, Void> {

    private final Activity activity;

    private Context mContext;
    private View rootView;

    private RealDoubleFFT transformer;
//    private RealDoubleFFT transformer = new RealDoubleFFT(512);
    boolean heard;
    short[] audioData = new short[] {};


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
        publishProgress();

        try {
            //start recording
            Log.e("AsyncRecord", "starting recording");
//            heard = pingRecorder.startRecording().heard;
            this.heard = pingRecorder.startRecording().heard;
            this.audioData = pingRecorder.startRecording().audioData;
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

        transformer.ft(toTransform);
//        publishProgress(toTransform);

        float[] floatArray = new float[toTransform.length];
        for (int i = 0 ; i < toTransform.length; i++)
        {
            floatArray[i] = (float) Math.sqrt((float) toTransform[i] * (float) toTransform[i]);
        }


        LineChart chart = (LineChart) this.activity.findViewById(R.id.chart);
//
//        List<Entry> entries = new ArrayList<Entry>();
//        for(int i = 0; i < this.audioData.length; i++) {
//            entries.add(new Entry(i,this.audioData[i]));
//        }


        List<Entry> entries = new ArrayList<Entry>();
        for(int i = 0; i < floatArray.length; i++) {
            entries.add(new Entry(i,floatArray[i]));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
        dataSet.setDrawCircles(false);
//        dataSet.setDrawFilled(true);
//        dataSet.setColor(...);
//        dataSet.setValueTextColor(...); // styling, ...

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // refresh


//        Arrays.asList(this.audioData);


    }
}
