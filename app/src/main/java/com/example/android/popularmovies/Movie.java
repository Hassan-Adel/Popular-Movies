package com.example.android.popularmovies;

/**
 * Created by hasan on 8/20/2016.
 */
public class Movie {

    private String title;
    private String posterPath;
    private String overview;
    private String voteAvg;
    private String releaseDate;
    private String id;

    public Movie() {
    }

    public Movie(String title, String posterPath, String overview, String voteAvg, String releaseDate, String id) {
        this.title = title;
        this.posterPath = posterPath;
        this.overview = overview;
        this.voteAvg = voteAvg;
        this.releaseDate = releaseDate;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getVoteAvg() {
        return voteAvg;
    }

    public void setVoteAvg(String voteAvg) {
        this.voteAvg = voteAvg;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String toString(){
        return title + " " + posterPath + " " + overview + " " + releaseDate + " " + voteAvg;
    }
}
