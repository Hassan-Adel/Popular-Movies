package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.support.v4.app.Fragment;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by hasan on 9/4/2016.
 */
public class ReviewsFragment extends Fragment {

    ArrayAdapter <String> reviewsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.fragment_reviews, container, false);
        ArrayList<String> reviews = new ArrayList<>();
        Intent intent = getActivity().getIntent();

        if(intent!=null || intent.hasExtra(Intent.EXTRA_TEXT)){
            reviews.clear();
            reviews = intent.getStringArrayListExtra(Intent.EXTRA_TEXT);
        }
        reviewsAdapter = new ArrayAdapter<>(getActivity(),R.layout.list_reviews ,R.id.textview_reviews, reviews);
        ListView list =(ListView)V.findViewById(R.id.listview_reviews);
        list.setAdapter(reviewsAdapter);

        return V;
    }

}
