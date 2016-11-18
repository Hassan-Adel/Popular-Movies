package com.example.android.popularmovies;

/**
 * Created by hasan on 8/20/2016.
 */
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {
    
    private final String LOG_TAG = ImageAdapter.class.getSimpleName();
    Context context;
    ArrayList<String> paths = new ArrayList<>();
    
    public ImageAdapter(Context context) {
        this.context = context;
    }

    public void setPaths(ArrayList<String> paths) {
        this.paths = paths;
    }

    @Override
    public int getCount() {
        return paths.size();
    }

    @Override
    public String getItem(int position) {
        return paths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.image_view, parent, false);
            imageView = (ImageView) convertView.findViewById(R.id.imagee);
            convertView.setTag(imageView);
        } else {
            imageView = (ImageView) convertView.getTag();
        }

        //Log.i(LOG_TAG, "---------------GET VIEW, position: " + position);
        //Log.i(LOG_TAG, "---------------GET VIEW, url: " + paths.get(position));

        Picasso.with(context).setLoggingEnabled(true);
        Picasso.with(context)
                .load(paths.get(position))
                .into(imageView);

        return convertView;
    }
}
