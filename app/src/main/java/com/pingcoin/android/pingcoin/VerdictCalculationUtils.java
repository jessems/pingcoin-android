package com.pingcoin.android.pingcoin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

class VerdictCalculationUtils {

    VerdictCalculationUtils() {
        //
    }

    public static Map<String, Boolean> getRecognizedResonanceFrequencies(LinkedList<Float> binnedPeaksExceedingThreshHz, Map<String, Float> resonanceFrequencies) {
        boolean C0D2Detected = false;
        boolean C0D3Detected = false;
        boolean C0D4Detected = false;
        Map<String, Boolean> recognizedResonanceFrequencies = new HashMap();

        for (float realPeakvalue : binnedPeaksExceedingThreshHz) {
            float peak = realPeakvalue;
            if ((peak > ((float) resonanceFrequencies.get("C0D2") * (1 - resonanceFrequencies.get("tolerance")))) && (peak < ((float) resonanceFrequencies.get("C0D2") * (1 + resonanceFrequencies.get("tolerance"))))) {
                C0D2Detected = true;
            } else if ((peak > ((float) resonanceFrequencies.get("C0D3") * (1 - resonanceFrequencies.get("tolerance")))) && (peak < ((float) resonanceFrequencies.get("C0D3") * (1 + resonanceFrequencies.get("tolerance"))))) {
                C0D3Detected = true;
            } else if ((peak > ((float) resonanceFrequencies.get("C0D4") * (1 - resonanceFrequencies.get("tolerance")))) && (peak < ((float) resonanceFrequencies.get("C0D4") * (1 + resonanceFrequencies.get("tolerance"))))) {
                C0D4Detected = true;
            }
        }
        recognizedResonanceFrequencies.put("C0D2", C0D2Detected);
        recognizedResonanceFrequencies.put("C0D3", C0D3Detected);
        recognizedResonanceFrequencies.put("C0D4", C0D4Detected);

        return recognizedResonanceFrequencies;
    }

    public static String getVerdict(Map<String,Boolean> detectedPeaks, Map<String, Double> spectralFeatures, Map<String, Double> featureThresholds) {
        boolean pingIsClean = false;
        String verdict = "NONE";

        int amountOfFrequenciesDetected =
                booleanToInt(detectedPeaks.get("C0D2")) +
                        booleanToInt(detectedPeaks.get("C0D3")) +
                        booleanToInt(detectedPeaks.get("C0D4"));


        if(spectralFeatures.get("spectralCentroid") > featureThresholds.get("spectralCentroidThreshold") &&
                spectralFeatures.get("spectralFlatness") < featureThresholds.get("spectralFlatnessThreshold") &&
                spectralFeatures.get("spectralMedian") < featureThresholds.get("spectralMedianThreshold")) {
            pingIsClean = true;
        }

        if (amountOfFrequenciesDetected == 0 && !pingIsClean) {
            verdict = "ZERO_PEAKS_DETECTED_NOT_CLEAN";
        } else if (amountOfFrequenciesDetected == 0 && pingIsClean) {
            verdict = "ZERO_PEAKS_DETECTED_CLEAN";
        } else if (amountOfFrequenciesDetected == 1 && !pingIsClean) {
            verdict = "ONE_PEAK_DETECTED_NOT_CLEAN";
        } else if (amountOfFrequenciesDetected == 1 && pingIsClean) {
            verdict = "ONE_PEAK_DETECTED_CLEAN";
        } else if (amountOfFrequenciesDetected == 2 && !pingIsClean) {
            verdict = "TWO_PEAKS_DETECTED_NOT_CLEAN";
        } else if (amountOfFrequenciesDetected == 2 && pingIsClean) {
            verdict = "TWO_PEAKS_DETECTED_CLEAN";
        } else if (amountOfFrequenciesDetected == 3 && !pingIsClean) {
            verdict = "THREE_PEAKS_DETECTED_NOT_CLEAN";
        } else if (amountOfFrequenciesDetected == 3 && pingIsClean) {
            verdict = "THREE_PEAKS_DETECTED_CLEAN";
        }
        return verdict;
    }

    public static int booleanToInt(boolean bool) {
        return bool ? 1 : 0;
    }
}
