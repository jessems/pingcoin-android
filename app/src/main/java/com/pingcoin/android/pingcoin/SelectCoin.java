package com.pingcoin.android.pingcoin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SelectCoin extends OverflowMenuActivity {

    ArrayList<Coin> coins;
    private CoinViewModel mCoinViewModel;

    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_feedback:
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","jessems@gmail.com", null));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Pingcoin app feedback");
                startActivity(Intent.createChooser(intent, "Choose an Email client :"));
                return true;

            case R.id.action_about_ping_test:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            case R.id.action_submit_coin:
                SubmitCoinDialog.display(getSupportFragmentManager());

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        setContentView(R.layout.activity_select_coin);


        Toolbar toolbar = findViewById(R.id.select_coin_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();


        actionBar.setTitle("Select a coin");










        // Lookup the recyclerview in activity layout
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rvCoins);

        // Initialize contacts
//        coins = Coin.createCoinList(20);


        // Create adapter passing in the sample user data
        final CoinListAdapter coinListAdapter = new CoinListAdapter(this);
        // Attach the adapter to the recyclerview to populate items
        recyclerView.setAdapter(coinListAdapter);
        // Set layout manager to position the items
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // That's all!

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        mCoinViewModel = ViewModelProviders.of(this).get(CoinViewModel.class);

        mCoinViewModel.getAllCoins().observe(this, new Observer<List<Coin>>() {
            @Override
            public void onChanged(@Nullable final List<Coin> coins) {
                // Update the cached copy of coins in the adapter
                coinListAdapter.setCoins(coins);
            }
        });


    }

}
