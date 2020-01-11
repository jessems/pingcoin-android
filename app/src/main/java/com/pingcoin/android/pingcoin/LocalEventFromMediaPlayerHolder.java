package com.pingcoin.android.pingcoin;

public class LocalEventFromMediaPlayerHolder {

    public static class UpdateLog {

        public final StringBuffer formattedMessage;

        public UpdateLog(StringBuffer formattedMessage) {
            this.formattedMessage = formattedMessage;
        }
    }

    public static class PlaybackDuration {

        public final int duration;

        public PlaybackDuration(int duration) {
            this.duration = duration;
        }
    }

    public static class PlaybackPosition {

        public final int position;

        public PlaybackPosition(int position) {
            this.position = position;
        }
    }

    public static class PlaybackCompleted {

    }

    public static class StateChanged {

        public final RecordingState currentState;

        public StateChanged(RecordingState currentState) {
            this.currentState = currentState;
        }
    }

}