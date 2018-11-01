package com.pingcoin.android.pingcoin;

public class SpectralPeak{
    private final float frequencyInHertz;
    private final float magnitude;
    private final float referenceFrequency;
    private final int bin;
    /**
     * Timestamp in fractional seconds
     */
    private final float timeStamp;

    public SpectralPeak(float timeStamp,float frequencyInHertz, float magnitude,float referenceFrequency,int bin){
        this.frequencyInHertz = frequencyInHertz;
        this.magnitude = magnitude;
        this.referenceFrequency = referenceFrequency;
        this.timeStamp = timeStamp;
        this.bin = bin;
    }

    public float getTimeStamp(){
        return timeStamp;
    }

    public float getMagnitude(){
        return magnitude;
    }

    public float getFrequencyInHertz(){
        return frequencyInHertz;
    }

    public float getRefFrequencyInHertz(){
        return referenceFrequency;
    }

    public int getBin() {
        return bin;
    }
}