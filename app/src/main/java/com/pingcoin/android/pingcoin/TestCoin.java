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
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
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
import android.widget.ImageView;
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
import static android.graphics.Color.colorSpace;

// TODO: Close cursor.close()

public class TestCoin extends OverflowMenuActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private boolean debugNoiseFloor = true;
    private Thread audioProcessorThread;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private AudioDispatcher dispatcher;
    private int menuResource;


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




    private static final int PERMISSION_RECORD_AUDIO = 0;


    private SQLiteDatabase db;
    private Cursor cursor;


//    String selectedCoinId = getIntent().getStringExtra("id");



    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_coin);


        // Prevent the screen from dimming
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);




        String selectedCoinPopularName = "";
        float selectedCoinWeightInOz = 0;
        String selectedCoinMaterialClass = "";
        float selectedCoinC0D2a = 0;
        float selectedCoinC0D3a = 0;
        float selectedCoinC0D4a = 0;
        float selectedCoinError = 0;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //The key argument here must match that used in the other activity
            selectedCoinPopularName = extras.getString("PopularName");
            selectedCoinWeightInOz = extras.getFloat("WeightInOz");
            selectedCoinMaterialClass = extras.getString("MaterialClass");
            selectedCoinC0D2a = extras.getFloat("C0D2a");
            selectedCoinC0D3a = extras.getFloat("C0D3a");
            selectedCoinC0D4a = extras.getFloat("C0D4a");
            selectedCoinError = extras.getFloat("Error");
        }

        Toolbar toolbar = findViewById(R.id.test_coin_toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar actionBar = getSupportActionBar();

        // Enable the Up button
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setTitle(selectedCoinPopularName);
        actionBar.setSubtitle(selectedCoinMaterialClass+ " (" + Float.toString(selectedCoinWeightInOz) + " oz)");



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




        naturalFrequencyC0D2 = (long) selectedCoinC0D2a;
        naturalFrequencyC0D3 = (long) selectedCoinC0D3a;
        naturalFrequencyC0D4 = (long) selectedCoinC0D4a;
        naturalFrequencyError = selectedCoinError;
//        Log.i("ERROR HERE: ", Long.toString(selectedCoinError));






        // We need to close the database connection

        final LineChart chart = findViewById(R.id.chart);







        // Configure the spectrum chart
        Log.i(TAG, "Configuring the spectrum chart");
        SpectrumPlottingUtils.configureSpectrumChart(chart, windowSize, sampleRate);






        // Expected peaks plotting

        SpectrumPlottingUtils.plotNaturalFrequency(chart, "c0d2", naturalFrequencyC0D2, naturalFrequencyError, sampleRate, windowSize);

        SpectrumPlottingUtils.plotNaturalFrequency(chart, "c0d3", naturalFrequencyC0D3, naturalFrequencyError, sampleRate, windowSize);

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
        this.dispatcher.addAudioProcessor(new HighPass(500, sampleRate));
        this.dispatcher.addAudioProcessor(spectralPeakFollower);


        this.dispatcher.addAudioProcessor(new AudioProcessor() {

            public void processingFinished() {
            }

            public boolean process(AudioEvent audioEvent) {
                final float[] currentMagnitudes = spectralPeakFollower.getMagnitudes();
                final float[] currentLogMagnitudes = spectralPeakFollower.getLogMagnitudes();
                final float[] noiseFloor = SpectralPeakProcessor.calculateNoiseFloor(spectralPeakFollower.getLogMagnitudes(), medianFilterLength, noiseFloorFactor);


                double[] doubleArray = new double[currentMagnitudes.length];
                for (int i = 0; i < currentMagnitudes.length; i++) {
                    doubleArray[i] = (double) Math.abs(currentMagnitudes[i]);
                }

                double gm = MeanCalculator.geometricMean(doubleArray);
                double am = MeanCalculator.arithmeticMean(doubleArray);
                final double spectralFlatness = gm / am;

                final double spectralCentroid = MeanCalculator.spectralCentroid(doubleArray);


                Log.i("Spectral Flatness: ", Double.toString(spectralFlatness));


                // The first element of the noise floor is sometimes infinity. Here we filter that out.
                for (int i = 0; i < noiseFloor.length; i++) {
                    if (Float.isInfinite(noiseFloor[i])) {
                        noiseFloor[i] = 0;
                    } else {
                        // Do nothing
                    }
                }

                List<Integer> localMaxima = SpectralPeakProcessor.findLocalMaxima(spectralPeakFollower.getLogMagnitudes(), noiseFloor);
                List<SpectralPeakProcessor.SpectralPeak> list = SpectralPeakProcessor.findPeaks(spectralPeakFollower.getLogMagnitudes(), spectralPeakFollower.getFrequencyEstimates(), localMaxima, numberOfPeaks, 1);


                final ArrayList<List> peakogram = addSliceToPeakogram(P, list);

                final float[] binnedPeaks = getBinnedPeaks(P);
                final List<Entry> binnedPeaksList = SpectrumPlottingUtils.floatArrayToList(binnedPeaks);


                // Get list of bin indexes where the value exceeds binnedPeakThresh
                ArrayList<Integer> binnedPeaksExceedingThresh = new ArrayList<>();
                int binnedPeakThresh = 0;
                for (int k = 0; k < binnedPeaks.length; k++) {
                    if (binnedPeaks[k] > binnedPeakThresh) {
                        binnedPeaksExceedingThresh.add(k);
                    } else {
                        // do nothing
                    }
                }


                ArrayList<Float> binnedPeaksExceedingThreshHz = binIndexesToHz(binnedPeaksExceedingThresh);


                // Compare to expected frequencies

                // Reset expected frequency colors:
                SpectrumPlottingUtils.detectedNaturalFrequency(chart, "c0d2", false);
                SpectrumPlottingUtils.detectedNaturalFrequency(chart, "c0d3", false);
                SpectrumPlottingUtils.detectedNaturalFrequency(chart, "c0d4", false);

                final ImageView c0d2 = findViewById(R.id.imageView_c0d2);
                final ImageView c0d3 = findViewById(R.id.imageView_c0d3);
                final ImageView c0d4 = findViewById(R.id.imageView_c0d4);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
//                        ImageView c0d2 = findViewById(R.id.imageView_c0d2);
//                        ImageView c0d3 = findViewById(R.id.imageView_c0d3);
//                        ImageView c0d4 = findViewById(R.id.imageView_c0d4);
                        c0d2.setImageDrawable(getResources().getDrawable(R.drawable.ic_c0d2));
                        c0d3.setImageDrawable(getResources().getDrawable(R.drawable.ic_c0d3));
                        c0d4.setImageDrawable(getResources().getDrawable(R.drawable.ic_c0d4));
                    }
                });


                // Check if expected frequencies are detected
                boolean C0D2Detected = false;
                boolean C0D3Detected = false;
                boolean C0D4Detected = false;


                float tempC0D2 = TestCoin.naturalFrequencyC0D2;
                float tempC0D3 = TestCoin.naturalFrequencyC0D3;
                float tempC0D4 = TestCoin.naturalFrequencyC0D4;
                float tempErrorMargin = (float) TestCoin.naturalFrequencyError;


                // Loop through each detected peak and compare with expected peaks
                // If it falls within the error margin, mark the detected boolean as true
                for (float realPeakvalue : binnedPeaksExceedingThreshHz) {
//                    float peak = ((float) realPeakvalue / windowSize) * sampleRate;
                    float peak = realPeakvalue;
                    if (peak > tempC0D2 * (1 - tempErrorMargin) && peak < tempC0D2 * (1 + tempErrorMargin)) {
                        C0D2Detected = true;
                    } else if (peak > tempC0D3 * (1 - tempErrorMargin) && peak < tempC0D3 * (1 + tempErrorMargin)) {
                        C0D3Detected = true;
                    } else if (peak > tempC0D4 * (1 - tempErrorMargin) && peak < tempC0D4 * (1 + tempErrorMargin)) {
                        C0D4Detected = true;
                    }

                }

                final boolean finalC0D2Detected1 = C0D2Detected;
                final boolean finalC0D3Detected1 = C0D3Detected;
                final boolean finalC0D4Detected1 = C0D4Detected;
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (finalC0D2Detected1 == true) {
                            c0d2.setImageDrawable(getResources().getDrawable(R.drawable.ic_c0d2_check));
                            SpectrumPlottingUtils.detectedNaturalFrequency(chart, "c0d2", finalC0D2Detected1);
                        }
                        if (finalC0D3Detected1 == true) {
                            c0d3.setImageDrawable(getResources().getDrawable(R.drawable.ic_c0d3_check));
                            SpectrumPlottingUtils.detectedNaturalFrequency(chart, "c0d3", finalC0D3Detected1);

                        }
                        if (finalC0D4Detected1 == true) {
                            c0d4.setImageDrawable(getResources().getDrawable(R.drawable.ic_c0d4_check));
                            SpectrumPlottingUtils.detectedNaturalFrequency(chart, "c0d4", finalC0D4Detected1);
                        }
                        chart.invalidate();
                    }
                });


                final List<Entry> logMagnitudesList = SpectrumPlottingUtils.floatArrayToList(currentLogMagnitudes);
                final List<Entry> magnitudesList = SpectrumPlottingUtils.floatArrayToList(currentMagnitudes);
                final List<Entry> noiseFloorList = SpectrumPlottingUtils.floatArrayToList(noiseFloor);


                final boolean finalC0D2Detected = C0D2Detected;
                final boolean finalC0D3Detected = C0D3Detected;
                final boolean finalC0D4Detected = C0D4Detected;
                final boolean allPeaksDetected = (C0D2Detected && C0D3Detected && C0D4Detected);

                final boolean plotSpectrum = true;
                final boolean plotNoiseFloor = false;

                if (!audioProcessorThread.isInterrupted()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {


                            // Plot detected fr

                            LineData chartData = new LineData();


                            if (plotNoiseFloor) {

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

                            } else if (plotSpectrum) {
                                LineDataSet dataSet1 = new LineDataSet(magnitudesList, "Current Magnitudes"); // add entries to dataset

                                dataSet1.setDrawCircles(false);
                                dataSet1.setDrawFilled(true);
                                dataSet1.setDrawValues(false);
                                dataSet1.setColor(Color.BLACK);


                                chartData.addDataSet(dataSet1);
                                chart.setData(chartData);

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

//                                          chartData.setValueTextColor(android.R.color.white);
                                chart.setData(chartData);
                                chart.invalidate();


                            }

                            // In the case where ALL 3 peaks are detected:
                            else {
                                if (audioProcessorThread.isAlive() && !audioProcessorThread.isInterrupted() && spectralCentroid > 600 && spectralFlatness < 0.5) {
                                    // Interrupt thread and display dialog
                                    audioProcessorThread.interrupt();

                                    AlertDialog.Builder builder;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
                                    } else {
                                        builder = new AlertDialog.Builder(getContext());
                                    }

                                    builder.setTitle("Authentic Coin Detected!")
                                            .setMessage("Spectral Flatness: " + spectralFlatness + ", Spectral Centroid: " + spectralCentroid)
                                            .setPositiveButton("New Ping", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    audioProcessorThread = new Thread(TestCoin.this.dispatcher, "Audio Dispatcher");
                                                    audioProcessorThread.start();
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

                }
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
                binnedPeaks[currentPeakBin] = binnedPeaks[currentPeakBin] + 1;
            }
        }
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

    public ArrayList<Float> binIndexesToHz(ArrayList<Integer> binnedPeaksExceedingThresh) {
        ArrayList<Float> binnedPeaksExceedingThreshHz = new ArrayList<>();
        for (int binnedPeak : binnedPeaksExceedingThresh) {
            float binnedPeakHz = ((float) binnedPeak / windowSize) * sampleRate;
            binnedPeaksExceedingThreshHz.add(binnedPeakHz);
        }
        return binnedPeaksExceedingThreshHz;
    }





}