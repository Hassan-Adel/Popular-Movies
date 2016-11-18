package com.example.android.popularmovies;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by hasan on 8/17/2016.
 */
public class MainActivity extends AppCompatActivity implements MoviesFragment.Callback  {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.detailsFrag) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detailsFrag, new DetailsFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
        //Log.i(LOG_TAG, "_________mTwoPane: " + mTwoPane);
    }

    @Override
    public void onItemSelected(int moviePos) {
        //Log.i(LOG_TAG, "________moviePos (onItemSelected): " + moviePos);
        if(mTwoPane){
            Bundle bundle = new Bundle();
            bundle.putInt("moviePos", moviePos);
            DetailsFragment detailsFrag = new DetailsFragment();
            detailsFrag.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detailsFrag, detailsFrag, DETAILFRAGMENT_TAG)
                    .commit();
        }
        else{
            Intent i = new Intent(this, DetailsActivity.class);
            i.putExtra("moviePos", moviePos);
            startActivity(i);
        }
    }
}
