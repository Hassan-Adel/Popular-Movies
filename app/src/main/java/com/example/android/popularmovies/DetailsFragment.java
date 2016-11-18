package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.jar.JarException;
import java.util.logging.Handler;

/**
 * Created by hasan on 8/25/2016.
 */
public class DetailsFragment extends Fragment {

    private final String LOG_TAG = DetailsFragment.class.getSimpleName();

    public int moviePosition;
    public Movie movie;
    LinearLayout reviews;
    LinearLayout trailers;
    Button trailer;
    public static boolean favoriteStateChanged = false;
    String inflaterService = Context.LAYOUT_INFLATER_SERVICE;
    FetchVideosTask fetchVideos;
    FetchReviewsTask fetchReviews;

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        boolean flag = false;

        final Button buttonRefresh = (Button) rootView.findViewById(R.id.button_refresh_details);
        buttonRefresh.setVisibility(View.INVISIBLE);

        if (isOnline()) {

            final CheckBox checkBoxFav = (CheckBox) rootView.findViewById(R.id.checkBoxFav);
            TextView title = (TextView) rootView.findViewById(R.id.originalTitle);
            TextView vote = (TextView) rootView.findViewById(R.id.voteAvg);
            TextView date = (TextView) rootView.findViewById(R.id.releaseDate);
            TextView overview = (TextView) rootView.findViewById(R.id.overview);
            ImageView imageView = (ImageView) rootView.findViewById(R.id.posterImageView);
            trailers = (LinearLayout) rootView.findViewById(R.id.trailers_llayout);
            reviews = (LinearLayout) rootView.findViewById(R.id.reviews_llayout);

            Intent i = getActivity().getIntent();

            if (i != null && i.hasExtra("moviePos")) {
                moviePosition = i.getIntExtra("moviePos", 0);
                //Log.i(LOG_TAG, "________moviePosition(intent): " + moviePosition);
                flag = true;
            }
            else if (getArguments() != null) {
                moviePosition = getArguments().getInt("moviePos");
                //Log.i(LOG_TAG, "________moviePosition(arguments): " + moviePosition);
                flag = true;
            }

            if (flag) {
                final Database db = new Database(getActivity());
                movie = MoviesFragment.movieResults.get(moviePosition);

                fetchVideos = new FetchVideosTask();
                fetchVideos.execute();
                fetchReviews = new FetchReviewsTask();
                fetchReviews.execute();

                if (db.isFavorited(movie)) {
                    checkBoxFav.setChecked(true);
                } else {
                    checkBoxFav.setChecked(false);
                }
                checkBoxFav.setVisibility(View.VISIBLE);

                checkBoxFav.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(getActivity(), "Clicked!", Toast.LENGTH_SHORT).show();
                        if (checkBoxFav.isChecked()) {
                            if (db.addFavorite(movie) > 0) {
                                Toast.makeText(getActivity(), "Favorited!", Toast.LENGTH_SHORT).show();
                            }
                        } else if (!checkBoxFav.isChecked()) {
                            if (db.removeMovie(movie) > 0) {
                                Toast.makeText(getActivity(), "Removed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        favoriteStateChanged = true;
                    }
                });

                title.setText(movie.getTitle());
                checkBoxFav.setVisibility(View.VISIBLE);
                vote.setText(movie.getVoteAvg());
                vote.setVisibility(View.VISIBLE);
                date.setText(movie.getReleaseDate());
                date.setVisibility(View.VISIBLE);
                overview.setText(movie.getOverview());
                overview.setVisibility(View.VISIBLE);

                Picasso.with(getContext()).setLoggingEnabled(true);
                Picasso.with(getContext())
                        .load(MoviesFragment.paths.get(moviePosition))
                        .into(imageView);
                imageView.setVisibility(View.VISIBLE);

                trailers.setPadding(0, 30, 0, 0);
                reviews.setPadding(0, 30, 0, 0);
            }
        }
        else{ //no internet
            Toast.makeText(getActivity(), "No Internet Connection..", Toast.LENGTH_SHORT).show();
            buttonRefresh.setVisibility(View.VISIBLE);
            buttonRefresh.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //restart activity
                    Intent intent = getActivity().getIntent();
                    getActivity().overridePendingTransition(0, 0);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    getActivity().finish();
                    getActivity().overridePendingTransition(0, 0);
                    startActivity(intent);
                }
            });
        }

        return rootView;
    }

    public class FetchReviewsTask extends AsyncTask<String, Void, ArrayList<String>> {

        public ArrayList<String> getReviewsDataFromJson(String R) throws JSONException {
            ArrayList<String> M_reviewsJsonStrs = new ArrayList<>();
            JSONObject j = new JSONObject(R);
            JSONArray Array = j.getJSONArray("results");

            for (int i = 0; i < Array.length(); i++) {
                JSONObject path = Array.getJSONObject(i);
                M_reviewsJsonStrs.add(path.getString("author") + " said:" + "\n" + path.getString("content") + "\n");
            }

            return M_reviewsJsonStrs;
        }

        @Override
        public ArrayList<String> doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String reviewsJsonStr = null;

            try {
                URL url = new URL("http://api.themoviedb.org/3/movie/" + movie.getId() + "/reviews" + "?api_key=a43f71fd4946d1e41d0d64badc99503d");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    reviewsJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    reviewsJsonStr = null;
                }
                reviewsJsonStr = buffer.toString();

                //Log.i(LOG_TAG, "_____________reviews json string: " + trailersJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                reviewsJsonStr = null;
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                //Log.i(LOG_TAG, reviewsJsonStr);
                return getReviewsDataFromJson(reviewsJsonStr);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(inflaterService);
            for (int i = 0; i < result.size(); i++) {
                if(!fetchReviews.isCancelled()) {
                    //Log.i(LOG_TAG, result.get(i));
                    View view = inflater.inflate(R.layout.list_reviews, null);
                    TextView txt_view = (TextView) view.findViewById(R.id.textview_reviews);
                    txt_view.setGravity(Gravity.CENTER_HORIZONTAL);
                    txt_view.setTextSize(20);
                    txt_view.setText(result.get(i));

                    reviews.addView(view);
                }
            }
        }
    }

    class FetchVideosTask extends AsyncTask<String, Void, ArrayList<String>> {
        
        public ArrayList<String> getVideoDataFromJson(String json) throws JarException, JSONException {
            
            ArrayList<String> video_keys = new ArrayList<>();
            JSONObject j = new JSONObject(json);
            JSONArray Array = j.getJSONArray("results");
            
            for (int i = 0; i < Array.length(); i++) {
                video_keys.add(Array.getJSONObject(i).getString("key"));
                //Log.i(LOG_TAG, video_keys.get(i));
            }
            
            return video_keys;
        }

        @Override
        public ArrayList<String> doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String trailersJsonStr = null;

            try{

                URL url = new URL("http://api.themoviedb.org/3/movie/" + movie.getId() + "/videos" + "?api_key=a43f71fd4946d1e41d0d64badc99503d");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    trailersJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    trailersJsonStr = null;
                }
                trailersJsonStr = buffer.toString();

                //Log.i(LOG_TAG, "_____________trailers json string: " + trailersJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                trailersJsonStr = null;
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                return getVideoDataFromJson(trailersJsonStr);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(final ArrayList<String> result) {

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(inflaterService);
            for (int i = 0; i < result.size(); i++) {
                if(!fetchVideos.isCancelled()) {
                    //Log.i(LOG_TAG, result.get(i));
                    View view = inflater.inflate(R.layout.list_videos, null);
                    trailer = (Button) view.findViewById(R.id.button_video);
                    trailer.setId(i);
                    trailer.setText("Trailer " + (i + 1));
                    trailer.setTextSize(20);
                    trailer.setTextColor(Color.BLACK);
                    /*try {
                        trailer.setImageBitmap(retrieveVideoFrameFromVideo("http://www.youtube.com/watch?v=" + result.get(trailer.getId())));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }*/

                    trailer.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + result.get(trailer.getId()))));
                        }
                    });
                    trailers.addView(view);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fetchVideos != null)
            fetchVideos.cancel(true);
        if (fetchReviews != null)
            fetchReviews.cancel(true);
    }

    public static Bitmap retrieveVideoFrameFromVideo(String videoPath)
            throws Throwable
    {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= 14)
                mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
            else
                mediaMetadataRetriever.setDataSource(videoPath);
            //   mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new Throwable(
                    "Exception in retriveVideoFrameFromVideo(String videoPath)"
                            + e.getMessage());
        }
        finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }
}
