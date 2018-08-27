package com.pingcoin.android.pingcoin;

import android.Manifest;
import android.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.VolleyLog;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.graphics.Color.argb;

// TODO: Close cursor.close()

public class TestCoin extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private boolean debugNoiseFloor = false;
    private Thread audioProcessorThread;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private AudioDispatcher dispatcher;




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }





    private TextView mTextMessage;
    TextView mDisplayCoinParameters;
//    final static String COIN_API_URL = "https://pataguides-1490028016004.appspot.com/_ah/api/echo/v1/coins";
    final static String COIN_API_URL = "http://192.168.1.17:8080/_ah/api/echo/v1/coins";
    String TAG = "TestCoin";

//    private SQLiteDatabase db;

    static Long naturalFrequencyC0D2 = null;
    static Long naturalFrequencyC0D3 = null;
    static Long naturalFrequencyC0D4 = null;
    static float naturalFrequencyError = 0;

    final int sampleRate = 44100;
    final int windowSize = 4096;
    final int nFrames = 10;
    ArrayList<float[]> S = new ArrayList<>(nFrames);
    ArrayList<List> P = new ArrayList<>(nFrames);






    private static Context mContext;





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


    private SQLiteDatabase db;
    private Cursor cursor;


//    String selectedCoinId = getIntent().getStringExtra("id");





//    @Dao
//    public interface MyDao {
//        @Query("SELECT c0d2a, c0d3a, c0d4a FROM coin_table WHERE coin_id = :selectedCoinId LIMIT 1")
//        public LiveData<Coin> loadSingleCoin(String selectedCoinId);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_coin);

        // Prevent the screen from dimming
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);


        float selectedCoinC0D2a = 0;
        float selectedCoinC0D3a = 0;
        float selectedCoinC0D4a = 0;
        float selectedCoinError = 0;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //The key argument here must match that used in the other activity
            selectedCoinC0D2a = extras.getFloat("C0D2a");
            selectedCoinC0D3a = extras.getFloat("C0D3a");
            selectedCoinC0D4a = extras.getFloat("C0D4a");
            selectedCoinError = extras.getFloat("Error");
        }

        final String TAG = "TestCoin";

        Log.d(TAG, Float.toString(selectedCoinC0D2a));
        Log.d(TAG, Float.toString(selectedCoinC0D3a));
        Log.d(TAG, Float.toString(selectedCoinC0D4a));
        Log.d(TAG, Float.toString(selectedCoinError));












        mContext = this;
        VolleyLog.DEBUG = true;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},1);

        }




        // Get coins from SQLite db
        SQLiteOpenHelper coinDatabaseHelper = new PingcoinDatabaseHelper(this);


        SQLiteDatabase db = coinDatabaseHelper.getReadableDatabase();


        // Get coins

        final Cursor cursor = getAllCoinNames(db);

        // Convert coins to arraylist
        List itemIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            String itemId = cursor.getString(
                    cursor.getColumnIndexOrThrow(CoinContract.CoinEntry.COLUMN_FULL_NAME));
            itemIds.add(itemId);
        }


        // Use array adapter
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.spinner_item, itemIds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set spinner items to array adapter
        Spinner sItems = (Spinner) findViewById(R.id.coin_selector);
        sItems.setAdapter(adapter);

        final String selectedCoin = sItems.getSelectedItem().toString();



        naturalFrequencyC0D2 = (long) selectedCoinC0D2a;
        naturalFrequencyC0D3 = (long) selectedCoinC0D3a;
        naturalFrequencyC0D4 = (long) selectedCoinC0D4a;
        naturalFrequencyError = selectedCoinError;
//        Log.i("ERROR HERE: ", Long.toString(selectedCoinError));





//        Log.i(TAG, selectedCoin);
//
//        cursor.moveToPosition(-1);
//
//        // Loop through cursor again to obtain the natural frequencies for the selected coin
//        while(cursor.moveToNext()) {
//            String itemId = cursor.getString(
//                    cursor.getColumnIndexOrThrow(CoinContract.CoinEntry.COLUMN_FULL_NAME));
//            Log.i(TAG, itemId + " vs " + selectedCoin);
//
//            if (itemId.equals(selectedCoin)) {
//                naturalFrequencyC0D2 = cursor.getLong(
//                        cursor.getColumnIndexOrThrow(CoinContract.CoinEntry.COLUMN_C0D2A));
//                Log.i(TAG, naturalFrequencyC0D2.toString());
//                naturalFrequencyC0D3 = cursor.getLong(
//                        cursor.getColumnIndexOrThrow(CoinContract.CoinEntry.COLUMN_C0D3A));
//                Log.i(TAG, naturalFrequencyC0D3.toString());
//                naturalFrequencyC0D4 = cursor.getLong(
//                        cursor.getColumnIndexOrThrow(CoinContract.CoinEntry.COLUMN_C0D4A));
//                Log.i(TAG, naturalFrequencyC0D4.toString());
//                naturalFrequencyError = cursor.getLong(
//                        cursor.getColumnIndexOrThrow(CoinContract.CoinEntry.COLUMN_CD_ERROR));
//                Log.i(TAG, naturalFrequencyError.toString());
//
//            }
//
//        }
        // We need to close the database connection

        final LineChart chart = findViewById(R.id.chart);







        // Configure the spectrum chart
        Log.i(TAG, "Configuring the spectrum chart");
        SpectrumPlottingUtils.configureSpectrumChart(chart, windowSize, sampleRate);


//        sItems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Log.i(TAG,parent.getItemAtPosition(position).toString());
//
////                chart.clear();
////                SpectrumPlottingUtils.addEmptyData(chart, sampleRate, windowSize);
//
//                XAxis bottomAxis = chart.getXAxis();
//                bottomAxis.removeAllLimitLines();
//
//                cursor.moveToPosition(-1);
//                while(cursor.moveToNext()) {
//                    String itemId = cursor.getString(
//                            cursor.getColumnIndexOrThrow(CoinContract.CoinEntry.COLUMN_FULL_NAME));
//                    Log.i(TAG, itemId + " vs " + selectedCoin);
//                    if (itemId.equals(parent.getItemAtPosition(position).toString())) {
//                        naturalFrequencyC0D2 = cursor.getLong(
//                                cursor.getColumnIndexOrThrow(CoinContract.CoinEntry.COLUMN_C0D2A));
//                        Log.i(TAG, naturalFrequencyC0D2.toString());
//                        naturalFrequencyC0D3 = cursor.getLong(
//                                cursor.getColumnIndexOrThrow(CoinContract.CoinEntry.COLUMN_C0D3A));
//                        Log.i(TAG, naturalFrequencyC0D3.toString());
//                        naturalFrequencyC0D4 = cursor.getLong(
//                                cursor.getColumnIndexOrThrow(CoinContract.CoinEntry.COLUMN_C0D4A));
//                        Log.i(TAG, naturalFrequencyC0D4.toString());
//                        naturalFrequencyError = cursor.getLong(
//                                cursor.getColumnIndexOrThrow(CoinContract.CoinEntry.COLUMN_CD_ERROR));
//                        Log.i(TAG, naturalFrequencyError.toString());
//
//                        if (naturalFrequencyC0D2.intValue() != 0) {
//                            SpectrumPlottingUtils.plotNaturalFrequency(chart, "c0d2", naturalFrequencyC0D2, naturalFrequencyError, sampleRate, windowSize);
//                        }
//                        if (naturalFrequencyC0D3.intValue() != 0) {
//                            SpectrumPlottingUtils.plotNaturalFrequency(chart, "c0d3", naturalFrequencyC0D3, naturalFrequencyError, sampleRate, windowSize);
//                        }
//                        if (naturalFrequencyC0D4.intValue() !=0) {
//                            SpectrumPlottingUtils.plotNaturalFrequency(chart, "c0d4", naturalFrequencyC0D4, naturalFrequencyError, sampleRate, windowSize);
//                        }
//                        chart.invalidate();
//
//                    }
//
//                }
//
//
//
//            }
//
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                //Another interface callback
//            }
//
//
//        });




        // Expected peaks plotting
        Log.i(TAG, "Plotting expected peaks");
        Log.i(TAG, "C0D2: " + Float.toString(convertHzToBin(naturalFrequencyC0D2, windowSize, sampleRate)));
        SpectrumPlottingUtils.plotNaturalFrequency(chart, "c0d2", naturalFrequencyC0D2, naturalFrequencyError, sampleRate, windowSize);

        Log.i(TAG, "C0D3: " + Float.toString(convertHzToBin(naturalFrequencyC0D3, windowSize, sampleRate)));
        SpectrumPlottingUtils.plotNaturalFrequency(chart, "c0d3", naturalFrequencyC0D3, naturalFrequencyError, sampleRate, windowSize);

        Log.i(TAG, "C0D4: " + Float.toString(convertHzToBin(naturalFrequencyC0D4, windowSize, sampleRate)));
        SpectrumPlottingUtils.plotNaturalFrequency(chart, "c0d4", naturalFrequencyC0D4, naturalFrequencyError, sampleRate, windowSize);


        chart.invalidate();



        // Fill the Spectrogram
        for(int i=0; i<nFrames; i++) {
            S.add(new float[windowSize]);
        }

        final SpectralPeakProcessor spectralPeakFollower = new SpectralPeakProcessor(windowSize, (int) 0.75 * windowSize, sampleRate);
        final int medianFilterLength = 25;
        // If windowSize is 4096 -> noiseFloorFactor should be 1.15f
        // If windowSize is 2048 -> noiseFloorFactor should be 1.10f
        final float noiseFloorFactor = 1.15f;
        final int numberOfPeaks = 10;


        for (int i=0; i<nFrames; i++) {
            P.add(new ArrayList());
        }


        // The factory is a design method that returns an object for a method call.
        // In this case the fromDefaultMicrophone() method returns an AudioDispatcher object.

        this.dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate,windowSize,(int) 0.75 * windowSize);
        this.dispatcher.addAudioProcessor(spectralPeakFollower);


        this.dispatcher.addAudioProcessor(new AudioProcessor() {

            public void processingFinished() {
            }

            public boolean process(AudioEvent audioEvent) {
                final float [] currentMagnitudes = spectralPeakFollower.getMagnitudes();
                final float[] noiseFloor = SpectralPeakProcessor.calculateNoiseFloor(spectralPeakFollower.getMagnitudes(), medianFilterLength, noiseFloorFactor);

                // The first element of the noise floor is sometimes infinity. Here we filter that out.
                for (int i = 0; i < noiseFloor.length; i++) {
                    if (Float.isInfinite(noiseFloor[i])) {
                        noiseFloor[i] = 0;
                    } else {
                        // Do nothing
                    }
                }

                List<Integer> localMaxima = SpectralPeakProcessor.findLocalMaxima(spectralPeakFollower.getMagnitudes(), noiseFloor);
                List<SpectralPeakProcessor.SpectralPeak> list = SpectralPeakProcessor.findPeaks(spectralPeakFollower.getMagnitudes(), spectralPeakFollower.getFrequencyEstimates(), localMaxima, numberOfPeaks, 1);





                final ArrayList<List> peakogram = addSliceToPeakogram(P, list);

                final float[] binnedPeaks = getBinnedPeaks(P);
                final List<Entry> binnedPeaksList = SpectrumPlottingUtils.floatArrayToList(binnedPeaks);



                Log.i("Slice: ", list.toString());
                Log.i("Peakogram: ", peakogram.toString());


                // Get list of bin indexes where the value exceeds 5
                ArrayList<Integer> binnedPeaksExceedingThresh = new ArrayList<>();
                int binnedPeakThresh = 1;
                for (int k=0; k<binnedPeaks.length; k++) {
                    if (binnedPeaks[k] > binnedPeakThresh) {
                        binnedPeaksExceedingThresh.add(k);
                    } else {
                        // do nothing
                    }
                }

                Log.i(TAG,"binnedPeaksExceedingThreshold: " + binnedPeaksExceedingThresh.toString());



                // Convert bin indexes to Hz
                ArrayList<Float> binnedPeaksExceedingThreshHz = new ArrayList<>();
                for (int binnedPeak : binnedPeaksExceedingThresh) {
                    float binnedPeakHz = ((float) binnedPeak / windowSize) * sampleRate;
                    binnedPeaksExceedingThreshHz.add(binnedPeakHz);
                    Log.i("PeaksExcThreshHz", binnedPeaksExceedingThreshHz.toString());
                }


                // Compare to expected frequencies

                // Reset expected frequency colors:
                SpectrumPlottingUtils.detectedNaturalFrequency(chart, "c0d2", false);
                SpectrumPlottingUtils.detectedNaturalFrequency(chart, "c0d3", false);
                SpectrumPlottingUtils.detectedNaturalFrequency(chart, "c0d4", false);

                // Check if expected frequencies are detected
                boolean C0D2Detected = false;
                boolean C0D3Detected = false;
                boolean C0D4Detected = false;
                float tempC0D2 = TestCoin.naturalFrequencyC0D2;
                float tempC0D3 = TestCoin.naturalFrequencyC0D3;
                float tempC0D4 = TestCoin.naturalFrequencyC0D4;
                float tempErrorMargin = (float) TestCoin.naturalFrequencyError;

                Log.i(TAG, "Natural frequencies: " + tempC0D2 + " " + tempC0D3 + " " + tempC0D4);
                Log.i(TAG, "Sample Rate and Window Size: " + sampleRate + " " + windowSize);
                Log.i(TAG, "Original: " + TestCoin.naturalFrequencyC0D2);


                // Loop through each detected peak and compare with expected peaks
                // If it falls within the error margin, mark the detected boolean as true
                for (float realPeakvalue : binnedPeaksExceedingThreshHz) {
//                    float peak = ((float) realPeakvalue / windowSize) * sampleRate;
                    float peak = realPeakvalue;
                    if (peak > tempC0D2 * (1 - tempErrorMargin) && peak < tempC0D2 * (1 + tempErrorMargin)) {
                        C0D2Detected = true;
                        SpectrumPlottingUtils.detectedNaturalFrequency(chart, "c0d2", C0D2Detected);
                        Log.i(TAG, "Expected: " + tempC0D2 + ". Peak: " + peak + ", Lower limit: " + tempC0D2 * (1 - tempErrorMargin) + " + , Upper Limit : " + tempC0D2 * (1 + tempErrorMargin));
                    } else if (peak > tempC0D3 * (1 - tempErrorMargin) && peak < tempC0D3 * (1 + tempErrorMargin)) {
                        C0D3Detected = true;
                        SpectrumPlottingUtils.detectedNaturalFrequency(chart, "c0d3", C0D3Detected);
                        Log.i(TAG, "Expected: " + tempC0D3 + ". Peak: " + peak + ", Lower limit: " + tempC0D3 * (1 - tempErrorMargin) + " + , Upper Limit : " + tempC0D3 * (1 + tempErrorMargin));
                        chart.invalidate();
                    } else if (peak > tempC0D4 * (1 - tempErrorMargin) && peak < tempC0D4 * (1 + tempErrorMargin)) {
                        C0D4Detected = true;
                        SpectrumPlottingUtils.detectedNaturalFrequency(chart, "c0d4", C0D4Detected);
                        Log.i(TAG, "Expected: " + tempC0D4 + ". Peak: " + peak + ", Lower limit: " + tempC0D4 * (1 - tempErrorMargin) + " + , Upper Limit : " + tempC0D4 * (1 + tempErrorMargin));
                    }

                    Log.i("Peak detection", realPeakvalue + ": C0D2(" + C0D2Detected + ") C0D3(" + C0D3Detected + ") C0D4(" + C0D4Detected + ")");
                }

                Log.i(TAG, "C0D2 Detected: " + C0D2Detected);
                Log.i(TAG, "C0D3 Detected: " + C0D3Detected);
                Log.i(TAG, "C0D4 Detected: " + C0D4Detected);











                final List<Entry> magnitudesList = SpectrumPlottingUtils.floatArrayToList(currentMagnitudes);
                final List<Entry> noiseFloorList = SpectrumPlottingUtils.floatArrayToList(noiseFloor);


                final boolean finalC0D2Detected = C0D2Detected;
                final boolean finalC0D3Detected = C0D3Detected;
                final boolean finalC0D4Detected = C0D4Detected;
                final boolean allPeaksDetected = (C0D2Detected && C0D3Detected && C0D4Detected);


                runOnUiThread(new Runnable() {
                                  @Override
                                  public void run() {


                                      // Plot detected fr

                                      LineData chartData = new LineData();


                                      if (debugNoiseFloor == true) {

                                          // Below code works to plot the real time spectrum and the noise floor.
                                          LineDataSet dataSet1 = new LineDataSet(magnitudesList, "Current Magnitudes"); // add entries to dataset
                                          LineDataSet dataSet2 = new LineDataSet(noiseFloorList, "Noise Floor"); // add entries to dataset

                                          dataSet1.setDrawCircles(false);
                                          dataSet1.setDrawFilled(true);
                                          dataSet1.setDrawValues(false);
                                          dataSet1.setColor(android.R.color.holo_blue_light);

                                          dataSet2.setDrawCircles(false);
                                          dataSet2.setDrawFilled(true);
                                          dataSet2.setDrawValues(false);
                                          dataSet2.setColor(android.R.color.holo_red_light);


                                          chartData.addDataSet(dataSet1);
                                          chartData.addDataSet(dataSet2);

                                          chart.setData(chartData);

                                      } else {
                                          // do nothing
                                      }

                                      // Continue to update the graph after every block as long as NOT all 3 peaks are detected
                                      if (!allPeaksDetected) {


                                          LineDataSet dataSet3 = new LineDataSet(binnedPeaksList, "Binned Peaks");

                                          dataSet3.setDrawCircles(false);
                                          dataSet3.setDrawFilled(true);
                                          dataSet3.setDrawValues(false);
                                          dataSet3.setColor(R.color.colorAccent);

//                                      LineData chartData = new LineData();

                                          chartData.addDataSet(dataSet3);

                                          chartData.setValueTextColor(android.R.color.white);
                                          chart.setData(chartData);
                                          chart.invalidate();

                                      } else {
                                          if (audioProcessorThread.isAlive() && !audioProcessorThread.isInterrupted()) {
                                              // Interrupt thread and display dialog
                                              audioProcessorThread.interrupt();
                                              AlertDialog.Builder builder;
                                              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                  builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
                                              } else {
                                                  builder = new AlertDialog.Builder(getContext());
                                              }

                                              builder.setTitle("Authentic Coin Detected!")
                                                      .setMessage("All three resonance frequencies detected!")
                                                      .setPositiveButton("New Ping", new DialogInterface.OnClickListener() {
                                                          public void onClick(DialogInterface dialog, int which) {
                                                              // TODO: A solution needs to be found here. I cannot access dispatcher in this inner class
                                                              // because I was declaring it final before.
//                                                              audioProcessorThread = new Thread(this.dispatcher,"Audio Dispatcher");
//                                                              audioProcessorThread.start();
                                                          }
                                                      })
//
                                                      .show();
                                          } else {
                                              // Do nothing
                                          }

                                      }



                                      // Pause the updating of the spectrum
                                      // Update the UI to show that all 3 peaks were detected
                                      // Require a user action to go back to recording
                                  }

                              });



                return true;
            }
        });



        audioProcessorThread = new Thread(this.dispatcher,"Audio Dispatcher");
        audioProcessorThread.start();



















        mTextMessage = (TextView) findViewById(R.id.message);


    }

    public static Context getContext(){
        return mContext;
    }


    public void onClickSelectCoin(View view){
//        TextView coinDetails = (TextView) findViewById(R.id.coin_details);
        Spinner coinSelector = (Spinner) findViewById(R.id.coin_selector);
        String selectedCoin = String.valueOf(coinSelector.getSelectedItem());
//        coinDetails.setText(selectedCoin);
    }

    private Cursor getAllCoinNames(SQLiteDatabase db) {
        return db.query(
                CoinContract.CoinEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                CoinContract.CoinEntry.COLUMN_FULL_NAME
        );
    }


    public float convertHzToBin(long inputFrequency, int windowSize, int sampleRate) {
        return (float) inputFrequency / (sampleRate/2) * windowSize;
    }

    public void addSliceToSpectrogram(ArrayList<float[]> S, float[] slice) {
//        Log.i("size of S", String.valueOf(S.size()));
//        Log.i("slice", Arrays.toString(slice));
        S.add(slice);
        S.remove(0);
        this.S = S;

        for (int k=0; k<S.size(); k++) {
            Log.i(Integer.toString(k),Arrays.toString(S.get(k)));
        }

    }

    public ArrayList<List> addSliceToPeakogram(ArrayList<List> P, List<SpectralPeakProcessor.SpectralPeak> slice) {

        P.add(0,slice);
        P.remove(P.size()-1);
        this.P = P;
        Log.i("addSliceToPeakogram", P.toString());

        return P;
    }

    public float[] getBinnedPeaks(ArrayList<List> P) {
        // Create the float array of binned peaks and fill it with zeros
        float[] binnedPeaks = new float[windowSize / 2];
        Arrays.fill(binnedPeaks, 0);

        // Loop through each peakList within P
        for (int i = 0; i < P.size() - 1; i++) {
            List<SpectralPeakProcessor.SpectralPeak> currentPeakList = P.get(i);

            // Loop through each peak in the currentPeakList
            // Get the currentPeak bin number and add it to the float array of binned peaks
            for (int j = 0; j < currentPeakList.size() - 1; j++) {
                SpectralPeakProcessor.SpectralPeak currentPeak = currentPeakList.get(j);
                int currentPeakBin = currentPeak.getBin();
                Log.i(TAG, Integer.toString(currentPeakBin));
                binnedPeaks[currentPeakBin] = binnedPeaks[currentPeakBin] + 1;
            }
        }
        Log.i("binnedPeaks",Arrays.toString(binnedPeaks));
        return binnedPeaks;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null!= this.dispatcher) {
            this.dispatcher.stop();
        }
        if(null != cursor) {
            cursor.close();
        }
        if (null != db) {
            db.close();
        }
    }


    // Enables back button and kills the current activity when pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }





}