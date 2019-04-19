package com.pingcoin.android.pingcoin;

import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class OverflowMenuActivity extends AppCompatActivity {
    private int menuResource;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        menuResource = R.menu.toolbar_menu;
        getMenuInflater().inflate(menuResource, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_feedback: {
                // do your feedback stuff
                break;
            }

            case R.id.action_about_ping_test: {
                // do your ping test explanation
                break;
            }
            // case blocks for other MenuItems (if any)
        }
        return false;
    }
}
