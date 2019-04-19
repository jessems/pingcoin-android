package com.pingcoin.android.pingcoin;


import java.io.FileDescriptor;

public interface PlayerAdapter {

    void loadMedia(int resourceId);

    void loadSavedFile(String fd);

    void release();

    boolean isPlaying();

    void play();

    void reset();

    void pause();

    void initializeProgressCallback();

    void seekTo(int position);
}