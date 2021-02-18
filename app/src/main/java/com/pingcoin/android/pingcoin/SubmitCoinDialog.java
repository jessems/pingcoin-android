package com.pingcoin.android.pingcoin;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.text.AlphabeticIndex;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.livinglifetechway.quickpermissions.annotations.WithPermissions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


public class SubmitCoinDialog extends DialogFragment implements EasyPermissions.PermissionCallbacks {

    public static final String TAG = "example dialog";
    private static final String APP_RECORDING_DIRECTORY = "Pingcoin";
    private static final String FIREBASE_RECORDING_DIR = "recordings";
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final String DIALOG_TITLE = "Submit a coin";
    private static final int RECORD_AUDIO = 123;
    private static final int DISPATCH_TAKE_PICTURE = 321;

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


    private Button mTakePictureButton;

    private MaterialButton mRecordButton;
    private MaterialButton mStopButton;
    private MaterialButton mPlayButton;
    private MaterialButton mPauseButton;

    private SeekBar mSeekbarAudio;


    private final AtomicReference<RecordingState> recordingStateReference = new AtomicReference<>();
    private final AtomicReference<PictureState> pictureStateReference = new AtomicReference<>();

    String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE,
            Manifest.permission.RECORD_AUDIO,
    };
    private DatabaseReference mDatabase;


    StorageReference storageRef;
    FirebaseStorage storage;

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
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING) * 4;



        // Prevent Fragment from getting recreated on rotation (which would screw up the playback)
        // (For an activity this needs to be done in the manifest file)
        setRetainInstance(true);

        this.coinRecording = null;
        this.coinPicture = null;

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
        Toast.makeText(getContext(), String.format("State changed to:%s", state),
                Toast.LENGTH_SHORT).show();
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



        switch(event.currentState) {

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
        Log.i(TAG, "StartPlayback() message sent to EventBus");
    }

    void reset() {
        EventBus.getDefault().post(new LocalEventFromMainActivity.ResetPlayback());
        Log.i(TAG, "ResetPlayback() message sent to EventBus");
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.submit_coin_dialog, container, false);

        // Initialize playback controller

        this.timestamp = getTimestamp();
        pathName = new File(Environment.getExternalStorageDirectory() + "/" + APP_RECORDING_DIRECTORY);
        coinRecording = new File(pathName, "test.wav");

        try {
            this.coinRecording = createFile("recording");
            Log.i(TAG, "Reached here???");
        } catch (IOException e) {
            Log.i(TAG, "uh oh!");
            e.printStackTrace();
        }


        toolbar = view.findViewById(R.id.toolbar);


        // TODO: Add check if device has a camera or not with hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        return view;
    }


    private void uploadFiles(File coinRecording, File coinPicture, String timestamp) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        if (null != coinRecording && null != coinPicture && coinRecording.exists() && coinPicture.exists()) {
            uploadRecording(coinRecording, timestamp);
            uploadPicture(coinPicture, timestamp);
            mDatabase.child("submitted_coins").child(timestamp).child("uid").setValue(mAuth.getCurrentUser().getUid());
        } else {
            Log.i(TAG, "Coin recording / picture might not exist.");
        }
        this.userEmailAddress = mEmailField.getText().toString();
        Log.i(TAG, userEmailAddress);
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
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.ic_cloud_done)
                .show();
    }

    private void uploadRecording(File recording, String timestamp) {

        Uri fileURI = Uri.fromFile(recording);
        StorageReference coinRecordingRef = storageRef.child(FIREBASE_RECORDING_DIR + "/" + timestamp + "/" + fileURI.getLastPathSegment());
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("audio/wav")
                .setCustomMetadata("coinName", "e.g. Krugerrand")
                .build();

        UploadTask uploadTask = coinRecordingRef.putFile(fileURI);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(exception -> Log.i("Upload", "Failure!")).addOnSuccessListener(taskSnapshot -> {
            Log.i("Upload", "Coin recording upload success!");
            coinRecordingRef.updateMetadata(metadata);
            coinRecordingRef.getDownloadUrl().addOnSuccessListener(url -> mDatabase.child("submitted_coins").child(timestamp).child("recording").setValue(url.toString()));

            coinRecordingUploaded = true;
            updateUploadProgressBar();

        });
    }

    private void updateUploadProgressBar() {

        if (this.coinRecordingUploaded && this.coinPictureUploaded) {
            mProgressbar.setVisibility(View.GONE);
            presentCompleteDialog();
        } else {
            // do nothing
        }
    }

    private void uploadPicture(File picture, String timestamp) {
        Log.i(TAG, "Reached here?");
        Uri fileURI = Uri.fromFile(picture);
        StorageReference coinPictureRef = storageRef.child(FIREBASE_RECORDING_DIR + "/" + timestamp + "/" + fileURI.getLastPathSegment());
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .setCustomMetadata("coinName", "e.g. Krugerrand")
                .build();

        UploadTask uploadTask = coinPictureRef.putFile(fileURI);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(exception -> Log.i("Upload", "Failure!")).addOnSuccessListener(taskSnapshot -> {
            Log.i("Upload", "Coin picture upload success!");
            coinPictureRef.updateMetadata(metadata);
            coinPictureRef.getDownloadUrl().addOnSuccessListener(url -> mDatabase.child("submitted_coins").child(timestamp).child("picture").setValue(url.toString()));
            coinPictureUploaded = true;
            updateUploadProgressBar();

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

        mRecordButton.setOnClickListener( v -> {
            if (recordingStateReference.get() == RecordingState.Unrecorded || recordingStateReference.get() == RecordingState.Recorded) {
                record();
            } else {
                // do nothing
            }
        });

        mStopButton.setOnClickListener( v -> {
            if (recordingStateReference.get() == RecordingState.Recording) {
                stopRecord();
            } else if (recordingStateReference.get() == RecordingState.Playing) {
                stopPlayback();
            } else if (recordingStateReference.get() == RecordingState.Paused) {
                stopPlayback();
            } else {
                // do nothing
            }
        });

        mPlayButton.setOnClickListener( v -> {
            if (recordingStateReference.get() == RecordingState.Recorded || recordingStateReference.get() == RecordingState.Paused) {
                play();
            } else {
                // do nothing
            }
        });

        mPauseButton.setOnClickListener( v -> {
            if (recordingStateReference.get() == RecordingState.Playing) {
                pause();
            } else {
                // do nothing
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

    // this will not run because this is a fragment not an activity
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
    @AfterPermissionGranted(RECORD_AUDIO)
    private void record() {
        String[] perms = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(getContext(), perms)) {
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(RECORDER_SAMPLERATE, bufferSize, 0);
            // Output

            EventBus.getDefault()
                    .post(new MediaStateChangeEvent.StateChanged(RecordingState.Recording));

            if (null != mMediaPlayerHolder) {
                mMediaPlayerHolder.release();
                Log.i(TAG, "mPlayerAdapter released.");
            }

            if (null != this.coinRecording && this.coinRecording.exists()) {
                Log.i(TAG, this.coinRecording.getAbsolutePath() + " exists: " + this.coinRecording.exists());
                this.coinRecording.delete();
                Log.i(TAG, "File deleted!");
                Log.i(TAG, this.coinRecording.getAbsolutePath() + " exists: " + this.coinRecording.exists());
            } else {
                Log.i(TAG, this.coinRecording.getAbsolutePath() + "doesn't exist!");
            }

            try {
                if (pathName.mkdirs()) {
                    System.out.println("Directory Created");
                } else {
                    if (pathName.exists()) {
                        Log.d(TAG, "Directory exists");
                    } else {
                        Log.d(TAG, "Directory does not exist");
                    }
                    if (pathName.isDirectory()) {
                        Log.d(TAG, "Directory is a directory");
                    } else {
                        Log.d(TAG, "Directory is not a directory");
                    }
                    System.out.println("Directory is not created");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Rescan available files
            MediaScannerConnection.scanFile(getActivity(), new String[]{Environment.getExternalStorageDirectory().toString()}, null, null);

            if (isExternalStorageWritable()) {
                // do this
            }

            try {
                RandomAccessFile outputFile = new RandomAccessFile(this.coinRecording, "rw");
                Log.i(TAG, "New random access file created: " + this.coinRecording.getAbsolutePath());
                TarsosDSPAudioFormat outputFormat = new TarsosDSPAudioFormat(RECORDER_SAMPLERATE, 16, 1, true, false);
                WriterProcessor writer = new WriterProcessor(outputFormat, outputFile);
                dispatcher.addAudioProcessor(writer);
                recordingThread = new Thread(() -> dispatcher.run(), "MyThread");
                recordingThread.start();

                Log.i(TAG, "Recording thread has started");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } else {
            EasyPermissions.requestPermissions(this, "We need access to your phone's microphone to record and submit a coin recording.",
                    RECORD_AUDIO, perms);
        }

    }



    private void stopRecord() {
        EventBus.getDefault()
                .post(new MediaStateChangeEvent.StateChanged(RecordingState.Recorded));
        Log.i("FILES", "The dispatcher: " + dispatcher.toString() + " dispatcher.isStopped(): " + dispatcher.isStopped());
        if (null != dispatcher && !dispatcher.isStopped()) {
            dispatcher.stop();
            recordingThread = null;
            mMediaPlayerHolder = new MediaPlayerHolder(getContext());
            mMediaPlayerHolder.loadSavedFile(this.coinRecording.getAbsolutePath());

        } else {
            Log.i("FILE", "Watch out dispatcher wasn't closed");
        }
        Log.i("FILES:", "List of files here: " + Arrays.toString(getContext().fileList()));

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
        if (requestCode == RECORD_AUDIO && perms.contains(Manifest.permission.RECORD_AUDIO) && perms.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            record();
        } else if (requestCode == DISPATCH_TAKE_PICTURE && perms.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            dispatchTakePictureIntent();
        } else {
            //
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
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
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

//    private boolean checkPermissions() {
//        int result;
//        List<String> listPermissionsNeeded = new ArrayList<>();
//        for (String p : permissions) {
//            result = ContextCompat.checkSelfPermission(getActivity(), p);
//            if (result != PackageManager.PERMISSION_GRANTED) {
//                listPermissionsNeeded.add(p);
//            }
//        }
//        if (!listPermissionsNeeded.isEmpty()) {
//            ActivityCompat.requestPermissions(getActivity(), listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
//            return false;
//        }
//        return true;
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        if (requestCode == 100) {
//            if (grantResults.length > 0
//                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // do something
//            }
//            return;
//        }
//    }


    @AfterPermissionGranted(DISPATCH_TAKE_PICTURE)
    private void dispatchTakePictureIntent() {

        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(getContext(), perms)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createFile("picture");
                    this.coinPicture = photoFile;
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {

                    Uri photoURI = FileProvider.getUriForFile(getContext(),
                            "com.pingcoin.android.pingcoin.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        } else {
            EasyPermissions.requestPermissions(this, "We need permission to write to your storage to store a temporary picture you take of your coin before it can be sent to our servers.",
                    DISPATCH_TAKE_PICTURE, perms);
        }


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO: {
                    if (resultCode == Activity.RESULT_OK) {
                        this.coinPicture = new File(currentPhotoPath);

                        mImageBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), Uri.fromFile(this.coinPicture));


                        if (mImageBitmap != null) {
                            coinThumbnail.setImageBitmap(mImageBitmap);
                        }

                        pictureStateReference.set(PictureState.Taken);
                        updateSubmitButtonStatus();
                    }
                    break;
                }
            }
        } catch (Exception error) {
            error.printStackTrace();
        }

    }

    // Function to enable the submit button if both recording and picture are present
    private void updateSubmitButtonStatus() {
        if (recordingStateReference.get() == RecordingState.Recorded && pictureStateReference.get() == PictureState.Taken) {
            // Set submit button to active
            setButtonEnabled(mSubmitButton);
        } else {
            // Set submit button inactive
            setButtonDisabled(mSubmitButton);
        }
    }

    private String getTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    private File createFile(String fileType) throws IOException {
        String timestamp = getTimestamp();
        String extensionPrefix = null;
        String suffix = null;
        File storageDir = null;
        File file = null;
        switch (fileType) {
            case "recording":
                extensionPrefix = "WAV";
                suffix = ".wav";
                storageDir = new File(Environment.getExternalStorageDirectory() + "/" + APP_RECORDING_DIRECTORY);
                file = concatenateFilenameElements(extensionPrefix, timestamp, suffix, storageDir);
                this.coinRecording = file;
                break;
            case "picture":
                extensionPrefix = "JPG";
                suffix = ".jpg";
                storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                file = concatenateFilenameElements(extensionPrefix, timestamp, suffix, storageDir);
                this.coinPicture = file;
                this.currentPhotoPath = file.getAbsolutePath();
                break;
        }
        return file;
    }

    private File concatenateFilenameElements(String extensionPrefix, String timestamp, String suffix, File storageDir) throws IOException {
        File file = null;
        if (null != extensionPrefix && null != suffix && null != timestamp && null != storageDir) {
            String fileName = extensionPrefix + "_" + timestamp + "_";
            Log.i("concat", "fileName: " + fileName);
            Log.i("concat", "suffix: " + suffix);
            Log.i("concat", "storageDir: " + storageDir);
            file = File.createTempFile(
                    fileName,   /* prefix */
                    suffix,     /* suffix */
                    storageDir  /* directory */
            );
        }
        return file;
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
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                EventBus.getDefault().post(new LocalEventFromMainActivity.SeekTo(
                        userSelectedPosition));
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

    // TODO: account for this somehow
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocalEventFromMediaPlayerHolder.PlaybackPosition event) {
        if (!isUserSeeking) {
            mSeekbarAudio.setProgress(event.position, true);
        }
    }



}
