package com.pingcoin.android.pingcoin;

import java.io.File;

public class MediaStateChangeEvent {

    public static class StateChanged {

        public final RecordingState currentState;
        public File recordedFile;

        public StateChanged(RecordingState currentState) {
            this.currentState = currentState;
        }

        public void setRecordedFile(File recordedFile) {
            this.recordedFile = recordedFile;
        }
    }

}