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
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class SubmitCoinDialog extends DialogFragment {

    public static final String TAG = "example dialog";
    private static final String APP_RECORDING_DIRECTORY = "Pingcoin";
    private static final String FIREBASE_RECORDING_DIR = "recordings";
    private static final int REQUEST_TAKE_PHOTO = 1;
    public boolean isRecording = false;
    private Bitmap mImageBitmap;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Toolbar toolbar;
    AudioDispatcher dispatcher;
    private Thread recordingThread;
    String currentPhotoPath;
    private int bufferSize;
    private File coinRecording;
    private File coinPicture;
    private PlayerAdapter mPlayerAdapter;
    private HashMap<String, File> filesToUpload;
    private ImageView coinThumbnail;
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE,
            Manifest.permission.RECORD_AUDIO,
    };



    StorageReference storageRef;
    FirebaseStorage storage;

    File pathName;
//    MediaPlayer mp;


    public static SubmitCoinDialog display(FragmentManager fragmentManager) {
        SubmitCoinDialog submitCoinDialog = new SubmitCoinDialog();
        submitCoinDialog.show(fragmentManager, TAG);
        return submitCoinDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);
//        MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.pingtest);
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING)*4;
//        File file = new File(Environment.getExternalStoragePublicDirectory("Pingcoin"), "test.wav");
//        boolean deleted = file.delete();
        checkPermissions();

        // Prevent Fragment from getting recreated on rotation (which would screw up the playback)
        // (For an activity this needs to be done in the manifest file)
        setRetainInstance(true);

        this.coinRecording = null;
        this.coinPicture = null;


        pathName = new File(Environment.getExternalStorageDirectory() + "/" + APP_RECORDING_DIRECTORY);
        coinRecording = new File(pathName, "test.wav");


        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.submit_coin_dialog, container, false);

// Initialize playback controller




        Button recordButton = view.findViewById(R.id.record_button);
        recordButton.setOnClickListener(v -> {
            if (getIsRecording()) {
                // If recording, stop it
                onStopRecord();
                toggleIsRecording();
                recordButton.setText("Record");
                Log.i("BLA", "Stopped recording");
            } else {
                // If not recording, start
                onRecord();
                toggleIsRecording();
                recordButton.setText("Stop");
                Log.i("BLA", "Recording!!");
            }
        });


        Button mPauseButton = view.findViewById(R.id.pause_button);
        Button mPlayButton = view.findViewById(R.id.play_button);
        Button mResetButton = view.findViewById(R.id.reset_button);
        Button mUploadButton = view.findViewById(R.id.upload_button);
        Button mTakePictureButton = view.findViewById(R.id.take_picture_button);
        coinThumbnail = view.findViewById(R.id.coin_thumbnail);

        mPauseButton.setOnClickListener(
                view13 -> mPlayerAdapter.pause());

        mPlayButton.setOnClickListener(
                view1 -> {
                    File file = new File(Environment.getExternalStorageDirectory() + "/" + "Pingcoin" + "/" + "test.wav");
                    mPlayerAdapter.loadSavedFile(Environment.getExternalStorageDirectory() + "/" + "Pingcoin" + "/" + "test.wav");
                    mPlayerAdapter.play();
                });
        mResetButton.setOnClickListener(
                view12 -> mPlayerAdapter.reset());


        // Check if recording exists
        // Files may not exist here

        mUploadButton.setOnClickListener((v) -> {
                    uploadFiles(this.coinRecording, this.coinPicture);
        });


        mTakePictureButton.setOnClickListener(v -> dispatchTakePictureIntent());


        MediaPlayerHolder mMediaPlayerHolder = new MediaPlayerHolder(getContext());
        Log.d(TAG, "initializePlaybackController: created MediaPlayerHolder");
        mMediaPlayerHolder.setPlaybackInfoListener(new PlaybackListener());
        mPlayerAdapter = mMediaPlayerHolder;
        Log.d(TAG, "initializePlaybackController: MediaPlayerHolder progress callback set");
        toolbar = view.findViewById(R.id.toolbar);


        // TODO: Add check if device has a camera or not with hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        return view;
    }

    private void uploadFiles(File coinRecording, File coinPicture) {
        if (null != coinRecording && null != coinPicture && coinRecording.exists() && coinPicture.exists()) {
            uploadRecording(coinRecording);
            uploadPicture(coinPicture);
        } else {
            Log.i(TAG, "Coin recording / picture might not exist.");
        }

    }

    private void uploadRecording(File recording) {

        Uri fileURI = Uri.fromFile(recording);
        StorageReference coinRecordingRef = storageRef.child(FIREBASE_RECORDING_DIR + "/" + fileURI.getLastPathSegment());
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("audio/wav")
                .setCustomMetadata("coinName", "e.g. Krugerrand")
                .build();

        UploadTask uploadTask = coinRecordingRef.putFile(fileURI);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(exception -> Log.i("Upload", "Failure!")).addOnSuccessListener(taskSnapshot -> {
            Log.i("Upload", "Coin recording upload success!");
            coinRecordingRef.updateMetadata(metadata);

        });
    }
    private void uploadPicture(File picture) {
        Log.i(TAG, "Reached here?");
        Uri fileURI = Uri.fromFile(picture);
        StorageReference coinPictureRef = storageRef.child(FIREBASE_RECORDING_DIR + "/" + fileURI.getLastPathSegment());
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .setCustomMetadata("coinName", "e.g. Krugerrand")
                .build();

        UploadTask uploadTask = coinPictureRef.putFile(fileURI);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(exception -> Log.i("Upload", "Failure!")).addOnSuccessListener(taskSnapshot -> {
            Log.i("Upload", "Coin picture upload success!");
            coinPictureRef.updateMetadata(metadata);

        });
    }




    public void toggleIsRecording() {
        this.isRecording = !this.isRecording;
    }

    public boolean getIsRecording() {
        return this.isRecording;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.setTitle("Some title");
        toolbar.inflateMenu(R.menu.submit_coin_dialog);
        toolbar.setOnMenuItemClickListener(item -> {
            dismiss();
            return true;
        });
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

//        mPlayerAdapter.loadMedia(R.raw.pingtest);
//        Log.i("FILE", Environment.getExternalStorageDirectory().getPath() + "/test.wav");

//        Log.i("FILE", getContext().getFilesDir().getAbsolutePath() + "/test.wav");
//        mPlayerAdapter.loadSavedFile(getContext().getFilesDir().getAbsolutePath() + "/test.wav");
//        Log.i(TAG, "onStart: create MediaPlayer");

    }

    // this will not run because this is a fragment not an activity
    @Override
    public void onStop() {
        super.onStop();
        if (getActivity().isChangingConfigurations() && mPlayerAdapter.isPlaying()) {
            Log.d(TAG, "onStop: don't release MediaPlayer as screen is rotating & playing");
        } else {
            mPlayerAdapter.release();
            Log.d(TAG, "onStop: release MediaPlayer");
        }
    }

    public void onRecord() {
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(RECORDER_SAMPLERATE, bufferSize, 0);
        // Output

        if(null != mPlayerAdapter) {
            mPlayerAdapter.release();
        }

        if (coinRecording.exists()) {
            coinRecording.delete();
            Log.i("BLA", "File deleted!");
        }

        try {
            if(pathName.mkdirs()) {
                System.out.println("Directory Created");
            } else {
                if (pathName.exists()) {
                    Log.d("DIRECTORY", "Directory exists");
                } else {
                    Log.d("DIRECTORY", "Directory does not exist");
                }
                if (pathName.isDirectory()) {
                    Log.d("DIRECTORY", "Directory is a directory");
                } else {
                    Log.d("DIRECTORY", "Directory is not a directory");
                }
                System.out.println("Directory is not created");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Rescan available files
        MediaScannerConnection.scanFile(getActivity(), new String[] {Environment.getExternalStorageDirectory().toString()}, null, null);

        if (isExternalStorageWritable()) {
            // do this
        }

        try {
            RandomAccessFile outputFile = new RandomAccessFile(coinRecording, "rw");
            Log.i("FILE:", "New random access file created");
            TarsosDSPAudioFormat outputFormat = new TarsosDSPAudioFormat(RECORDER_SAMPLERATE, 16, 1, true, false);
            WriterProcessor writer = new WriterProcessor(outputFormat, outputFile);
            dispatcher.addAudioProcessor(writer);
            recordingThread = new Thread(() -> dispatcher.run(), "MyThread");
            recordingThread.start();

            Log.i("FILE","Recording thread has started");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    private void onStopRecord() {
        Log.i("FILES", "The dispatcher: " + dispatcher.toString() + " dispatcher.isStopped(): " + dispatcher.isStopped());
        if(null != dispatcher && !dispatcher.isStopped()) {
            dispatcher.stop();
            recordingThread = null;
        } else {
            Log.i("FILE","Watch out dispatcher wasn't closed");
        }
        Log.i("FILES:","List of files here: " + Arrays.toString(getContext().fileList()));

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

        @Override
        public void onLogUpdated(String message) {
//            if (mTextDebug != null) {
//                mTextDebug.append(message);
//                mTextDebug.append("\n");
//                // Moves the scrollContainer focus to the end.
//                mScrollContainer.post(
//                        new Runnable() {
//                            @Override
//                            public void run() {
//                                mScrollContainer.fullScroll(ScrollView.FOCUS_DOWN);
//                            }
//                        });
//            }
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

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(getActivity(), p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(getActivity(), listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do something
            }
            return;
        }
    }



    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createFile("picture");
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
//                Uri photoURI = FileProvider.getUriForFile(getContext(),
//                        BuildConfig.APPLICATION_ID + "." + "provider",
//                        photoFile);

//                Uri photoURI = FileProvider.getUriForFile(getContext(),
//                        Environment.getExternalStorageDirectory() + "/" + "Pingcoin", photoFile);
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.pingcoin.android.pingcoin.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
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
                    }
                    break;
                }
            }
        } catch (Exception error) {
            error.printStackTrace();
        }

    }

    private File createFile(String fileType) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String extensionPrefix = null;
        String suffix = null;
        File storageDir = null;
        File file = null;
        switch (fileType) {
            case "recording":
                extensionPrefix = "JPEG";
                suffix = "jpg";
                storageDir = new File(Environment.getExternalStorageDirectory() + "/" + APP_RECORDING_DIRECTORY + "/" + "test.wav");
                file = concatenateFilenameElements(extensionPrefix, timestamp, suffix, storageDir);
                this.currentPhotoPath = file.getAbsolutePath();
                this.coinPicture = file;
                break;
            case "picture":
                extensionPrefix = "WAV";
                suffix = "wav";
                storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                file = concatenateFilenameElements(extensionPrefix, timestamp, suffix, storageDir);
                this.coinRecording = file;
                break;
        }
        return file;
    }

    private File concatenateFilenameElements(String extensionPrefix, String timestamp, String suffix, File storageDir) throws IOException {
        File file = null;
        if(null != extensionPrefix && null != suffix && null != timestamp && null != storageDir) {
            String fileName = extensionPrefix + "_" + timestamp + "_";
            file = File.createTempFile(
                    fileName,   /* prefix */
                    suffix,     /* suffix */
                    storageDir  /* directory */
            );
        }
        return file;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }




}
