package com.pingcoin.android.pingcoin;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;


public class SubmitCoinDialog extends DialogFragment implements EasyPermissions.PermissionCallbacks {

    public static final String TAG = "example dialog";
    private static final String APP_RECORDING_DIRECTORY = "Pingcoin";
    private static final String FIREBASE_RECORDING_DIR = "recordings";
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final String DIALOG_TITLE = "Submit a coin";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 123;
    private static final int REQUEST_CAMERA_PERMISSION = 321;

    private Bitmap mImageBitmap;
    private Toolbar toolbar;
    AudioDispatcher dispatcher;
    private Thread recordingThread;
    String currentPhotoPath;
    private int bufferSize;
    private File coinRecording;
    private File coinPicture;
    private ImageView coinThumbnail;
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private boolean isUserSeeking;
    private MediaPlayerHolder mMediaPlayerHolder;

    private RandomAccessFile outputFile;


    private Button mTakePictureButton;

    private MaterialButton mRecordButton;
    private MaterialButton mStopButton;
    private MaterialButton mPlayButton;
    private MaterialButton mPauseButton;

    private SeekBar mSeekbarAudio;


    private final AtomicReference<RecordingState> recordingStateReference = new AtomicReference<>();
    private final AtomicReference<PictureState> pictureStateReference = new AtomicReference<>();

    String[] permissions = new String[]{Manifest.permission.INTERNET, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,};
    private DatabaseReference mDatabase;

    StorageReference storageRef;
    FirebaseStorage storage;

    private enum FileType {
        RECORDING, PICTURE
    }

    private File pathName;
    private String timestamp;
    private MaterialButton mSubmitButton;
    private String userEmailAddress;
    private EditText mEmailField;
    private View mLayoutContainer;
    private boolean coinRecordingUploaded = false;
    private boolean coinPictureUploaded = false;
    private ProgressBar mProgressbar;
    private View mProgressbarLinearLayout;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;


    public static SubmitCoinDialog display(FragmentManager fragmentManager) {
        SubmitCoinDialog submitCoinDialog = new SubmitCoinDialog();
        submitCoinDialog.show(fragmentManager, TAG);
        return submitCoinDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);


        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING) * 4;


        // Prevent Fragment from getting recreated on rotation (which would screw up the playback)
        // (For an activity this needs to be done in the manifest file)
        setRetainInstance(true);

        this.coinRecording = null;
        this.coinPicture = null;

        setupFirebase();
    }

    private void setupFirebase() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        mAuth = FirebaseAuth.getInstance();
        this.currentUser = mAuth.getCurrentUser();
    }

    private void bindViews() {
        View view = getView();

        mProgressbar = view.findViewById(R.id.progressBar);
        mProgressbarLinearLayout = view.findViewById(R.id.layout_container_progressbar);

        mLayoutContainer = view.findViewById(R.id.layout_container);

        mRecordButton = view.findViewById(R.id.button_record);
        mStopButton = view.findViewById(R.id.button_stop);
        mPlayButton = view.findViewById(R.id.button_play);
        mPauseButton = view.findViewById(R.id.button_pause);


        mSeekbarAudio = view.findViewById(R.id.seekbar_audio);

        mEmailField = view.findViewById(R.id.emailField);

        mTakePictureButton = view.findViewById(R.id.take_picture_button);
        coinThumbnail = view.findViewById(R.id.coin_thumbnail);
        mSubmitButton = view.findViewById(R.id.button_submit);
    }

    public void displayToast(RecordingState state) {
        Toast.makeText(getContext(), String.format("State changed to:%s", state), Toast.LENGTH_SHORT).show();
    }

    private void setButtonDisabled(MaterialButton button) {
        button.setAlpha(0.5f);
        button.setClickable(false);
    }

    private void setButtonEnabled(MaterialButton button) {
        button.setAlpha(1f);
        button.setClickable(true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MediaStateChangeEvent.StateChanged event) {
        Boolean debug = true;


        switch (event.currentState) {

            case Unrecorded:
                Log.i(TAG, "State is UNRECORDED");
                setButtonEnabled(mRecordButton);
                setButtonDisabled(mStopButton);
                setButtonDisabled(mPlayButton);
                setButtonDisabled(mPauseButton);

                recordingStateReference.set(RecordingState.Unrecorded);

                if (debug == true) {
                    Log.i(TAG, "State is " + event.currentState.toString());
                    displayToast(event.currentState);
                }


                break;

            case Recording:
                Log.i(TAG, "State is RECORDING");
                setButtonDisabled(mRecordButton);
                setButtonEnabled(mStopButton);
                setButtonDisabled(mPlayButton);
                setButtonDisabled(mPauseButton);

                recordingStateReference.set(RecordingState.Recording);

                if (debug == true) {
                    Log.i(TAG, "State is " + event.currentState.toString());
                    displayToast(event.currentState);
                }
                break;

            case Recorded:
                Log.i(TAG, "State is RECORDED");
                setButtonEnabled(mRecordButton);
                setButtonDisabled(mStopButton);
                setButtonEnabled(mPlayButton);
                setButtonDisabled(mPauseButton);
                recordingStateReference.set(RecordingState.Recorded);

                if (debug == true) {
                    Log.i(TAG, "State is " + event.currentState.toString());
                    displayToast(event.currentState);
                }
                break;

            case Playing:
                Log.i(TAG, "State is PLAYING");
                setButtonDisabled(mRecordButton);
                setButtonEnabled(mStopButton);
                setButtonDisabled(mPlayButton);
                setButtonEnabled(mPauseButton);

                recordingStateReference.set(RecordingState.Playing);

                if (debug == true) {
                    Log.i(TAG, "State is " + event.currentState.toString());
                    displayToast(event.currentState);
                }
                break;

            case Paused:
                Log.i(TAG, "State is PAUSED");
                setButtonDisabled(mRecordButton);
                setButtonDisabled(mStopButton);
                setButtonEnabled(mPlayButton);
                setButtonDisabled(mPauseButton);

                recordingStateReference.set(RecordingState.Paused);

                if (debug == true) {
                    Log.i(TAG, "State is " + event.currentState.toString());
                    displayToast(event.currentState);
                }
                break;

            default:
                Log.i(TAG, "Default reached");
        }
    }

    void stopPlayback() {
        EventBus.getDefault().post(new LocalEventFromMainActivity.StopPlayback());
    }

    void pause() {
        EventBus.getDefault().post(new LocalEventFromMainActivity.PausePlayback());
    }

    void play() {
        EventBus.getDefault().post(new LocalEventFromMainActivity.StartPlayback());
        Log.d(TAG, "StartPlayback() message sent to EventBus");
    }

    void reset() {
        EventBus.getDefault().post(new LocalEventFromMainActivity.ResetPlayback());
        Log.d(TAG, "ResetPlayback() message sent to EventBus");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.submit_coin_dialog, container, false);

        // Initialize playback controller

        this.timestamp = getTimestamp();

        toolbar = view.findViewById(R.id.toolbar);
        if (toolbar == null) {
            Log.e(TAG, "Toolbar not found");
            // handle error here
        }

        return view;
    }

    private void uploadFiles(File coinRecording, File coinPicture, String timestamp) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Check if recording and picture exist
        if (coinRecording == null || coinPicture == null || !coinRecording.exists() || !coinPicture.exists()) {
            Log.d(TAG, "Coin recording / picture might not exist.");
            showErrorDialog("Tried to upload but coin recording / picture doesn't exist.");
            return;
        }

        uploadRecording(coinRecording, timestamp);
        uploadPicture(coinPicture, timestamp);

        // Check if user exists
        if (currentUser == null) {
            Log.e(TAG, "Current user is null. UID could not be retrieved.");
            showErrorDialog("Tried to upload but couldn't resolve user.");
            return;
        }

        mDatabase.child("submitted_coins").child(timestamp).child("uid").setValue(currentUser.getUid());

        // Check if email field is populated and update if it is
        if (mEmailField != null && mEmailField.getText() != null && !mEmailField.getText().toString().isEmpty()) {
            this.userEmailAddress = mEmailField.getText().toString();
            mDatabase.child("submitted_coins").child(timestamp).child("email").setValue(userEmailAddress);
        }

        this.userEmailAddress = mEmailField.getText().toString();
        mDatabase.child("submitted_coins").child(timestamp).child("email").setValue(userEmailAddress);
    }


    private void presentCompleteDialog() {

        new AlertDialog.Builder(getContext())
                .setTitle("Thank you")
                .setMessage("Many thanks for your recording. If you supplied your email address you will receive an email once the coin has been added.")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, (dialog, which) -> dismiss())

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setIcon(R.drawable.ic_cloud_done)
                .show();
    }

    private void uploadRecording(File recording, String timestamp) {
        Uri fileURI = Uri.fromFile(recording);
        StorageReference coinRecordingRef = storageRef.child(FIREBASE_RECORDING_DIR + "/" + timestamp + "/" + fileURI.getLastPathSegment());
        StorageMetadata metadata = new StorageMetadata.Builder().setContentType("audio/wav").setCustomMetadata("coinName", "e.g. Krugerrand").build();

        UploadTask uploadTask = coinRecordingRef.putFile(fileURI);
        Handler handler = new Handler();

        // This Runnable will be executed if upload takes more than 10 seconds
        Runnable runnable = () -> {
            uploadTask.cancel();
            Toast.makeText(getContext(), "Upload timed out. Please try again.", Toast.LENGTH_SHORT).show();
        };

        // Start the 10 seconds timer
        handler.postDelayed(runnable, 10000);

        uploadTask.addOnFailureListener(exception -> {
            // Remove the Runnable from the Handler queue as it's no longer needed
            handler.removeCallbacks(runnable);

            // Log the exception and inform the user
            Log.e("Upload", "Failure!", exception);
            Toast.makeText(getContext(), "Upload failed. Please try again.", Toast.LENGTH_SHORT).show();
        }).addOnSuccessListener(taskSnapshot -> {
            // Remove the Runnable from the Handler queue as it's no longer needed
            handler.removeCallbacks(runnable);

            Log.i("Upload", "Coin recording upload success!");
            coinRecordingRef.updateMetadata(metadata);
            coinRecordingRef.getDownloadUrl().addOnSuccessListener(url -> {
                mDatabase.child("submitted_coins").child(timestamp).child("recording").setValue(url.toString());
                Toast.makeText(getContext(), "Recording uploaded successfully!", Toast.LENGTH_SHORT).show();
            });

            coinRecordingUploaded = true;
            updateUploadProgressBar();
        });
    }



    private void updateUploadProgressBar() {
        if (this.coinRecordingUploaded && this.coinPictureUploaded) {
            mProgressbar.setVisibility(View.GONE);
            presentCompleteDialog();
        }
    }

    private void uploadPicture(File picture, String timestamp) {
        Uri fileURI = Uri.fromFile(picture);
        StorageReference coinPictureRef = storageRef.child(FIREBASE_RECORDING_DIR + "/" + timestamp + "/" + fileURI.getLastPathSegment());
        StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpg").setCustomMetadata("coinName", "e.g. Krugerrand").build();

        UploadTask uploadTask = coinPictureRef.putFile(fileURI);
        Handler handler = new Handler();

        // This Runnable will be executed if upload takes more than 10 seconds
        Runnable runnable = () -> {
            uploadTask.cancel();
            showErrorDialog("Upload timed out. Please try again.");
        };

        // Start the 10 seconds timer
        handler.postDelayed(runnable, 10000);

        uploadTask.addOnFailureListener(exception -> {
            // Remove the Runnable from the Handler queue as it's no longer needed
            handler.removeCallbacks(runnable);

            Log.e("Upload", "Failure!", exception);  // Log exception message
            showErrorDialog("Upload failed. Please try again.");
        }).addOnSuccessListener(taskSnapshot -> {
            // Remove the Runnable from the Handler queue as it's no longer needed
            handler.removeCallbacks(runnable);

            Log.i("Upload", "Coin picture upload success!");
            coinPictureRef.updateMetadata(metadata);
            coinPictureRef.getDownloadUrl().addOnSuccessListener(url -> {
                mDatabase.child("submitted_coins").child(timestamp).child("picture").setValue(url.toString());
                coinPictureUploaded = true;
                updateUploadProgressBar();
            }).addOnFailureListener(exception -> {
                showErrorDialog("Error: Tried to upload but failed to get the download URL.");
                Log.e("Upload", "Failed to get the download URL", exception);  // Log exception message
            });
        });
    }



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.setTitle(DIALOG_TITLE);

        bindViews();

        setOnClickListeners();

        setupSeekbar();

        setButtonDisabled(mSubmitButton);
        setButtonDisabled(mStopButton);
        setButtonDisabled(mPlayButton);
        setButtonDisabled(mPauseButton);

        mProgressbar.setVisibility(View.GONE);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setOnClickListeners() {

        recordingStateReference.set(RecordingState.Unrecorded);

        mRecordButton.setOnClickListener(v -> {
            if (recordingStateReference.get() == RecordingState.Unrecorded || recordingStateReference.get() == RecordingState.Recorded) {
                record();
            }
        });

        mStopButton.setOnClickListener(v -> {
            if (recordingStateReference.get() == RecordingState.Recording) {
                stopRecord();
            } else if (recordingStateReference.get() == RecordingState.Playing) {
                stopPlayback();
            } else if (recordingStateReference.get() == RecordingState.Paused) {
                stopPlayback();
            }
        });

        mPlayButton.setOnClickListener(v -> {
            if (recordingStateReference.get() == RecordingState.Recorded || recordingStateReference.get() == RecordingState.Paused) {
                play();
            }
        });

        mPauseButton.setOnClickListener(v -> {
            if (recordingStateReference.get() == RecordingState.Playing) {
                pause();
            }
        });


        mSubmitButton.setOnClickListener((v) -> {
            mLayoutContainer.setVisibility(View.GONE);
            mProgressbarLinearLayout.setVisibility(View.VISIBLE);
            mProgressbar.setVisibility(View.VISIBLE);
            uploadFiles(this.coinRecording, this.coinPicture, getTimestamp());
        });


        mTakePictureButton.setOnClickListener(v -> dispatchTakePictureIntent());
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setWindowAnimations(R.style.AppTheme_Slide);
        }


    }

    @Override
    public void onStop() {
        super.onStop();
        if (getActivity().isChangingConfigurations() && mMediaPlayerHolder.isPlaying()) {
            Log.d(TAG, "onStop: don't release MediaPlayer as screen is rotating & playing");
        } else {

            // Release if it exists
            if (null != mMediaPlayerHolder) {
                mMediaPlayerHolder.release();
                Log.d(TAG, "onStop: release MediaPlayer");
            }

        }
    }

    // TODO: Come up with better CONST values instead of 321
    @AfterPermissionGranted(REQUEST_RECORD_AUDIO_PERMISSION)
    private void record() {
        String[] perms = {Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(getContext(), perms)) {
            try {
                dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(RECORDER_SAMPLERATE, bufferSize, 0);
            } catch (Exception e) {
                showErrorDialog("Couldn't start recording. Please try again later.");
                Log.e(TAG, "Failed to initialize audio dispatcher.", e);
                return;
            }

            EventBus.getDefault().post(new MediaStateChangeEvent.StateChanged(RecordingState.Recording));

            if (mMediaPlayerHolder != null) {
                mMediaPlayerHolder.release();
                Log.i(TAG, "mPlayerAdapter released.");
            }

            File pathName = getActivity().getFilesDir();
            if (!pathName.exists() && !pathName.mkdirs()) {
                showErrorDialog("Couldn't start recording due to a storage issue. Please try again later.");
                Log.d(TAG, "Directory creation failed");
                return;
            }

            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                String timeStamp = formatter.format(new Date());
                this.coinRecording = new File(pathName, timeStamp + ".wav");
                this.coinRecording.createNewFile();
            } catch (IOException ex) {
                showErrorDialog("Couldn't start recording due to a storage issue. Please try again later.");
                Log.e(TAG, "Error occurred while creating the File", ex);
                return;
            }

            try {
                outputFile = new RandomAccessFile(this.coinRecording, "rw");
                Log.i(TAG, "New random access file created: " + this.coinRecording.getAbsolutePath());
                TarsosDSPAudioFormat outputFormat = new TarsosDSPAudioFormat(RECORDER_SAMPLERATE, 16, 1, true, false);
                WriterProcessor writer = new WriterProcessor(outputFormat, outputFile);
                dispatcher.addAudioProcessor(writer);

                recordingThread = new Thread(() -> {
                    try {
                        dispatcher.run();
                    } catch (Exception e) {
                        showErrorDialog("An error occurred during recording. Please try again.");
                        Log.e(TAG, "Error occurred during recording", e);
                    }
                }, "AudioDispatcherThread");
                recordingThread.start();
                Log.i(TAG, "Recording thread has started");
                Toast.makeText(getContext(), "Recording has started!", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                showErrorDialog("Couldn't start recording due to a storage issue. Please try again later.");
                Log.e(TAG, "Error occurred while accessing the file for recording.", e);
            } catch (IOException e) {
                showErrorDialog("An error occurred. Please try again.");
                Log.e(TAG, "Error occurred.", e);
            }
        } else {
            EasyPermissions.requestPermissions(this, "We need access to your phone's microphone to record.", REQUEST_RECORD_AUDIO_PERMISSION, perms);
        }
    }


    private void showErrorDialog(String errorMessage) {
        new AlertDialog.Builder(getContext()).setTitle("Error").setMessage(errorMessage).setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss()).show();
    }


    private void stopRecord() {
        EventBus.getDefault().post(new MediaStateChangeEvent.StateChanged(RecordingState.Recorded));

        dispatcher.stop();
        recordingThread = null;

        // Close the RandomAccessFile
        if (outputFile != null) {
            try {
                outputFile.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing RandomAccessFile", e);
            }
        }

        if (this.coinRecording == null || this.coinRecording.getAbsolutePath() == null) {
            Log.d(TAG, "The coinRecording is null or the path is invalid");
            showErrorDialog("The coin recording could not be found.");
            return;
        }

        try {
            mMediaPlayerHolder = new MediaPlayerHolder(getContext());
            mMediaPlayerHolder.loadSavedFile(this.coinRecording.getAbsolutePath());
        } catch (Exception e) {
            Log.d(TAG, "Error occurred while loading media player with the recorded file", e);
            showErrorDialog("Error occurred while loading media player with the recorded file");
            // Show error to the user or handle it appropriately
            return;
        }

        Log.d(TAG, "Successfully stopped recording and loaded media player");
    }


    private File getTempFile(Context context) {
        File file = null;
        try {
            Date date = new Date();
            String fileName = new Timestamp(date.getTime()).toString();
            file = File.createTempFile(fileName, null, context.getCacheDir());
        } catch (IOException e) {
            // Error while creating file
        }
        return file;
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        // Do nothing
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        } else {
            requestCameraAndStoragePermission();
        }
    }

    public class PlaybackListener extends PlaybackInfoListener {

        @Override
        public void onDurationChanged(int duration) {
//            mSeekbarAudio.setMax(duration);
//            Log.d(TAG, String.format("setPlaybackDuration: setMax(%d)", duration));
        }

        @Override
        public void onPositionChanged(int position) {
//            if (!mUserIsSeeking) {
//                mSeekbarAudio.setProgress(position, true);
//                Log.d(TAG, String.format("setPlaybackPosition: setProgress(%d)", position));
//            }
        }

        @Override
        public void onStateChanged(@State int state) {
            String stateToString = PlaybackInfoListener.convertStateToString(state);
            onLogUpdated(String.format("onStateChanged(%s)", stateToString));
        }

        @Override
        public void onPlaybackCompleted() {
        }


    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start recording
                record();
            } else {
                // Permission denied, show an error or something
                showErrorDialog("Permission to access microphone was denied");
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, take picture
                dispatchTakePictureIntent();
            } else {
                // Permission denied, show an error or something
                showErrorDialog("Permission to access storage and camera was denied");
            }
        } else {
            // Forward results to EasyPermissions for other permission requests
            EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        }
    }


    @AfterPermissionGranted(REQUEST_CAMERA_PERMISSION)
    private void dispatchTakePictureIntent() {

        String[] perms = {Manifest.permission.CAMERA};
        if (!EasyPermissions.hasPermissions(getContext(), perms)) {
            EasyPermissions.requestPermissions(this, "We need permission to use your camera so you can take a picture of your coin.", REQUEST_CAMERA_PERMISSION, perms);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) == null) {
            showErrorDialog("No camera application found. Please install a camera application and try again.");
            return;
        }

        File pathName = getActivity().getFilesDir();
        if (!pathName.exists() && !pathName.mkdirs()) {
            showErrorDialog("Couldn't start due to a storage issue. Please try again later.");
            Log.d(TAG, "Directory creation failed");
            return;
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
            String timeStamp = formatter.format(new Date());
            this.coinPicture = new File(pathName, timeStamp + ".jpg");
            this.coinPicture.createNewFile();
        } catch (IOException ex) {
            showErrorDialog("Couldn't start due to a storage issue. Please try again later.");
            Log.e(TAG, "Error occurred while creating the File", ex);
            return;
        }

        // Continue only if the File was successfully created
        if (coinPicture == null) {
            showErrorDialog("Failed to create a new file for the photo. Please try again.");
            return;
        }

        Uri photoURI = FileProvider.getUriForFile(getContext(), "com.pingcoin.android.pingcoin.fileprovider", coinPicture);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
    }


    private void requestCameraAndStoragePermission() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.somePermissionPermanentlyDenied(this, Arrays.asList(perms))) {
            new AppSettingsDialog.Builder(this).build().show();
        } else {
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(this, REQUEST_CAMERA_PERMISSION, perms)
                            .setRationale("We need permission to write to your storage to store a temporary picture you take of your coin before it can be sent to our servers.")
                            .build());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_TAKE_PHOTO || resultCode != Activity.RESULT_OK) {
            return;
        }

        if (this.coinPicture == null || !this.coinPicture.exists()) {
            showErrorDialog("An error occurred while capturing the picture. Please try again.");
            return;
        }

        try {
            mImageBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), Uri.fromFile(this.coinPicture));

            if (mImageBitmap == null) {
                showErrorDialog("An error occurred while trying to load the picture. Please try again.");
                return;
            }

            coinThumbnail.setImageBitmap(mImageBitmap);
            pictureStateReference.set(PictureState.Taken);
            updateSubmitButtonStatus();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("An error occurred while trying to load the picture. Please try again.");
        }
    }


    // Function to enable the submit button if both recording and picture are present
    private void updateSubmitButtonStatus() {
        Log.d(TAG, "Current picture state: " + pictureStateReference.get());
        Log.d(TAG, "Current recording state: " + recordingStateReference.get());
        boolean isReadyForSubmission = recordingStateReference.get() == RecordingState.Recorded
                && pictureStateReference.get() == PictureState.Taken;

        if (isReadyForSubmission) {
            // Set submit button to active
            setButtonEnabled(mSubmitButton);
            Log.d(TAG, "Submit button enabled");
        } else {
            // Set submit button inactive
            setButtonDisabled(mSubmitButton);
            Log.d(TAG, "Submit button disabled");
        }
    }


    private String getTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


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
                // Intentionally left empty
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                EventBus.getDefault().post(new LocalEventFromMainActivity.SeekTo(userSelectedPosition));
            }
        });
    }

    // Event subscribers.
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocalEventFromMediaPlayerHolder.UpdateLog event) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocalEventFromMediaPlayerHolder.PlaybackDuration event) {
        mSeekbarAudio.setMax(event.duration);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocalEventFromMediaPlayerHolder.PlaybackPosition event) {
        if (!isUserSeeking) {
            mSeekbarAudio.setProgress(event.position, true);
        }
    }


}
