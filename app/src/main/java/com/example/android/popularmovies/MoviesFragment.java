package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

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

/**
 * Created by hasan on 8/17/2016.
 */
public class MoviesFragment extends Fragment {

    public static ArrayList<Movie> movieResults = new ArrayList<>();
    public static ArrayList<String> paths = new ArrayList<>();
    ImageAdapter adapter;
    GridView gridView;
    View myView;
    Button buttonRefresh;
    private final String LOG_TAG = MoviesFragment.class.getSimpleName();

    public static int moviePosition;
    public static String sortingType;
    public static boolean isRefreshed = false;

    public MoviesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        myView = rootView;

        buttonRefresh = (Button) rootView.findViewById(R.id.button_refresh);
        buttonRefresh.setVisibility(View.INVISIBLE);
        gridView = (GridView) myView.findViewById(R.id.movies_grid_view);

        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //restart activity
                isRefreshed = true;
                Intent intent = getActivity().getIntent();
                intent.putExtra("sortingType", sortingType);
                getActivity().overridePendingTransition(0, 0);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                getActivity().finish();
                getActivity().overridePendingTransition(0, 0);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu, menu);

        MenuItem item = menu.findItem(R.id.sorting_spinner);
        final Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        final ArrayAdapter<CharSequence> arrAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.spinner_list_item_array, android.R.layout.simple_spinner_item);
        arrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //Log.i(LOG_TAG, "________sortingType before: " + sortingType);
                if(isRefreshed) {
                    isRefreshed = false; //refresh button clicked once
                    for(int i = 0; i < spinner.getCount(); i++){
                        if(spinner.getItemAtPosition(i).toString().equals(sortingType)){
                            spinner.setSelection(i);
                            break;
                        }
                    }
                }
                else{
                    sortingType = parent.getItemAtPosition(position).toString();
                }
                //Log.i(LOG_TAG, "________sortingType after: " + sortingType);

                movieResults.clear();
                paths.clear();
                adapter = new ImageAdapter(getActivity());

                if(isOnline()) {
                    // On selecting a spinner item
                    if (sortingType.equals("Favorites")) {
                        buttonRefresh.setVisibility(View.INVISIBLE);
                        displayFavorites();
                    } else if (!sortingType.equals("Favorites")) {
                        buttonRefresh.setVisibility(View.INVISIBLE);
                        FetchMoviesTask task = new FetchMoviesTask();
                        task.execute(sortingType);
                    }
                }
                else {
                    gridView.setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(), "No Internet Connection..", Toast.LENGTH_SHORT).show();
                    buttonRefresh.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do nothing
            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }


    public void displayFavorites(){
        movieResults.clear();
        paths.clear();

        Database db = new Database(getActivity());
        movieResults = db.getFavorites();
        for(int i = 0; i < movieResults.size(); i++) {
            paths.add("http://image.tmdb.org/t/p/w185/" + movieResults.get(i).getPosterPath());
            //Log.v(LOG_TAG, "----------MOVIE POSTER: " + paths.get(i));
        }

        adapter.setPaths(paths);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                /*Intent i = new Intent(getContext(), DetailsActivity.class);
                i.putExtra("moviePos", position);
                moviePosition = position;
                startActivity(i);*/

                moviePosition = position;
                //Log.i(LOG_TAG, "________moviePosition pre-sending: " + moviePosition);
                ((Callback)getActivity()).onItemSelected(position);
            }
        });
    }

    private ArrayList<Movie> getMoviesDataFromJson(String moviesJsonStr)
            throws JSONException {

        final String ORIGINAL_TITLE = "original_title";
        final String POSTER_PATH = "poster_path";
        final String OVERVIEW = "overview";
        final String VOTE_AVERAGE = "vote_average";
        final String RELEASE_DATE = "release_date";
        final String ID = "id";

        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        JSONArray moviesArray = moviesJson.getJSONArray("results");

        for(int i = 0; i < moviesArray.length(); i++) {

            JSONObject movieJson = moviesArray.getJSONObject(i);
            Movie movie = new Movie();

            movie.setTitle(movieJson.getString(ORIGINAL_TITLE));
            movie.setPosterPath(movieJson.getString(POSTER_PATH));
            movie.setOverview(movieJson.getString(OVERVIEW));
            movie.setReleaseDate(movieJson.getString(RELEASE_DATE));
            movie.setVoteAvg(movieJson.getString(VOTE_AVERAGE));
            movie.setId(movieJson.getString(ID));

            movieResults.add(movie);

            //Log.v(LOG_TAG, "----------MOVIE ENTRY: " + movie.toString());
        }

        return movieResults;
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String moviesJsonStr = null;
            movieResults.clear();
            paths.clear();

            try {
                URL url;
                if(sortingType.equals("Most Popular")) {
                    url = new URL("http://api.themoviedb.org/3/movie/popular?api_key=a43f71fd4946d1e41d0d64badc99503d");
                    //Log.i(LOG_TAG, "------------Most Popular");
                }
                else { //Top Rated
                    url = new URL("http://api.themoviedb.org/3/movie/top_rated?api_key=a43f71fd4946d1e41d0d64badc99503d");
                    //Log.i(LOG_TAG, "------------Top Rated");
                }

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                moviesJsonStr = buffer.toString();
                //Log.i(LOG_TAG, "------------MOVIES JSON STR: " + moviesJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);

                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMoviesDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> result) {

            for(int i = 0; i < result.size(); i++) {
                paths.add("http://image.tmdb.org/t/p/w185/" + result.get(i).getPosterPath());
                //Log.v(LOG_TAG, "----------MOVIE POSTER: " + paths.get(i));
            }

            adapter.setPaths(paths);
            gridView.setAdapter(adapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                /*Intent i = new Intent(getContext(), DetailsActivity.class);
                i.putExtra("moviePos", position);
                moviePosition = position;
                startActivity(i);*/

                moviePosition = position;
                //Log.i(LOG_TAG, "________moviePosition pre-sending: " + moviePosition);
                ((Callback)getActivity()).onItemSelected(position);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.i(LOG_TAG, "_______favoriteStateChanged: " + DetailsFragment.favoriteStateChanged);
        if(DetailsFragment.favoriteStateChanged && sortingType.equals("Favorites")){
            DetailsFragment.favoriteStateChanged = false; //pend new updates
            buttonRefresh.post(new Runnable(){
                @Override
                public void run() {
                    buttonRefresh.performClick();
                }
            });
        }
    }

    public interface Callback {
        public void onItemSelected(int moviePos);
    }
}
