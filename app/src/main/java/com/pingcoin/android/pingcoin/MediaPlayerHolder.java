package com.pingcoin.android.pingcoin;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;
import android.widget.SeekBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.annotation.RequiresApi;

/**
 * Exposes the functionality of the {@link MediaPlayer} and implements the {@link PlayerAdapter}
 * so that  can control music playback.
 */
public final class MediaPlayerHolder {


    public static final int SEEKBAR_REFRESH_INTERVAL_MS = 100;
    private static final String TAG = "example dialog";

    private final Context mContext;
    private MediaPlayer mMediaPlayer;
    private int mResourceId;
    private PlaybackInfoListener mPlaybackInfoListener;
    private ScheduledExecutorService mExecutor;
    private Runnable mSeekbarPositionUpdateTask;
    private Runnable mSeekbarProgressUpdateTask;
    private boolean isUserSeeking;
    private ArrayList<String> mLogMessages = new ArrayList<>();
    SeekBar mSeekbarAudio;

    public MediaPlayerHolder(Context context) {

        mContext = context.getApplicationContext();
        EventBus.getDefault().register(this);

    }

    /**
     * Once the {@link MediaPlayer} is released, it can't be used again, and another one has to be
     * created. In the onStop() method of the  the {@link MediaPlayer} is
     * released. Then in the onStart() of the  a new {@link MediaPlayer}
     * object has to be created. That's why this method is private, and called by load(int) and
     * not the constructor.
     */



    public void initializeMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnCompletionListener(mediaPlayer -> {
                stopUpdatingSeekbarWithPlaybackProgress(true);
                logToUI("MediaPlayer playback completed");
                EventBus.getDefault().post(
                        new LocalEventFromMediaPlayerHolder
                                .PlaybackCompleted());
                EventBus.getDefault()
                        .post(new MediaStateChangeEvent.StateChanged(RecordingState.Recorded));

            });
            logToUI("mMediaPlayer = new MediaPlayer()");
        }
    }

    public void setPlaybackInfoListener(PlaybackInfoListener listener) {
        mPlaybackInfoListener = listener;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void loadMedia(int resourceId) {
        mResourceId = resourceId;

        initializeMediaPlayer();

        AssetFileDescriptor assetFileDescriptor =
                mContext.getResources().openRawResourceFd(mResourceId);
        try {
            logToUI("load() {1. setDataSource}");
            mMediaPlayer.setDataSource(assetFileDescriptor);
        } catch (Exception e) {
            logToUI(e.toString());
        }

        try {
            logToUI("load() {2. prepare}");
            mMediaPlayer.prepare();
        } catch (Exception e) {
            logToUI(e.toString());
        }
        initSeekbar();
        logToUI("initializeProgressCallback()");
    }

    public void loadSavedFile(String fd) {
        initializeMediaPlayer();

        try {
            logToUI("load() {1. setDataSource}");
            mMediaPlayer.setDataSource(fd);
        } catch (Exception e) {
            logToUI(e.toString());
        }

        try {
            logToUI("load() {2. prepare}");
            mMediaPlayer.prepare();
        } catch (Exception e) {
            logToUI(e.toString());
        }
        initSeekbar();

    }

    public void initSeekbar() {
        // Set the duration.
        final int duration = mMediaPlayer.getDuration();
        EventBus.getDefault().post(
                new LocalEventFromMediaPlayerHolder.PlaybackDuration(duration));
        logToUI(String.format("setting seekbar max %d sec",
                TimeUnit.MILLISECONDS.toSeconds(duration)));
    }


    public void release() {
        if (mMediaPlayer != null) {
            logToUI("release() and mMediaPlayer = null");
            mMediaPlayer.release();
            mMediaPlayer = null;
            EventBus.getDefault().unregister(this);
        }
    }


    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }


    public void play() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
//            logToUI(String.format("playbackStart() %s",
//                    mContext.getResources().getResourceEntryName(mResourceId)));
            Log.i(TAG, "Tracking info: " + mMediaPlayer.getTrackInfo().toString());
            mMediaPlayer.start();
            startUpdatingSeekbarWithPlaybackProgress();
            EventBus.getDefault()
                    .post(new MediaStateChangeEvent.StateChanged(RecordingState.Playing));
        }
    }



    public void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            logToUI("playbackPause()");
            EventBus.getDefault()
                    .post(new MediaStateChangeEvent.StateChanged(RecordingState.Paused));  // Put stopped here watch out
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void reset() {
        if (mMediaPlayer != null) {
            logToUI("playbackReset()");
            mMediaPlayer.reset();
//            loadMedia(mResourceId);
            stopUpdatingSeekbarWithPlaybackProgress(true);
            EventBus.getDefault()
                    .post(new MediaStateChangeEvent.StateChanged(RecordingState.Recorded));
        }
    }


    public void seekTo(int position) {
        if (mMediaPlayer != null) {
            logToUI(String.format("seekTo() %d ms", position));
            mMediaPlayer.seekTo(position);
        }
    }


    private void stopUpdatingSeekbarWithPlaybackProgress(boolean resetUIPlaybackPosition) {
        mExecutor.shutdownNow();
        mExecutor = null;
        mSeekbarProgressUpdateTask = null;
        if (resetUIPlaybackPosition) {
            EventBus.getDefault().post(new LocalEventFromMediaPlayerHolder.PlaybackPosition(0));
        }
    }

    private void updateProgressCallbackTask() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            int currentPosition = mMediaPlayer.getCurrentPosition();
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onPositionChanged(currentPosition);
            }
        }
    }

    private void startUpdatingSeekbarWithPlaybackProgress() {
        // Setup a recurring task to sync the mMediaPlayer position with the Seekbar.
        if (mExecutor == null) {
            mExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        if (mSeekbarProgressUpdateTask == null) {
            mSeekbarProgressUpdateTask = () -> {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    int currentPosition = mMediaPlayer.getCurrentPosition();
                    EventBus.getDefault().post(
                            new LocalEventFromMediaPlayerHolder.PlaybackPosition(
                                    currentPosition));
                }
            };
        }
        mExecutor.scheduleAtFixedRate(
                mSeekbarProgressUpdateTask,
                0,
                SEEKBAR_REFRESH_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(LocalEventFromMainActivity.SeekTo event) {
        seekTo(event.position);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(
            LocalEventFromMainActivity.StopUpdatingSeekbarWithMediaPosition event) {
        stopUpdatingSeekbarWithPlaybackProgress(false);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(
            LocalEventFromMainActivity.StartUpdatingSeekbarWithPlaybackPosition event) {
        startUpdatingSeekbarWithPlaybackProgress();
    }

    // Logging to UI methods.

    public void logToUI(String msg) {
        mLogMessages.add(msg);
        fireLogUpdate();
    }

    /**
     * update the MainActivity's UI with the debug log messages
     */
    public void fireLogUpdate() {
        StringBuffer formattedLogMessages = new StringBuffer();
        for (int i = 0; i < mLogMessages.size(); i++) {
            formattedLogMessages.append(i)
                    .append(" - ")
                    .append(mLogMessages.get(i));
            if (i != mLogMessages.size() - 1) {
                formattedLogMessages.append("\n");
            }
        }
        EventBus.getDefault().post(
                new LocalEventFromMediaPlayerHolder.UpdateLog(formattedLogMessages));
    }



    // Handle user input for Seekbar changes.

    public void setupSeekbar() {
        mSeekbarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // This holds the progress value for onStopTrackingTouch.
            int userSelectedPosition = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Only fire seekTo() calls when user stops the touch event.
                if (fromUser) {
                    userSelectedPosition = progress;
                    isUserSeeking = true;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                EventBus.getDefault().post(new LocalEventFromMainActivity.SeekTo(
                        userSelectedPosition));
            }
        });
    }

    // Respond to playback localevents.

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(LocalEventFromMainActivity.PausePlayback event) {
        pause();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(LocalEventFromMainActivity.StopPlayback event) {
        Log.i(TAG, "StopPlayback event received in Event Bus");
        pause();
        seekTo(0);
        stopUpdatingSeekbarWithPlaybackProgress(true);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(LocalEventFromMainActivity.StartPlayback event) {
        Log.i(TAG, "StartPlayback event received in Event Bus");
        play();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(LocalEventFromMainActivity.ResetPlayback event) {
        Log.i(TAG, "ResetPlayback event received in Event Bus");
        reset();
    }







}