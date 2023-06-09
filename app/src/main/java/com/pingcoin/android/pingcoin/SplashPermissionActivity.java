package com.pingcoin.android.pingcoin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * This {@link Activity} functions as a base class for a splash screen. This
 * class will function differently on pre- and post-Android 6.0 devices,
 * although in both cases, the {@link #getNextActivityClass()} method must be
 * overridden, since {@link #getNextActivityClass()} returns the
 * {@link Activity} to start once the splash screen times out.
 *
 *
 * <p/>
 *
 * On pre-Android 6.0 devices, this {@link Activity} will display a
 * random color for {@link #getTimeoutMillis()} milliseconds, before
 * starting the {@link Activity} specified by {@link #getNextActivityClass()}.
 *
 * <p/>
 *
 * On post-Android 6.0 devices, this app will additionally force the user to
 * grant all of the currently ungranted app permissions before timing out and
 * starting the next {@link Activity} specified by
 * {@link #getNextActivityClass()} (see
 * <a href="http://developer.android.com/training/permissions/requesting.html">
 * Requesting Android Permissions</a>). In pre-Android 6.0 devices, app
 * permissions were granted during installation and could not be revoked.
 * However, since Android 6.0, users can revoke app permissions after
 * installation. This {@link Activity} will gather all of the required app
 * permissions from the manifest, and check that this app has been granted all
 * of those permissions. The user will then be forced to granted all ungranted
 * permissions before continuing. Note, however, that the user may still revoke
 * permissions while the app is running, and this {@link Activity} does nothing
 * to protect your app from such occurrences. Specifically, this
 * {@link Activity} only does a check at start up.
 *
 * <p/>
 *
 * You can change the timeout duration (in milliseconds) and the permissions
 * required by your app by extending this class and overriding
 * {@link #getTimeoutMillis()} and {@link #getRequiredPermissions()} methods.
 */
abstract public class SplashPermissionActivity extends Activity {

    /*
     * ---------------------------------------------
     *
     * Private Fields
     *
     * ---------------------------------------------
     */
    /**
     * The time that the splash screen will be on the screen in milliseconds.
     */
    private int                 timeoutMillis       = 2000;

    /** The time when this {@link Activity} was created. */
    private long                startTimeMillis     = 0;

    /** The code used when requesting permissions */
    private static final int    PERMISSIONS_REQUEST = 1234;

    /** A random number generator for the background colors. */
    private static final Random random              = new Random();

    /**
     * The TextView which is used to inform the user whether the permissions are
     * granted.
     */
    private TextView            textView            = null;
    private static final int    textViewID          = View.generateViewId();

    /*
     * ---------------------------------------------
     *
     * Getters
     *
     * ---------------------------------------------
     */
    /**
     * Get the time (in milliseconds) that the splash screen will be on the
     * screen before starting the {@link Activity} who's class is returned by
     * {@link #getNextActivityClass()}.
     */
    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    /** Get the {@link Activity} to start when the splash screen times out. */
    @SuppressWarnings("rawtypes")
//    public Class getNextActivityClass() {
//        return MainActivity.class;
//    };

    abstract public Class getNextActivityClass();

    private FirebaseAuth mAuth;

    /**
     * Get the list of required permissions by searching the manifest. If you
     * don't think the default behavior is working, then you could try
     * overriding this function to return something like:
     *
     * <pre>
     * <code>
     * return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
     * </code>
     * </pre>
     */
    public String[] getRequiredPermissions() {
        String[] permissions = null;
        try {
            permissions = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_PERMISSIONS).requestedPermissions;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (permissions == null) {
            return new String[0];
        } else {
            return permissions.clone();
        }
    }

    /*
     * ---------------------------------------------
     *
     * Activity Methods
     *
     * ---------------------------------------------
     */
    @TargetApi(23)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /** Default creation code. */
        super.onCreate(savedInstanceState);

        /** Create the layout that will hold the TextView. */
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        /** Add a TextView and set the initial text. */
        textView = new TextView(this);
        textView.setTextSize(50);
        textView.setId(textViewID);
        textView.setText("Waiting for permissions...");
        mainLayout.addView(textView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        /** Set the background color. */
        int off = 128;
        int rest = 256 - off;
        int color = Color.argb(255, off + random.nextInt(rest), off + random.nextInt(rest), off + random.nextInt(rest));
        mainLayout.setBackgroundColor(color);

        /** Set the mainLayout as the content view */
        setContentView(mainLayout);

        /**
         * Save the start time of this Activity, which will be used to determine
         * when the splash screen should timeout.
         */
        startTimeMillis = System.currentTimeMillis();

        /**
         * On a post-Android 6.0 devices, check if the required permissions have
         * been granted.
         */
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions();
        } else {
            startNextActivity();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);

        if(currentUser == null) {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("Splash", "signInAnonymously:success");
                                FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("Splash", "signInAnonymously:failure", task.getException());
                                Toast.makeText(SplashPermissionActivity.this, "Something went wrong.",
                                        Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                            }

                            // ...
                        }
                    });
        }

    }

    /**
     * See if we now have all of the required dangerous permissions. Otherwise,
     * tell the user that they cannot continue without granting the permissions,
     * and then request the permissions again.
     */
    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            checkPermissions();
        }
    }

    /*
     * ---------------------------------------------
     *
     * Other Methods
     *
     * ---------------------------------------------
     */
    /**
     * After the timeout, start the {@link Activity} as specified by
     * {@link #getNextActivityClass()}, and remove the splash screen from the
     * backstack. Also, we can change the message shown to the user to tell them
     * we now have the requisite permissions.
     */
    private void startNextActivity() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                textView.setText("Permissions granted...");
            }
        });
        long delayMillis = getTimeoutMillis() - (System.currentTimeMillis() - startTimeMillis);
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashPermissionActivity.this, getNextActivityClass()));
                finish();
            }
        }, delayMillis);
    }

    /**
     * Check if the required permissions have been granted, and
     * {@link #startNextActivity()} if they have. Otherwise
     * {@link #requestPermissions(String[], int)}.
     */
    private void checkPermissions() {
        String[] ungrantedPermissions = requiredPermissionsStillNeeded();
        if (ungrantedPermissions.length == 0) {
            startNextActivity();
        } else {
            ActivityCompat.requestPermissions(SplashPermissionActivity.this, ungrantedPermissions, PERMISSIONS_REQUEST);
        }
    }

    /**
     * Convert the array of required permissions to a {@link Set} to remove
     * redundant elements. Then remove already granted permissions, and return
     * an array of ungranted permissions.
     */
    @TargetApi(23)
    private String[] requiredPermissionsStillNeeded() {

        Set<String> permissions = new HashSet<String>();
        for (String permission : getRequiredPermissions()) {
            permissions.add(permission);
        }
        for (Iterator<String> i = permissions.iterator(); i.hasNext();) {
            String permission = i.next();
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d(SplashPermissionActivity.class.getSimpleName(),
                        "Permission: " + permission + " already granted.");
                i.remove();
            } else {
                Log.d(SplashPermissionActivity.class.getSimpleName(),
                        "Permission: " + permission + " not yet granted.");
            }
        }
        return permissions.toArray(new String[permissions.size()]);
    }
}