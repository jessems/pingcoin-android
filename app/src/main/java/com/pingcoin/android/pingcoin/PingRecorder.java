package com.pingcoin.android.pingcoin;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * record an audio clip and pass it to the listener
 *
 * @author gmilette
 *
 */
public class PingRecorder
{
    private static final String TAG = "AudioClipRecorder";

    private AudioRecord recorder;
    private AudioClipListener clipListener;

    private Context _mainContext;

    /**
     * state variable to control starting and stopping recording
     */
    private boolean continueRecording;

    public static final int RECORDER_SAMPLERATE_CD = 44100;
    public static final int RECORDER_SAMPLERATE_8000 = 8000;

    private static final int DEFAULT_BUFFER_INCREASE_FACTOR = 3;
    private static int READ_BUFFER_SIZE_IN_BYTES = 1024;

    private boolean heard;
    public AsyncRecord task;

    public PingRecorder(AudioClipListener clipListener)
    {
        this.clipListener = clipListener;
        heard = false;
//        task = null;
        _mainContext = SelectCoin.getContext();  // returns null

    }

    public PingRecorder(AudioClipListener clipListener, AsyncRecord task)
    {
        // Calls the first constructor
        this(clipListener);
        this.task = task;
    }



    /**
     * records with some default parameters
     */
    public PingRecording startRecording()
    {
        // Calls the alternate method declaration with default arguments filled in
        return startRecording(RECORDER_SAMPLERATE_CD,
                AudioFormat.ENCODING_PCM_16BIT);
    }

    /**
     * start recording: set the parameters that correspond to a buffer that
     * contains millisecondsPerAudioClip milliseconds of samples
     */
    public PingRecording startRecordingForTime(int millisecondsPerAudioClip,
                                         int sampleRate, int encoding)
    {
        float percentOfASecond = (float) millisecondsPerAudioClip / 1000.0f;
        int numSamplesRequired = (int) ((float) sampleRate * percentOfASecond);
        int bufferSize =
                determineCalculatedBufferSize(sampleRate, encoding,
                        numSamplesRequired);

        return doRecording(sampleRate, encoding, bufferSize,
                numSamplesRequired, DEFAULT_BUFFER_INCREASE_FACTOR);
    }

    /**
     * start recording: Use a minimum audio buffer and a read buffer of the same
     * size.
     */
    public PingRecording startRecording(final int sampleRate, int encoding)
    {
        // Determine the minimum buffer size based on the samplerate and encoding
        int bufferSize = determineMinimumBufferSize(sampleRate, encoding);
        // Record
        return doRecording(sampleRate, encoding, bufferSize, READ_BUFFER_SIZE_IN_BYTES,
                DEFAULT_BUFFER_INCREASE_FACTOR);
    }

    private int determineMinimumBufferSize(final int sampleRate, int encoding)
    {
        int minBufferSize =
                AudioRecord.getMinBufferSize(sampleRate,
                        AudioFormat.CHANNEL_IN_MONO, encoding);
        return minBufferSize;
    }

    /**
     * Calculate audio buffer size such that it holds numSamplesInBuffer and is
     * bigger than the minimum size<br>
     *
     * @param numSamplesInBuffer
     *            Make the audio buffer size big enough to hold this many
     *            samples
     */
    private int determineCalculatedBufferSize(final int sampleRate,
                                              int encoding, int numSamplesInBuffer)
    {
        int minBufferSize = determineMinimumBufferSize(sampleRate, encoding);

        int bufferSize;
        // each sample takes two bytes, need a bigger buffer
        if (encoding == AudioFormat.ENCODING_PCM_16BIT)
        {
            bufferSize = numSamplesInBuffer * 2;
        }
        else
        {
            bufferSize = numSamplesInBuffer;
        }

        if (bufferSize < minBufferSize)
        {
            Log.w(TAG, "Increasing buffer to hold enough samples "
                    + minBufferSize + " was: " + bufferSize);
            bufferSize = minBufferSize;
        }

        return bufferSize;
    }

    /**
     * Records audio until stopped the {@link #task} is canceled,
     * {@link #continueRecording} is false, or {@link #clipListener} returns
     * true <br>
     * records audio to a short [readBufferSize] and passes it to
     * {@link #clipListener} <br>
     * uses an audio buffer of size bufferSize * bufferIncreaseFactor
     *
     * @param recordingBufferSize
     *            minimum audio buffer size
     * @param readBufferSize
     *            reads a buffer of this size
     * @param bufferIncreaseFactor
     *            to increase recording buffer size beyond the minimum needed
     */
    private PingRecording doRecording(final int sampleRate, int encoding,
                                int recordingBufferSize, int readBufferSize,
                                int bufferIncreaseFactor)
    {

        PingRecording pingRecording = new PingRecording();

        pingRecording.heard = heard;
//        pingRecording.audioData = readBuffer;

        if (recordingBufferSize == AudioRecord.ERROR_BAD_VALUE)
        {
            Log.e(TAG, "Bad encoding value, see logcat");
            pingRecording.heard = false;
            return pingRecording;
        }
        else if (recordingBufferSize == AudioRecord.ERROR)
        {
            Log.e(TAG, "Error creating buffer size");
            pingRecording.heard = false;
            return pingRecording;
        }

        // give it extra space to prevent overflow
        int increasedRecordingBufferSize =
                recordingBufferSize * bufferIncreaseFactor;

        recorder =
                new AudioRecord(AudioSource.MIC, sampleRate,
                        AudioFormat.CHANNEL_IN_MONO, encoding,
                        increasedRecordingBufferSize);

        final short[] readBuffer = new short[readBufferSize];
        Log.i("PingRecorder", "readbufferSize: " + readBufferSize);

//        pingRecording.audioData = readBuffer;

        continueRecording = true;
        Log.d(TAG, "start recording, " + "recording bufferSize: "
                + increasedRecordingBufferSize
                + " read buffer size: " + readBufferSize);

        //Note: possible IllegalStateException
        //if audio recording is already recording or otherwise not available
        //AudioRecord.getState() will be AudioRecord.STATE_UNINITIALIZED
        recorder.startRecording();

        while (continueRecording)
        {
            // Recording gets read into the readBuffer
            int bufferResult = recorder.read(readBuffer, 0, readBufferSize);

            pingRecording.audioData = readBuffer;

            //in case external code stopped this while read was happening
            if ((!continueRecording) || ((task != null) && task.isCancelled()))
            {
                break;
            }
            // check for error conditions
            if (bufferResult == AudioRecord.ERROR_INVALID_OPERATION)
            {
                Log.e(TAG, "error reading: ERROR_INVALID_OPERATION");
            }
            else if (bufferResult == AudioRecord.ERROR_BAD_VALUE)
            {
                Log.e(TAG, "error reading: ERROR_BAD_VALUE");
            }
            else
            // no errors, do processing
            {
                heard = clipListener.heard(readBuffer, sampleRate);

                if (heard)
                {

                    Log.i("PingRecorder", "Called stopRecording");
//                    try {
////                        TimeUnit.MILLISECONDS.sleep(5);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    stopRecording();
                    if (pingRecording.audioData.length > 0) {
                        Log.i(TAG, "audioData length: " + Integer.toString(pingRecording.audioData.length));
//                        task.doProgress(pingRecording);
                        // TODO: Once the clapper threshold is reached, it seems like there are multiple data points for which it is reached.
                        // It will go through all of them one by one. This is not necessary of course.

                        stopRecording();
                        try {
                        TimeUnit.MILLISECONDS.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        AsyncRecord pingContinuousRecord = new AsyncRecord((Activity) _mainContext);
                        pingContinuousRecord.execute();

                        try {
                            TimeUnit.MILLISECONDS.sleep(30);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
        done();

//        return heard;


        return pingRecording;
    }


    public boolean isRecording()
    {
        return continueRecording;
    }

    public void stopRecording()
    {
        continueRecording = false;
    }

    /**
     * need to call this when completely done with recording
     */
    public void done()
    {
        Log.d("PingRecorder", "shut down recorder");
        if (recorder != null)
        {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    /**
     * @param audioData
     *            will be filled when reading the audio data
     */
    private void setOnPositionUpdate(final short[] audioData,
                                     final int sampleRate, int numSamplesInBuffer)
    {
        // possibly do it that way
        // setOnNotification(audioData, sampleRate, numSamplesInBuffer);

        OnRecordPositionUpdateListener positionUpdater =
                new OnRecordPositionUpdateListener()
                {
                    @Override
                    public void onPeriodicNotification(AudioRecord recorder)
                    {
                        // no need to read the audioData again since it was just
                        // read
                        heard = clipListener.heard(audioData, sampleRate);
                        if (heard)
                        {
                            Log.d(TAG, "heard audio");
                            stopRecording();
                        }
                    }

                    @Override
                    public void onMarkerReached(AudioRecord recorder)
                    {
                        Log.d(TAG, "marker reached");
                    }
                };
        // get notified after so many samples collected
        recorder.setPositionNotificationPeriod(numSamplesInBuffer);
        recorder.setRecordPositionUpdateListener(positionUpdater);
    }
}