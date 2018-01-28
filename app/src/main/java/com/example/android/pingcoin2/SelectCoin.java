package com.example.android.pingcoin2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class SelectCoin extends AppCompatActivity {

    private TextView mTextMessage;
    TextView mDisplayCoinParameters;
//    final static String COIN_API_URL = "https://pataguides-1490028016004.appspot.com/_ah/api/echo/v1/coins";
    final static String COIN_API_URL = "http://192.168.1.109:8080/_ah/api/echo/v1/coins";



    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("coin_api_response.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }
//    JSONObject obj = new JSONObject(loadJSONFromAsset(this));



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    private static final int PERMISSION_RECORD_AUDIO = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_coin);

        LoudNoiseDetector pingDetector = new LoudNoiseDetector();
//        ConsistentFrequencyDetector pingDetector = new ConsistentFrequencyDetector(100, 3000, 200);

        LineChart chart = findViewById(R.id.chart);

        final AudioClipRecorder pingRecorder = new AudioClipRecorder(pingDetector);
//        final PingRecorder pingRecorder = new PingRecorder(pingDetector);

        // TODO put Async call into the onClick so it can be executed again.


//        final AsyncRecord pingContinuousRecord = new AsyncRecord(this);
        AsyncRecord pingContinuousRecord;
        final Activity _main_activity = this;

        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (ContextCompat.checkSelfPermission(SelectCoin.this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions(SelectCoin.this,
                        new String[] { Manifest.permission.RECORD_AUDIO },
                        PERMISSION_RECORD_AUDIO);
                return;
            }
            AsyncRecord outter.pingContinuousRecord = new AsyncRecord(_main_activity);
            // Permission already available
//            pingRecorder.startRecording();
            pingContinuousRecord.execute();
            Log.e("SelectCoin", "Async Record executed");

        }
        });

        findViewById(R.id.btnStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                pingRecorder.stopRecording();
//                pingRecorder.done();


                if (pingContinuousRecord.getStatus() == AsyncTask.Status.RUNNING) {
                    pingContinuousRecord.cancel(true);
                    Log.e("SelectCoin", "Ping recording cancelled.");
                }
            }
        });

//        AudioClipListener pingListener = new AudioClipListener() {
//            @Override
//            public boolean heard(short[] audioData, int sampleRate) {
//                return false;
//            }
//        };
//
//        AudioClipRecorder pingRecorder = new AudioClipRecorder(pingListener);
//        pingRecorder.startRecording(44100, AudioFormat.ENCODING_PCM_16BIT);
//        pingRecorder.startRecordingForTime(100, 44100, AudioFormat.ENCODING_PCM_16BIT);

//        ConsistentFrequencyDetector pingDetector = new ConsistentFrequencyDetector(100, 100, 2000);
//        ContextCompat.checkSelfPermission(AudioRecordActivity.this, Manifest.permission.RECORD_AUDIO)
//        LoudNoiseDetector pingDetector = new LoudNoiseDetector();
//        AudioClipRecorder pingRecorder = new AudioClipRecorder(pingDetector);
//
//
//        pingRecorder.startRecordingForTime(1000, pingRecorder.RECORDER_SAMPLERATE_8000, AudioFormat.ENCODING_PCM_16BIT);
//        pingRecorder.done();

//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            public void run() {
//                pingRecorder.stopRecording();
//                pingRecorder.done();
//            }
//        }, 10000);




        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mDisplayCoinParameters = (TextView) findViewById(R.id.coin_parameters);

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, COIN_API_URL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("response", response.toString());
                        try {
                            JSONArray mCoinArray = (JSONArray) response.get("coins");
                            List<String> spinnerArray =  new ArrayList<String>();

                            for(int i=0; i<mCoinArray.length(); i++) {
                                JSONObject coinObject = mCoinArray.getJSONObject(i);
                                spinnerArray.add(coinObject.getString("fullName"));
                                Log.i("coinObject", coinObject.toString());
                            }
                            ArrayAdapter<String> adapter;
                            adapter = new ArrayAdapter<String>(
                                    SelectCoin.this, android.R.layout.simple_spinner_item, spinnerArray);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            Spinner sItems = (Spinner) findViewById(R.id.coin_selector);
                            sItems.setAdapter(adapter);

                            mDisplayCoinParameters.setText("Response: " + response.getString("coins"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
        });
        Log.i("Singleton Reached", "singleton");
        VolleyLog.DEBUG = true;
        Log.isLoggable("Volley", Log.VERBOSE);
        MySingleton.getInstance(this).addToRequestQueue(jsObjRequest);

    }

    public void onClickSelectCoin(View view){
        TextView coinDetails = (TextView) findViewById(R.id.coin_details);
        Spinner coinSelector = (Spinner) findViewById(R.id.coin_selector);
        String selectedCoin = String.valueOf(coinSelector.getSelectedItem());
        coinDetails.setText(selectedCoin);
    }








}