package com.example.android.pingcoin2;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by jmscdch on 24/01/18.
 */

public class AsyncRecord extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... voids) {

        Log.e("AsyncRecord", "Are we reaching here at least?");

        // Instantiate a clapper
        LoudNoiseDetector pingDetector = new LoudNoiseDetector();
//        ConsistentFrequencyDetector pingDetector = new ConsistentFrequencyDetector(100, 3000, 200);

        // Pass the clapper to a recorder
        AudioClipRecorder pingRecorder = new AudioClipRecorder(pingDetector, this);


        // Start recording
//        pingRecorder.startRecording();
        Log.e("AsyncRecord", "reached here");
        boolean heard = false;
        publishProgress();

        try {
            //start recording
            Log.e("AsyncRecord", "starting recording");
            heard = pingRecorder.startRecording();
            publishProgress();
        } catch (IllegalStateException se) {
            Log.e("AsyncRecord", "failed to record, recorder not setup properly", se);
            heard = false;
        } catch (RuntimeException se) {
            Log.e("AsyncRecord", "failed to record, recorder already being used.", se);
            heard = false;
        }

        return null;

    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);

        Log.e("AsyncRecord", "Reached here in onprogressupdate");
    }
}
