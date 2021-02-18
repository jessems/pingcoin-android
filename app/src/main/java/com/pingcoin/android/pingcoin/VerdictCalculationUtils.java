package com.pingcoin.android.pingcoin;

import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

class VerdictCalculationUtils {

    static String TAG = VerdictCalculationUtils.class.getSimpleName();

    public static Map<String, Boolean> getRecognizedResonanceFrequencies(LinkedList<Float> detectedPeaksHz, Map<String, Float> resonanceFrequencies) {
        boolean C0D2Detected = false;
        boolean C0D3Detected = false;
        boolean C0D4Detected = false;
        Map<String, Boolean> recognizedResonanceFrequencies = new HashMap();


        if(detectedPeaksHz.size() < 50) {
            for (float realPeakvalue : detectedPeaksHz) {
                float peak = realPeakvalue;
                if ((peak > ((float) resonanceFrequencies.get("C0D2") * (1 - resonanceFrequencies.get("C0D2Error")))) && (peak < ((float) resonanceFrequencies.get("C0D2") * (1 + resonanceFrequencies.get("C0D2Error"))))) {
                    C0D2Detected = true;
                } else if ((peak > ((float) resonanceFrequencies.get("C0D3") * (1 - resonanceFrequencies.get("C0D3Error")))) && (peak < ((float) resonanceFrequencies.get("C0D3") * (1 + resonanceFrequencies.get("C0D3Error"))))) {
                    C0D3Detected = true;
                } else if ((peak > ((float) resonanceFrequencies.get("C0D4") * (1 - resonanceFrequencies.get("C0D4Error")))) && (peak < ((float) resonanceFrequencies.get("C0D4") * (1 + resonanceFrequencies.get("C0D4Error"))))) {
                    C0D4Detected = true;
                }
            }
        } else {
            Log.e(TAG, "Too many peaks to get the recognized peaks.");
        }

        recognizedResonanceFrequencies.put("C0D2", C0D2Detected);
        recognizedResonanceFrequencies.put("C0D3", C0D3Detected);
        recognizedResonanceFrequencies.put("C0D4", C0D4Detected);

        return recognizedResonanceFrequencies;
    }

    public static Verdict getVerdict(Map<String,Boolean> detectedPeaks, Map<String, Double> spectralFeatures, Map<String, Double> featureThresholds) {
        boolean pingIsClean = false;
//        String verdict = "NONE";
        Verdict verdict;

        int amountOfFrequenciesDetected =
                booleanToInt(detectedPeaks.get("C0D2")) +
                        booleanToInt(detectedPeaks.get("C0D3")) +
                        booleanToInt(detectedPeaks.get("C0D4"));


//        if(spectralFeatures.get("spectralCentroid") > featureThresholds.get("spectralCentroidThreshold") &&
//                spectralFeatures.get("spectralFlatness") < featureThresholds.get("spectralFlatnessThreshold") &&
//                spectralFeatures.get("spectralMedian") < featureThresholds.get("spectralMedianThreshold")) {
//            pingIsClean = true;
//        }

//        if(spectralFeatures.get("inverseSpectralFlatness") > 3) {
//            Log.i(TAG, "spectral flatness" + Double.toString(spectralFeatures.get("inverseSpectralFlatness")));
//        }
        if(spectralFeatures.get("inverseSpectralFlatness") > 2.5 && spectralFeatures.get("spectralBandwidth") < 50 && spectralFeatures.get("spectralCentroid") > 300) {
            Log.i(TAG, "spectral bandwidth: " + Double.toString(spectralFeatures.get("spectralBandwidth")));
            Log.i(TAG, "spectral flatness: " + Double.toString(spectralFeatures.get("inverseSpectralFlatness")));
            Log.i(TAG, "spectral centroid: " + Double.toString(spectralFeatures.get("spectralCentroid")));
        }


        if(spectralFeatures.get("inverseSpectralFlatness") > 2 && spectralFeatures.get("spectralBandwidth") < 6 && spectralFeatures.get("spectralCentroid") > 500) {
            pingIsClean = true;
            Log.i(TAG, "spectral bandwidth: " + Double.toString(spectralFeatures.get("spectralBandwidth")));
            Log.i(TAG, "spectral flatness: " + Double.toString(spectralFeatures.get("inverseSpectralFlatness")));
            Log.i(TAG, "spectral centroid: " + Double.toString(spectralFeatures.get("spectralCentroid")));
        }

        if (amountOfFrequenciesDetected == 0 && !pingIsClean) {
            verdict = Verdict.ZERO_RESONANCE_FREQUENCIES_RECOGNIZED_NOT_CLEAN;
        } else if (amountOfFrequenciesDetected == 0 && pingIsClean) {
            verdict = Verdict.ZERO_RESONANCE_FREQUENCIES_RECOGNIZED_CLEAN;
        } else if (amountOfFrequenciesDetected == 1 && !pingIsClean) {
            verdict = Verdict.ONE_RESONANCE_FREQUENCY_RECOGNIZED_NOT_CLEAN;
        } else if (amountOfFrequenciesDetected == 1 && pingIsClean) {
            verdict = Verdict.ONE_RESONANCE_FREQUENCY_RECOGNIZED_CLEAN;
        } else if (amountOfFrequenciesDetected == 2 && !pingIsClean) {
            verdict = Verdict.TWO_RESONANCE_FREQUENCIES_RECOGNIZED_NOT_CLEAN;
        } else if (amountOfFrequenciesDetected == 2 && pingIsClean) {
            verdict = Verdict.TWO_RESONANCE_FREQUENCIES_RECOGNIZED_CLEAN;
        } else if (amountOfFrequenciesDetected == 3 && !pingIsClean) {
            verdict = Verdict.THREE_RESONANCE_FREQUENCIES_RECOGNIZED_NOT_CLEAN;
        } else if (amountOfFrequenciesDetected == 3 && pingIsClean) {
            verdict = Verdict.THREE_RESONANCE_FREQUENCIES_RECOGNIZED_CLEAN;
        } else {
            verdict = Verdict.NONE;
        }
        return verdict;
    }

    public static int booleanToInt(boolean bool) {
        return bool ? 1 : 0;
    }
}
