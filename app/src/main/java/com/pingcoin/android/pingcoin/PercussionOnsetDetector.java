/*
*      _______                       _____   _____ _____
*     |__   __|                     |  __ \ / ____|  __ \
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|
*
* -------------------------------------------------------------
*
* TarsosDSP is developed by Joren Six at IPEM, University Ghent
*
* -------------------------------------------------------------
*
*  Info: http://0110.be/tag/TarsosDSP
*  Github: https://github.com/JorenSix/TarsosDSP
*  Releases: http://0110.be/releases/TarsosDSP/
*
*  TarsosDSP includes modified source code by various authors,
*  for credits and info, see README.
*
*/


package com.pingcoin.android.pingcoin;

import android.util.Log;

import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;


import java.util.Arrays;
import java.util.LinkedList;

import static java.util.Arrays.fill;

/**
 * <p>
 * Estimates the locations of percussive onsets using a simple method described
 * in <a
 * href="http://arrow.dit.ie/cgi/viewcontent.cgi?article=1018&context=argcon"
 * >"Drum Source Separation using Percussive Feature Detection and Spectral
 * Modulation"</a> by Dan Barry, Derry Fitzgerald, Eugene Coyle and Bob Lawlor,
 * ISSC 2005.
 * </p>
 * <p>
 * Implementation based on a <a href=
 * "http://vamp-plugins.org/code-doc/PercussionOnsetDetector_8cpp-source.html"
 * >VAMP plugin example</a> by Chris Cannam at Queen Mary, London:
 *
 * <pre>
 *  Centre for Digital Music, Queen Mary, University of London.
 *  Copyright 2006 Chris Cannam.
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use, copy,
 *  modify, merge, publish, distribute, sublicense, and/or sell copies
 *  of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR
 *  ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 *  CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *  Except as contained in this notice, the names of the Centre for
 *  Digital Music; Queen Mary, University of London; and Chris Cannam
 *  shall not be used in advertising or otherwise to promote the sale,
 *  use or other dealings in this Software without prior written
 *  authorization.
 * </pre>
 *
 * </p>
 *
 * @author Joren Six
 * @author Chris Cannam
 */
public class PercussionOnsetDetector implements AudioProcessor, OnsetDetector {

    public static final double DEFAULT_THRESHOLD = 8;

    public static final double DEFAULT_SENSITIVITY = 20;

    private final FFT fft;

    private final float[] priorMagnitudes;
    private final float[] currentMagnitudes;

    private float dfMinus1, dfMinus2;

    private OnsetHandler handler;

    private final float sampleRate;//samples per second (Hz)
    private long processedSamples;//in samples

    final static String TAG = "PercussionOnsetDetector";

    /**
     * Sensitivity of peak detector applied to broadband detection function (%).
     * In [0-100].
     */
    private final double sensitivity;

    /**
     * Energy rise within a frequency bin necessary to count toward broadband
     * total (dB). In [0-20].
     *
     */
    private final double threshold;

    /**
     * Create a new percussion onset detector. With a default sensitivity and threshold.
     *
     * @param sampleRate
     *            The sample rate in Hz (used to calculate timestamps)
     * @param bufferSize
     *            The size of the buffer in samples.
     * @param bufferOverlap
     *            The overlap of buffers in samples.
     * @param handler
     *            An interface implementor to handle percussion onset events.
     */
    public PercussionOnsetDetector(float sampleRate, int bufferSize,
                                   int bufferOverlap, OnsetHandler handler) {
        this(sampleRate, bufferSize, handler,
                DEFAULT_SENSITIVITY, DEFAULT_THRESHOLD);
    }

    /**
     * Create a new percussion onset detector.
     *
     * @param sampleRate
     *            The sample rate in Hz (used to calculate timestamps)
     * @param bufferSize
     *            The size of the buffer in samples.
     * @param handler
     *            An interface implementor to handle percussion onset events.
     * @param sensitivity
     *            Sensitivity of the peak detector applied to broadband
     *            detection function (%). In [0-100].
     * @param threshold
     *            Energy rise within a frequency bin necessary to count toward
     *            broadband total (dB). In [0-20].
     */
    public PercussionOnsetDetector(float sampleRate, int bufferSize, OnsetHandler handler, double sensitivity, double threshold) {
        fft = new FFT(bufferSize);
        this.threshold = threshold;
        this.sensitivity = sensitivity;
        priorMagnitudes = new float[bufferSize/2];
        currentMagnitudes = new float[bufferSize/2];
        this.handler = handler;
        this.sampleRate = sampleRate;

    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        float[] audioFloatBuffer = audioEvent.getFloatBuffer();
        this.processedSamples += audioFloatBuffer.length;
        this.processedSamples -= audioEvent.getOverlap();

        float[] a = new float[] {0f, 0f, 0f, 0f};

        // TODO: Apparently we cannot do a FFT transform on a float array with zeros.
        // Why do we have a float array of zeros?
        // Problem seems to be that we are trying to access index 1024 in the array, when the length is 1024 (e.g. 1023 is last index)
        // Maybe it's the window?

//        fft.forwardTransform(a);


        fft.forwardTransform(audioFloatBuffer);
        fft.modulus(audioFloatBuffer, currentMagnitudes);

        float[] audioSpectrumDouble;
        float[] audioSpectrum;
        audioSpectrum = currentMagnitudes.clone();

//        audioSpectrum = Arrays.copyOfRange(audioSpectrumDouble, 0, audioSpectrumDouble.length/2);

        double[] doubleSpectrum = new double[audioSpectrum.length];

        for (int i = 0; i < audioSpectrum.length; ++i) {
            doubleSpectrum[i] = (double) audioSpectrum[i];
        }



        // Get top 3 bands

        // Divide cumulative power of top 3 bands by other bands (threshold 3)

        // Get average frequency of top 3 bands (threshold 2)


// Old percussion detection code
        int binsOverThreshold = 0;
        for (int i = 0; i < currentMagnitudes.length; i++) {
            if (priorMagnitudes[i] > 0.f) {
                double diff = 10 * Math.log10(currentMagnitudes[i]
                        / priorMagnitudes[i]);
                if (diff >= threshold) {
                    binsOverThreshold++;
                }
            }
            priorMagnitudes[i] = currentMagnitudes[i];
        }


        // Divide spectrum into bands
        float[] bandArray;
        bandArray = chunkArray(audioSpectrum, 64);

        float[] bandArraySorted = Arrays.copyOf(bandArray, bandArray.length);
        Arrays.sort(bandArraySorted);


        float median;
        if (bandArraySorted.length % 2 == 0)
            median = (bandArraySorted[bandArraySorted.length/2] + bandArraySorted[bandArraySorted.length/2 - 1])/2;
        else
            median = bandArraySorted[bandArraySorted.length/2];

        float[] topBandsArray = Arrays.copyOfRange(bandArraySorted,bandArraySorted.length-4,bandArraySorted.length);
        float[] bottomBandsArray = Arrays.copyOfRange(bandArraySorted, 0, bandArraySorted.length );

        float sumTopBands = 0;
        float sumBottomBands = 0;

        for (float top : topBandsArray)
            sumTopBands += top;

        for (float bottom : bottomBandsArray)
            sumBottomBands += bottom;

        float peakinessRatio = sumTopBands / sumBottomBands;


//        Log.i("PercussionOnsetDetector", "peakinessRatio: " + peakinessRatio);

        float com = centerOfMass(bandArray);

        Kurtosis kurtosisObject = new Kurtosis();

        double kurtosis = kurtosisObject.evaluate(doubleSpectrum);

        double[] spectrumDoubleArray = new double[audioSpectrum.length];

        for(int i=0; i<audioSpectrum.length; ++i) {
            spectrumDoubleArray[i] = (double) audioSpectrum[i];
        }

        LinkedList<Integer> peaks = new LinkedList<Integer>();
        peaks = Peaks.findPeaks(spectrumDoubleArray, 20, 10, 0, true);

        float lastPeak = 0;

//        lastPeak = (peaks.getLast() / 1024) * sampleRate;
//        Log.i(TAG, "Last peak: " + lastPeak);


        double peakRatio = 0;
        if (peaks.size() > 2) {
            if (peaks.get(0) == 0) {
                peakRatio = (double) peaks.get(1) / peaks.get(2);
//                Log.i("PercussionOnsetDetector", "first peak: " + peaks.get(1));
//                Log.i("PercussionOnsetDetector", "second peak: " + peaks.get(2));

            } else {
                peakRatio = (double) peaks.get(0) / peaks.get(1);
//                Log.i("PercussionOnsetDetector", "first peak: " + peaks.get(0));
//                Log.i("PercussionOnsetDetector", "second peak: " + peaks.get(1));
            }
            Log.i("PercussionOnsetDetector", "peak ratio: " + peakRatio);

        }

        if (dfMinus2 < dfMinus1
                && dfMinus1 >= binsOverThreshold
                && dfMinus1 > ((100 - sensitivity) * audioFloatBuffer.length) / 200) {
            Log.i("PercussionOnsetDetector", "sumTopBands: " + sumTopBands);
            Log.i("PercussionOnsetDetector", "sumBottomBands: " + sumBottomBands);
            Log.i("PercussionOnsetDetector", "Peakiness ratio: " + peakinessRatio);
            Log.i("PercussionOnsetDetector", "Center of mass: " + com);
//            Log.i("PercussionOnsetDetector", "kurtosis: " + kurtosis);
//            Log.i("PercussionOnsetDetector", "dfMinu1: " + dfMinus1);
            Log.i("PercussionOnsetDetector", "peaks: " + peaks.size());
//            Log.i("PercussionOnsetDetector", "First peak: " + firstPeak);



        }



        if (dfMinus2 < dfMinus1
                && dfMinus1 >= binsOverThreshold
                && dfMinus1 > ((100 - sensitivity) * audioFloatBuffer.length) / 200
//                && peakinessRatio > 0.5
//                && kurtosis > 50
//                && com < 0.2
//                && (peakRatio > 0.3 && peakRatio < 0.5)
//                && (peaks.size() > 2 && peaks.size() < 10)
                ) {




            float timeStamp = processedSamples / sampleRate;
            handler.handleOnset(audioSpectrum,timeStamp,-1);

        }

        dfMinus2 = dfMinus1;
        dfMinus1 = binsOverThreshold;
//        Log.i("PercussionOnsetDetector", "dfMinus2: " + dfMinus2);
//        Log.i("PercussionOnsetDetector", "dfMinus1: " + dfMinus1);

        return true;
    }

    @Override
    public void processingFinished() {
    }

    @Override
    public void setHandler(OnsetHandler handler) {
        this.handler = handler;
    }

    public static float[] chunkArray(float[] array, int numOfChunks) {
        int chunkSize = (int)Math.ceil((double)array.length / numOfChunks);
        float[] output = new float[numOfChunks];

        for(int i = 0; i < numOfChunks; ++i) {
            int start = i * chunkSize;
            int length = Math.min(array.length - start, chunkSize);

            float[] temp = new float[length];
            System.arraycopy(array, start, temp, 0, length);

            float sum = 0;
            for (float n : temp)
                sum += n;

            output[i] = sum;
        }

        return output;
    }

    public static float centerOfMass(float[] array) {
        float weighted = 0;
        float total = 0;
        for(int i = 0; i < array.length; ++i) {

            weighted += i * array[i];
            total += i;
        }
//        Log.i("What?", "Weighted: " + weighted + " total: " + total);
        return weighted / total;
    }

}
