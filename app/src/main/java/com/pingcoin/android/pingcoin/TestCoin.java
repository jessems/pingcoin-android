package com.pingcoin.android.pingcoin;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyLog;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.graphics.Color.argb;
import static com.pingcoin.android.pingcoin.VerdictCalculationUtils.*;

// TODO: Close cursor.close()

public class TestCoin extends OverflowMenuActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static boolean finalVerdictIsBeingDisplayed;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private boolean debugNoiseFloor = true;
    private Thread audioProcessorThread;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    AudioDispatcher dispatcher;
    private int menuResource;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

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


    private static final int PERMISSION_RECORD_AUDIO = 0;


    private SQLiteDatabase db;
    private Cursor cursor;

    public boolean secondaryBannerIsSlideInBanner;
    public long verdictExpirationTime;

    @Override
    protected void onResume() {
        super.onResume();
        setFinalVerdictIsBeingDisplayed(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_coin);

        // Prevent the screen from dimming
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String selectedCoinPopularName = "";
        String selectedCoinId = "";
        float selectedCoinWeightInOz = 0;
        String selectedCoinMaterialClass = "";
        float selectedCoinC0D2a = 0;
        float selectedCoinC0D3a = 0;
        float selectedCoinC0D4a = 0;
        float selectedCoinError = 0;
        finalVerdictIsBeingDisplayed = false;

        // Set up the intent for the coinTesting activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // The key argument here must match that used in the other activity
            selectedCoinPopularName = extras.getString("PopularName");
            selectedCoinId = extras.getString("id");
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

        // Set up ActionBar
        actionBar.setTitle(selectedCoinPopularName);
        actionBar.setSubtitle(selectedCoinMaterialClass + " (" + Float.toString(selectedCoinWeightInOz) + " oz)");


        // Set up Start Again button and hide it by default
        final Button startAgainButton;
        startAgainButton = (Button) findViewById(R.id.start_again_button);
        startAgainButton.setVisibility(View.GONE);
        startAgainButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                startAgainButton.setVisibility(View.GONE);
                resetVerdictBanner();
                setFinalVerdictIsBeingDisplayed(false);
            }
        });


        mContext = this;
        VolleyLog.DEBUG = true;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 1);

        }


        // Get coins from SQLite db
        SQLiteOpenHelper coinDatabaseHelper = new PingcoinDatabaseHelper(this);
        SQLiteDatabase db = coinDatabaseHelper.getReadableDatabase();


        // Get coins

        final Cursor cursor = getAllCoinNames(db);

        // Convert coins to arraylist
        List itemIds = new ArrayList<>();
        while (cursor.moveToNext()) {
            String itemId = cursor.getString(
                    cursor.getColumnIndexOrThrow(CoinContract.CoinEntry.COLUMN_FULL_NAME));
            itemIds.add(itemId);
        }


        naturalFrequencyC0D2 = (long) selectedCoinC0D2a;
        naturalFrequencyC0D3 = (long) selectedCoinC0D3a;
        naturalFrequencyC0D4 = (long) selectedCoinC0D4a;
        naturalFrequencyError = selectedCoinError;


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
        for (int i = 0; i < nFrames; i++) {
            S.add(new float[windowSize]);
        }

        final PingProcessor pingProcessor = new PingProcessor(windowSize, windowSize * 75 / 100, sampleRate);
        final int medianFilterLength = 25;
        // If windowSize is 4096 -> noiseFloorFactor should be 1.15f
        // If windowSize is 2048 -> noiseFloorFactor should be 1.10f
        final float noiseFloorFactor = 1.15f;
        final int numberOfPeaks = 10;


        for (int i = 0; i < nFrames; i++) {
            P.add(new ArrayList());
        }


        // The factory is a design method that returns an object for a method call.
        // In this case the fromDefaultMicrophone() method returns an AudioDispatcher object.

        this.dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, windowSize, windowSize * 75 / 100);
        this.dispatcher.addAudioProcessor(new HighPass(1000, sampleRate));
        // TODO: Simplify the pingProcessor (We don't need all it's functions.)
        this.dispatcher.addAudioProcessor(pingProcessor);

        // Set Thresholds
        final Map<String, Double> featureThresholds = new HashMap<>();
        featureThresholds.put("spectralCentroidThreshold", 500d); // Must be larger than this
        featureThresholds.put("spectralFlatnessThreshold", 0.5d); // Must be larger than this
        featureThresholds.put("spectralMedianThreshold", 0.04d); // Must be smaller than this

        // There are 2 Banner views. The one out of screen should be labeled the SlideIn banner. This toggle does that.
        // If false, it implies that the PRIMARY banner becomes the slide-in banner
        secondaryBannerIsSlideInBanner = true;


        // Add a last anonymous AudioProcessor object where most of the magic happens.
        final String finalSelectedCoinId = selectedCoinId;
        final String finalSelectedCoinPopularName = selectedCoinPopularName;
        this.dispatcher.addAudioProcessor(new AudioProcessor() {

            public void processingFinished() {
            }

            // The process method for the main (anonymous) AudioProcessor object.
            public boolean process(AudioEvent audioEvent) {

                // Set up the variables
                boolean pingIsClean = false;

                final float[] currentMagnitudes = pingProcessor.getMagnitudes();
                final double[] currentMagnitudesAsDoubles = convertFloatsToDoubles(currentMagnitudes);
                final float[] currentLogMagnitudes = pingProcessor.getLogMagnitudes();
                final float[] currentFrequencyEstimates = pingProcessor.getFrequencyEstimates();
                final float[] noiseFloor;


                // Create an empty list of SpectralPeak objects to fill in the peaks later
                List<SpectralPeak> peakList = new ArrayList<>();


                // FEATURE DETECTION
                // Collect the spectral features in a map
                final Map<String, Double> spectralFeatures = SpectrumPlottingUtils.getSpectralFeatures(currentMagnitudes);


                // PEAK DETECTION
                // Set the peak detection algorithm
                LinkedList<Integer> detectedPeaksBins = new LinkedList<>();
                LinkedList<Float> detectedPeaksHz = new LinkedList<>();
                String peakDetectionAlgorithm = "GENERAL_PEAK_PICKING_METHOD";

                // Initialize the peak detection algorithm based on the one that was selected
                switch (peakDetectionAlgorithm) {
                    case "GENERAL_PEAK_PICKING_METHOD":
                        detectedPeaksBins = Peaks.findPeaks(currentMagnitudesAsDoubles, 5, 0.10, 0.05, true);

                        if (detectedPeaksBins.size() < 100) {
                            // Create a parallel LinkedList of peaks in Hz for use later.
                            detectedPeaksHz = convertBinsToHz(detectedPeaksBins, currentFrequencyEstimates);
                            convertToListOfSpectralPeaks(detectedPeaksBins, peakList, currentMagnitudes);
                        } else {
                            // do this
                        }

                }


                // Compare to expected frequencies

                // Reset expected frequency colors:


                final ImageView c0d2 = findViewById(R.id.imageView_c0d2);
                final ImageView c0d3 = findViewById(R.id.imageView_c0d3);
                final ImageView c0d4 = findViewById(R.id.imageView_c0d4);


                Map<String, Float> expectedResonanceFrequencies = new HashMap();
                expectedResonanceFrequencies.put("C0D2", (float) TestCoin.naturalFrequencyC0D2);
                expectedResonanceFrequencies.put("C0D3", (float) TestCoin.naturalFrequencyC0D3);
                expectedResonanceFrequencies.put("C0D4", (float) TestCoin.naturalFrequencyC0D4);
                expectedResonanceFrequencies.put("tolerance", (float) TestCoin.naturalFrequencyError);


                // RESONANCE FREQUENCY RECOGNITION
                // Returns a map with recognized frequencies
                final Map<String, Boolean> recognizedResonanceFrequencies =
                        getRecognizedResonanceFrequencies(detectedPeaksHz, expectedResonanceFrequencies);

                // Returns a verdict based the frequencies that were recognized
                final Verdict verdict =
                        getVerdict(recognizedResonanceFrequencies, spectralFeatures, featureThresholds);

                final List<Entry> magnitudesList = SpectrumPlottingUtils.floatArrayToList(currentMagnitudes);

                // TODO: We should only change the color on the UI thread
//                updateResonanceFrequencyToleranceBars(recognizedResonanceFrequencies, chart);

                if(!finalVerdictIsBeingDisplayed) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SpectrumPlottingUtils.resonanceFrequencyToleranceBar(chart, "c0d2", false);
                            SpectrumPlottingUtils.resonanceFrequencyToleranceBar(chart, "c0d3", false);
                            SpectrumPlottingUtils.resonanceFrequencyToleranceBar(chart, "c0d4", false);
//                            updateResonanceFrequencyToleranceBars(recognizedResonanceFrequencies, chart);
                            updateResonanceFrequencyIcons(recognizedResonanceFrequencies);
                        }
                    });
                }


                if (!audioProcessorThread.isInterrupted() && !finalVerdictIsBeingDisplayed) {

                    final LineData chartData = new LineData();

                    Map<String, ImageView> resonanceFrequencyIconImageViews = new HashMap<>();
                    resonanceFrequencyIconImageViews.put("c0d2", c0d2);
                    resonanceFrequencyIconImageViews.put("c0d3", c0d3);
                    resonanceFrequencyIconImageViews.put("c0d4", c0d4);


                    // Continue to update the graph after every block as long as NOT all 3 peaks are detected
                    switch (verdict) {
                        case ZERO_RESONANCE_FREQUENCIES_RECOGNIZED_NOT_CLEAN:
                            if (verdictHasExpired()
                                    && !getFinalVerdictIsBeingDisplayed()
                                    ) {
                                updateChart(chart, chartData, magnitudesList);
                                updateDetectedPeaks(detectedPeaksBins, chart);
                                updateResonanceFrequencyToleranceBars(recognizedResonanceFrequencies, chart);
                                break;
                            }
                        case ZERO_RESONANCE_FREQUENCIES_RECOGNIZED_CLEAN:
                            if (verdictHasExpired()
                                    && !getFinalVerdictIsBeingDisplayed()
                                    ) {
                                updateChart(chart, chartData, magnitudesList);
                                updateDetectedPeaks(detectedPeaksBins, chart);
                                updateResonanceFrequencyToleranceBars(recognizedResonanceFrequencies, chart);

                                // If the ping is clean and some peaks are detected, it might be a coin (just not THIS coin)
                                // If between 2 and 10 peaks are detected, freeze the graph as we do with a proper verdict.
                                if (detectedPeaksBins.size() > 2 && detectedPeaksBins.size() < 10) {
                                    updateVerdictBanner(verdict, spectralFeatures);
                                    setFinalVerdictIsBeingDisplayed(true);
                                    setStartAgainButtonVisibility(startAgainButton, View.VISIBLE);
                                } else {
//                                    showToast(getString(R.string.zero_resonance_frequences_recognized_clean_toast));
                                }
                                break;

                            }
                        case ONE_RESONANCE_FREQUENCY_RECOGNIZED_NOT_CLEAN:
                            if (verdictHasExpired()
                                    && !getFinalVerdictIsBeingDisplayed()
                                    ) {

                                updateChart(chart, chartData, magnitudesList);
                                updateDetectedPeaks(detectedPeaksBins, chart);
                                updateResonanceFrequencyToleranceBars(recognizedResonanceFrequencies, chart);
                                break;
                            }
                        case ONE_RESONANCE_FREQUENCY_RECOGNIZED_CLEAN:
                            if (verdictHasExpired()
                                    && !getFinalVerdictIsBeingDisplayed()
                                    ) {
                                updateChart(chart, chartData, magnitudesList);
//                                updateVerdictBanner(verdict, spectralFeatures);

//                                showToast(getString(R.string.zero_resonance_frequences_recognized_clean_toast));

                                updateDetectedPeaks(detectedPeaksBins, chart);
                                updateResonanceFrequencyToleranceBars(recognizedResonanceFrequencies, chart);

//                                setFinalVerdictIsBeingDisplayed(true);
//                                setStartAgainButtonVisibility(startAgainButton, View.VISIBLE);
                                break;
                            }
                        case TWO_RESONANCE_FREQUENCIES_RECOGNIZED_NOT_CLEAN:
                            if (verdictHasExpired()
                                    && !getFinalVerdictIsBeingDisplayed()
                                    ) {
                                updateChart(chart, chartData, magnitudesList);
//                                updateVerdictBanner(verdict, spectralFeatures);

//                                showToast(getString(R.string.zero_resonance_frequences_recognized_clean_toast));

                                updateDetectedPeaks(detectedPeaksBins, chart);
                                updateResonanceFrequencyToleranceBars(recognizedResonanceFrequencies, chart);

//                                setFinalVerdictIsBeingDisplayed(true);
//                                setStartAgainButtonVisibility(startAgainButton, View.VISIBLE);
                                break;
                            }
                        case TWO_RESONANCE_FREQUENCIES_RECOGNIZED_CLEAN:
                            if (verdictHasExpired()
                                    && !getFinalVerdictIsBeingDisplayed()
                                    ) {
                                updateChart(chart, chartData, magnitudesList);
                                updateVerdictBanner(verdict, spectralFeatures);
                                updateDetectedPeaks(detectedPeaksBins, chart);
                                updateResonanceFrequencyToleranceBars(recognizedResonanceFrequencies, chart);

                                setFinalVerdictIsBeingDisplayed(true);
                                setStartAgainButtonVisibility(startAgainButton, View.VISIBLE);
                                break;
                            }
                        case THREE_RESONANCE_FREQUENCIES_RECOGNIZED_NOT_CLEAN:
                            if (verdictHasExpired()
                                    && !getFinalVerdictIsBeingDisplayed()
                                    ) {
                                updateChart(chart, chartData, magnitudesList);
//                                updateVerdictBanner(verdict, spectralFeatures);

//                                showToast(getString(R.string.zero_resonance_frequences_recognized_clean_toast));

                                updateDetectedPeaks(detectedPeaksBins, chart);
                                updateResonanceFrequencyIcons(recognizedResonanceFrequencies);
                                updateResonanceFrequencyToleranceBars(recognizedResonanceFrequencies, chart);

//                                setFinalVerdictIsBeingDisplayed(true);
//                                setStartAgainButtonVisibility(startAgainButton, View.VISIBLE);

                                break;
                            }
                        case THREE_RESONANCE_FREQUENCIES_RECOGNIZED_CLEAN:
                            if (verdictHasExpired()
                                    && !getFinalVerdictIsBeingDisplayed()
                                    ) {
                                updateChart(chart, chartData, magnitudesList);
                                updateVerdictBanner(verdict, spectralFeatures);
                                updateDetectedPeaks(detectedPeaksBins, chart);
                                updateResonanceFrequencyIcons(recognizedResonanceFrequencies);
                                updateResonanceFrequencyToleranceBars(recognizedResonanceFrequencies, chart);

                                setFinalVerdictIsBeingDisplayed(true);
                                setStartAgainButtonVisibility(startAgainButton, View.VISIBLE);

                                displayAuthenticCoinDialog(finalSelectedCoinId, finalSelectedCoinPopularName, spectralFeatures);
//                                SpectrumPlottingUtils.drawDetectedPeaks(detectedPeaksBins, chart);

                            }
                            break;
                    }
                } // end of switch statement

                // The process() method needs to return a boolean value. This happens here.
                    return true;
            }
        });


        if (audioProcessorThread == null)

        {
            audioProcessorThread = new Thread(this.dispatcher, "Audio Dispatcher");
            audioProcessorThread.start();
        }


        mTextMessage = (TextView)

                findViewById(R.id.message);


    }

    private boolean getFinalVerdictIsBeingDisplayed() {
        return this.finalVerdictIsBeingDisplayed;
    }

    private void setFinalVerdictIsBeingDisplayed(boolean b) {
        this.finalVerdictIsBeingDisplayed = b;
    }


    private void setStartAgainButtonVisibility(final Button startAgainButton, final int visibility) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                startAgainButton.setVisibility(visibility);

            }
        });
    }

    private void resetResonanceFrequencyIcons(final Map<String, ImageView> resonanceFrequencyIconImageViews) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Reset the icons
                resonanceFrequencyIconImageViews.get("c0d2").setImageDrawable(getResources().getDrawable(R.drawable.ic_c0d2));
                resonanceFrequencyIconImageViews.get("c0d3").setImageDrawable(getResources().getDrawable(R.drawable.ic_c0d3));
                resonanceFrequencyIconImageViews.get("c0d4").setImageDrawable(getResources().getDrawable(R.drawable.ic_c0d4));

            }
        });
    }

    private LinkedList<Float> convertBinsToHz(LinkedList<Integer> peakIndexes, float[] currentFrequencyEstimates) {
        LinkedList<Float> peakFrequencies = new LinkedList<>();
        for (int element : peakIndexes) {
//            peakFrequencies.add(binIndexToHz(element));
            peakFrequencies.add(currentFrequencyEstimates[element]);
        }
        return peakFrequencies;
    }

    public static Context getContext() {
        return mContext;
    }


    public void displayAuthenticCoinDialog(final String selectedCoinId, final String selectedCoinPopularName, final Map spectralFeatures) {

        final AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.setTitle("Authentic " + selectedCoinPopularName + " Detected!")
                        .setMessage("The ping you produced was consistent with an authentic " + selectedCoinPopularName + ".")
//                        .setMessage("Spectral Flatness: " + spectralFeatures.get("spectralFlatness") +
//                                ", Spectral Centroid: " + spectralFeatures.get("spectralCentroid"))
                        .setIcon(getResources().getIdentifier("ic_" + selectedCoinId + "_listview", "drawable", getContext().getPackageName()))
                        .setPositiveButton("New Ping", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });

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
        return (float) inputFrequency / (sampleRate / 2) * windowSize;
    }

    public void addSliceToSpectrogram(ArrayList<float[]> S, float[] slice) {
//        Log.i("size of S", String.valueOf(S.size()));
//        Log.i("slice", Arrays.toString(slice));
        S.add(slice);
        S.remove(0);
        this.S = S;

        for (int k = 0; k < S.size(); k++) {
            Log.i(Integer.toString(k), Arrays.toString(S.get(k)));
        }

    }

    public ArrayList<List> addSliceToPeakogram(ArrayList<List> P, List<SpectralPeak> slice) {

        P.add(0, slice);
        P.remove(P.size() - 1);
        this.P = P;

        return P;
    }


    public float[] getBinnedPeaks(ArrayList<List> P) {
        // Create the float array of binned peaks and fill it with zeros
        float[] binnedPeaks = new float[windowSize / 2];
        Arrays.fill(binnedPeaks, 0);

        // Loop through each peakList within P
        for (int i = 0; i < P.size() - 1; i++) {
            List<SpectralPeak> currentPeakList = P.get(i);

            // Loop through each peak in the currentPeakList
            // Get the currentPeak bin number and add it to the float array of binned peaks
            for (int j = 0; j < currentPeakList.size() - 1; j++) {
                SpectralPeak currentPeak = currentPeakList.get(j);
                int currentPeakBin = currentPeak.getBin();
                binnedPeaks[currentPeakBin] = binnedPeaks[currentPeakBin] + 1;
            }
        }
        return binnedPeaks;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != this.dispatcher) {
            this.dispatcher.stop();
        }
        if (null != cursor) {
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

    public Float binIndexToHz(Integer binnedPeakIndex) {

        float binnedPeakHz = ((float) binnedPeakIndex / windowSize) * sampleRate;

        return binnedPeakHz;
    }

    public void slideVerdictBannerUp(View view) {
//        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,  // fromYDelta
                -view.getHeight());                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    public void slideVerdictBannerDown(View view) {
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                -view.getHeight(),                 // fromYDelta
                0); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    public void resetVerdictBanner() {
        final View slideInVerdictBanner;
        final ImageView slideInVerdictIcon;
        final TextView slideInVerdictText;
        final TextView slideInVerdictSubtitle;
        final TextView slideInVerdictInstructionText;
        final View slideOutVerdictBanner;

        if (secondaryBannerIsSlideInBanner) {
            slideInVerdictBanner = findViewById(R.id.verdict_secondary_banner);
            slideInVerdictIcon = findViewById(R.id.verdict_secondary_icon);
            slideInVerdictText = findViewById(R.id.verdict_secondary_text);
            slideInVerdictSubtitle = findViewById(R.id.verdict_secondary_subtitle);
            slideInVerdictInstructionText = findViewById(R.id.verdict_secondary_instruction_text);
            slideOutVerdictBanner = findViewById(R.id.verdict_primary_banner);

        } else {
            slideInVerdictBanner = findViewById(R.id.verdict_primary_banner);
            slideInVerdictIcon = findViewById(R.id.verdict_primary_icon);
            slideInVerdictText = findViewById(R.id.verdict_primary_text);
            slideInVerdictSubtitle = findViewById(R.id.verdict_primary_subtitle);
            slideInVerdictInstructionText = findViewById(R.id.verdict_primary_instruction_text);
            slideOutVerdictBanner = findViewById(R.id.verdict_secondary_banner);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                slideInVerdictIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_listening));
                slideInVerdictText.setText("Listening...");
                slideInVerdictSubtitle.setText("I didn't hear a coin yet");
                slideInVerdictInstructionText.setText("Flick a coin to get started");

                slideVerdictBannerDown(slideInVerdictBanner);

                slideVerdictBannerUp(slideOutVerdictBanner);

            }
        });


        // Make whatever banner was not the slide-in banner the new slide-in banner
        secondaryBannerIsSlideInBanner = !secondaryBannerIsSlideInBanner;

    }

    public void updateVerdictBanner(Verdict verdict, final Map spectralFeatures) {

        final View slideInVerdictBanner;
        final ImageView slideInVerdictIcon;
        final TextView slideInVerdictText;
        final TextView slideInVerdictSubtitle;
        final TextView slideInVerdictInstructionText;
        final View slideOutVerdictBanner;

        if (secondaryBannerIsSlideInBanner) {
            slideInVerdictBanner = findViewById(R.id.verdict_secondary_banner);
            slideInVerdictIcon = findViewById(R.id.verdict_secondary_icon);
            slideInVerdictText = findViewById(R.id.verdict_secondary_text);
            slideInVerdictSubtitle = findViewById(R.id.verdict_secondary_subtitle);
            slideInVerdictInstructionText = findViewById(R.id.verdict_secondary_instruction_text);
            slideOutVerdictBanner = findViewById(R.id.verdict_primary_banner);

        } else {
            slideInVerdictBanner = findViewById(R.id.verdict_primary_banner);
            slideInVerdictIcon = findViewById(R.id.verdict_primary_icon);
            slideInVerdictText = findViewById(R.id.verdict_primary_text);
            slideInVerdictSubtitle = findViewById(R.id.verdict_primary_subtitle);
            slideInVerdictInstructionText = findViewById(R.id.verdict_primary_instruction_text);
            slideOutVerdictBanner = findViewById(R.id.verdict_secondary_banner);
        }

        switch (verdict) {
            default:
                // Do nothing
                break;

            case ZERO_RESONANCE_FREQUENCIES_RECOGNIZED_NOT_CLEAN:
                // If no peaks are detected, this case should actually not get triggered at all...
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        slideInVerdictIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_unclear));
                        slideInVerdictText.setText("Unclear");
                        slideInVerdictSubtitle.setText("No resonance frequencies detected");
                        Log.d("SPEC", "Flat " + spectralFeatures.get("spectralFlatness").toString() +
                                ", Cen: " + spectralFeatures.get("spectralCentroid").toString() + ", Median: " + spectralFeatures.get("spectralMedian").toString());

                    }
                });
                break;

            case ZERO_RESONANCE_FREQUENCIES_RECOGNIZED_CLEAN:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        slideInVerdictIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_unclear));
                        slideInVerdictText.setText("Not recognized");
                        slideInVerdictSubtitle.setText("No resonance frequencies detected");
                        slideInVerdictInstructionText.setText("");
                        Log.d("SPEC", "Flat " + spectralFeatures.get("spectralFlatness").toString() +
                                ", Cen: " + spectralFeatures.get("spectralCentroid").toString() + ", Median: " + spectralFeatures.get("spectralMedian").toString());

                    }
                });
                break;

            case ONE_RESONANCE_FREQUENCY_RECOGNIZED_NOT_CLEAN:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        slideInVerdictIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_unclear));
                        slideInVerdictText.setText("Unclear");
                        slideInVerdictSubtitle.setText("Detected 1 out of 3 frequencies");
                        Log.d("SPEC", "Flat " + spectralFeatures.get("spectralFlatness").toString() +
                                ", Cen: " + spectralFeatures.get("spectralCentroid").toString() + ", Median: " + spectralFeatures.get("spectralMedian").toString());

                    }
                });
                break;

            case ONE_RESONANCE_FREQUENCY_RECOGNIZED_CLEAN:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        slideInVerdictIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_unclear));
                        slideInVerdictText.setText("Ping again");
                        slideInVerdictSubtitle.setText("Detected 1 out of 3 frequencies");
                        Log.d("SPEC", "Flat " + spectralFeatures.get("spectralFlatness").toString() +
                                ", Cen: " + spectralFeatures.get("spectralCentroid").toString() + ", Median: " + spectralFeatures.get("spectralMedian").toString());

                    }
                });
                break;

            case TWO_RESONANCE_FREQUENCIES_RECOGNIZED_NOT_CLEAN:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        slideInVerdictIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_unclear));
                        slideInVerdictText.setText("Ping not clean!");
                        slideInVerdictSubtitle.setText("Detected 2 out of 3 frequencies");
                        slideInVerdictInstructionText.setText("Press \"START AGAIN\" to try again.");

                        Log.d("SPEC", "Flat " + spectralFeatures.get("spectralFlatness").toString() +
                                ", Cen: " + spectralFeatures.get("spectralCentroid").toString() + ", Median: " + spectralFeatures.get("spectralMedian").toString());

                    }
                });
                break;

            case TWO_RESONANCE_FREQUENCIES_RECOGNIZED_CLEAN:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        slideInVerdictIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_unclear));
                        slideInVerdictText.setText("Close!");
                        slideInVerdictSubtitle.setText("Detected 2 out of 3 frequencies");
                        slideInVerdictInstructionText.setText("Press \"START AGAIN\" to try again.");
                        Log.d("SPEC", "Flat " + spectralFeatures.get("spectralFlatness").toString() +
                                ", Cen: " + spectralFeatures.get("spectralCentroid").toString() + ", Median: " + spectralFeatures.get("spectralMedian").toString());


                    }
                });
                break;

            case THREE_RESONANCE_FREQUENCIES_RECOGNIZED_NOT_CLEAN:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        slideInVerdictIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_unclear));
                        slideInVerdictText.setText("Ping not clean!");
                        slideInVerdictSubtitle.setText("Detected 3 out of 3 frequencies.");
                        slideInVerdictInstructionText.setText("Press \"START AGAIN\" to try again.");
                        Log.d("SPEC", "Flat " + spectralFeatures.get("spectralFlatness").toString() +
                                ", Cen: " + spectralFeatures.get("spectralCentroid").toString() + ", Median: " + spectralFeatures.get("spectralMedian").toString());

                    }
                });
                break;

            case THREE_RESONANCE_FREQUENCIES_RECOGNIZED_CLEAN:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        slideInVerdictIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_authentic));
                        slideInVerdictText.setText("Authentic!");
                        slideInVerdictSubtitle.setText("Detected 3 out of 3 frequencies");
                        slideInVerdictInstructionText.setText("Press \"START AGAIN\" to try again.");
                        Log.d("SPEC", "Flat " + spectralFeatures.get("spectralFlatness").toString() +
                                ", Cen: " + spectralFeatures.get("spectralCentroid").toString() + ", Median: " + spectralFeatures.get("spectralMedian").toString());

                    }
                });
                break;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                slideVerdictBannerDown(slideInVerdictBanner);
                slideVerdictBannerUp(slideOutVerdictBanner);
            }
        });


        // Make whatever banner was not the slide-in banner the new slide-in banner
        secondaryBannerIsSlideInBanner = !secondaryBannerIsSlideInBanner;
    }

    public void updateChart(final LineChart chart, final LineData chartData, final List<Entry> magnitudesList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SpectrumPlottingUtils.plotSpectrum(chart, chartData, magnitudesList);
            }
        });
    }

    public void invalidateChartOnUiThread(final LineChart chart) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chart.invalidate();
            }
        });
    }


    public boolean verdictHasExpired() {
        long currentTime = System.currentTimeMillis();
        boolean isVerdictOlderThanExpiration = currentTime > this.verdictExpirationTime;
        if (isVerdictOlderThanExpiration) {
            return true;
        } else {
            return false;
        }
    }

    public void resetVerdictExpirationTime() {
        long currentTime = System.currentTimeMillis();
        this.verdictExpirationTime = currentTime + 1 * 1500;
        Log.i("resetVerdictExpTime", "Verdict expiration time RESET!!");
    }

    public void updateResonanceFrequencyIcons(Map<String, Boolean> detectedPeaks) {
        final ImageView c0d2 = findViewById(R.id.imageView_c0d2);
        final ImageView c0d3 = findViewById(R.id.imageView_c0d3);
        final ImageView c0d4 = findViewById(R.id.imageView_c0d4);

        if (detectedPeaks.get("C0D2")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    c0d2.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_c0d2_check, null));
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    c0d2.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_c0d2, null));
                }
            });
        }
        if (detectedPeaks.get("C0D3")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    c0d3.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_c0d3_check, null));
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    c0d3.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_c0d3, null));
                }
            });
        }
        if (detectedPeaks.get("C0D4")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    c0d4.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_c0d4_check, null));
                }
            });

        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    c0d4.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_c0d4, null));
                }
            });

        }
    }

    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TestCoin.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateResonanceFrequencyToleranceBars(Map<String, Boolean> recognizedResonanceFrequencies, final LineChart chart) {
        if (recognizedResonanceFrequencies.get("C0D2")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SpectrumPlottingUtils.resonanceFrequencyToleranceBar(chart, "c0d2", true);
                }
            });
        }
        if (recognizedResonanceFrequencies.get("C0D3")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SpectrumPlottingUtils.resonanceFrequencyToleranceBar(chart, "c0d3", true);
                }
            });

        }
        if (recognizedResonanceFrequencies.get("C0D4")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SpectrumPlottingUtils.resonanceFrequencyToleranceBar(chart, "c0d4", true);
                }
            });
        }
    }

    // Draws a line for each peak detected by the peak detection algorithm
    public void updateDetectedPeaks(final LinkedList<Integer> detectedPeaksBins, final LineChart chart) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SpectrumPlottingUtils.drawDetectedPeaks(detectedPeaksBins, chart);
            }
        });
    }

    public static double[] convertFloatsToDoubles(float[] input) {
        if (input == null) {
            return null; // Or throw an exception - your choice
        }
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = input[i];
        }
        return output;
    }

    public void updateViews(final LineChart chart, final LineData chartData, final LinkedList<Integer> peakIndexes, final List<Entry> magnitudesList, final Verdict verdict, final Map spectralFeatures, final Map detectedPeaks) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SpectrumPlottingUtils.drawDetectedPeaks(peakIndexes, chart);
                updateChart(chart, chartData, magnitudesList);
                updateVerdictBanner(verdict, spectralFeatures);
                updateResonanceFrequencyIcons(detectedPeaks);
                updateResonanceFrequencyToleranceBars(detectedPeaks, chart);
                chart.invalidate();
            }
        });
    }

    public void refreshFrequencyIcons(Map detectedPeaks) {
//        detectedPeaks.
    }

    public List convertToListOfSpectralPeaks(LinkedList<Integer> peakIndexes, List<SpectralPeak> peakList, float[] currentMagnitudes) {

        for (Integer element : peakIndexes) {
            SpectralPeak peak = new SpectralPeak(
                    0f,
                    binIndexToHz(element),
                    currentMagnitudes[element],
                    binIndexToHz(element),
                    element
            );
            peakList.add(peak);
        }
        return peakList;
    }

    class ResonanceFrequencyIconResourceHolder {
        public Integer C0D2_default = R.drawable.ic_c0d2;
        public Integer C0D2_check = R.drawable.ic_c0d3_check;
        public Integer C0D3_default = R.drawable.ic_c0d3;
        public Integer C0D3_check = R.drawable.ic_c0d3_check;
        public Integer C0D4_default = R.drawable.ic_c0d4;
        public Integer C0D4_check = R.drawable.ic_c0d4_check;

        ResonanceFrequencyIconResourceHolder() {
            //
        }


    }
}