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

import android.content.Context;
import android.util.Log;

import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

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
public class SpectrogramConstructor implements AudioProcessor {


    private final FFT fft;

    private final float[] currentMagnitudes;

    private OnsetHandler handler;

    private final float sampleRate;//samples per second (Hz)
    private long processedSamples;//in samples

    final static String TAG = "SpectrogramConstructor";

    private SelectCoin mainObject;
    private ArrayList<float[]> S;



    public SpectrogramConstructor(float sampleRate, int bufferSize, int frames, SelectCoin mainObject, ArrayList<float[]> S) {
        fft = new FFT(bufferSize, new HammingWindow());
        this.sampleRate = sampleRate;
        this.mainObject = mainObject;
        this.S = S;
        currentMagnitudes = new float[bufferSize/2];
    }



    @Override
    public boolean process(AudioEvent audioEvent) {
        float[] audioFloatBuffer = audioEvent.getFloatBuffer();
        this.processedSamples += audioFloatBuffer.length;
        this.processedSamples -= audioEvent.getOverlap();

        float timeStamp = processedSamples / sampleRate;


        float[] a = new float[]{0f, 0f, 0f, 0f};

        // TODO: Apparently we cannot do a FFT transform on a float array with zeros.
        // Why do we have a float array of zeros?
        // Problem seems to be that we are trying to access index 1024 in the array, when the length is 1024 (e.g. 1023 is last index)
        // Maybe it's the window?


        fft.forwardTransform(audioFloatBuffer);
        fft.modulus(audioFloatBuffer, currentMagnitudes);

        float[] audioSpectrumDouble;
        float[] audioSpectrum;
        audioSpectrum = currentMagnitudes.clone();

        Log.i("audioSpectrum",Arrays.toString(audioSpectrum));

        mainObject.addSliceToSpectrogram(S, audioSpectrum);
//        mainObject.addSliceToPeakogram(P, peaks);


        double[] doubleSpectrum = new double[audioSpectrum.length];

        for (int i = 0; i < audioSpectrum.length; ++i) {
            doubleSpectrum[i] = (double) audioSpectrum[i];
        }
        return false;
    }





    @Override
    public void processingFinished() {
    }


}