package com.pingcoin.android.pingcoin;

import java.io.File;

/**
 * Holds all the local event types that are fired from the MainActivity to the
 * {@link MediaPlayerHolder} via the EventBus.
 */
public class LocalEventFromMainActivity {



    public static class StartPlayback {
//        public File recordedFile;
//
//        StartPlayback(File recordedFile) {
//            this.recordedFile = recordedFile;
//        }
    }

    public static class ResetPlayback {

    }

    public static class PausePlayback {

    }

    public static class StopPlayback {

    }

    public static class StartRecord {

    }

    public static class StopRecord {

    }

    public static class StopUpdatingSeekbarWithMediaPosition {

    }

    public static class StartUpdatingSeekbarWithPlaybackPosition {

    }

    public static class SeekTo {

        public final int position;

        public SeekTo(int position) {
            this.position = position;
        }
    }

}