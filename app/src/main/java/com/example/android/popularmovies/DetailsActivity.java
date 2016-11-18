package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by hasan on 8/25/2016.
 */
public class DetailsActivity extends AppCompatActivity{

    public static int moviePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        /*Intent i = getIntent();
        moviePosition = i.getExtras().getInt("position");
        //Log.i("DetailsActivity", "______________Movie Position is: " + moviePosition);*/
    }
}
