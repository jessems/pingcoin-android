package com.pingcoin.android.pingcoin;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

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
                String url = "https://www.speakpipe.com/pingcoin";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
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
        setContentView(R.layout.activity_select_coin);


        Toolbar toolbar = findViewById(R.id.select_coin_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();


        actionBar.setTitle("Select a coin");





//        actionBar.setHomeButtonEnabled(true);




//        dl = (DrawerLayout)findViewById(R.id.drawer_layout);
//        t = new ActionBarDrawerToggle(this, dl, );

//        dl.addDrawerListener(t);
//        t.syncState();





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
