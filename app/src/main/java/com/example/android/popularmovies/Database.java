package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by hasan on 9/5/2016.
 */
public class Database {

    private final String LOG_TAG = Database.class.getSimpleName();
    Helper helper;

    public Database(Context context){
        helper = new Helper(context);
    }

    static class Helper extends SQLiteOpenHelper {

        Context context;
        private static final String DB_NAME = "favMovies.db";
        private static final int DB_VERSION = 3;
        private static final String FAVORITES_TBL = "favoritesTbl";
        private static final String MOVIE_ID = "my_id";
        private static final String TITLE = "title";
        private static final String VOTE_AVG = "votAvg";
        private static final String RELEASE_DATE = "releaseDate";
        private static final String OVERVIEW = "overview";
        private static final String POSTER_PATH = "posterPath";

        public Helper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + FAVORITES_TBL + " (" + MOVIE_ID + " INTEGER PRIMARY KEY , "
                    + TITLE + " VARCHAR(255), " + VOTE_AVG + " VARCHAR(255) , " + RELEASE_DATE +
                    " VARCHAR(255), " + OVERVIEW + " VARCHAR(2550), " + POSTER_PATH + " VARCHAR(255));");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE favoritesTbl");
            onCreate(db);
        }
    }

    public long addFavorite(Movie movie){
        SQLiteDatabase database =  helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Helper.MOVIE_ID, movie.getId());
        contentValues.put(Helper.TITLE, movie.getTitle());
        contentValues.put(Helper.VOTE_AVG, movie.getVoteAvg());
        contentValues.put(Helper.RELEASE_DATE, movie.getReleaseDate());
        contentValues.put(Helper.OVERVIEW, movie.getOverview());
        contentValues.put(Helper.POSTER_PATH,movie.getPosterPath());

        //Log.i(LOG_TAG, "__________insert result: " + result);

        return database.insert(Helper.FAVORITES_TBL, null, contentValues);
    }

    public long removeMovie(Movie movie){
        SQLiteDatabase database =  helper.getWritableDatabase();
        //Log.i(LOG_TAG, "__________remove result: " + result);

        return database.delete(Helper.FAVORITES_TBL, Helper.MOVIE_ID +  "=" + movie.getId(), null);
    }

    public boolean isFavorited(Movie movie){
        ArrayList<Movie> favorited = getFavorites();
        for(int i = 0; i < favorited.size(); i++){
            if(favorited.get(i).getId().equals(movie.getId())){
                return true;
            }
        }

        return false;
    }

    public ArrayList<Movie> getFavorites(){
        SQLiteDatabase db = helper.getReadableDatabase();

        String[] cols = {
                Helper.MOVIE_ID,
                Helper.TITLE,
                Helper.VOTE_AVG,
                Helper.RELEASE_DATE,
                Helper.OVERVIEW,
                Helper.POSTER_PATH
        };

        Cursor cursor = db.query(Helper.FAVORITES_TBL, cols, null, null, null, null, null);
        ArrayList<Movie> favMovies = new ArrayList<>();

        while(cursor.moveToNext()){

            Movie movie = new Movie();
            movie.setId(cursor.getString(cursor.getColumnIndex(Helper.MOVIE_ID)));
            movie.setTitle(cursor.getString(cursor.getColumnIndex(Helper.TITLE)));
            movie.setOverview(cursor.getString(cursor.getColumnIndex(Helper.OVERVIEW)));
            movie.setVoteAvg(cursor.getString(cursor.getColumnIndex(Helper.VOTE_AVG)));
            movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(Helper.RELEASE_DATE)));
            movie.setPosterPath(cursor.getString(cursor.getColumnIndex(Helper.POSTER_PATH)));

            //Log.i(LOG_TAG, "Favorited: " + movie.getTitle());
            favMovies.add(movie);
        }
        cursor.close();
        db.close();

        return favMovies;
    }
}
