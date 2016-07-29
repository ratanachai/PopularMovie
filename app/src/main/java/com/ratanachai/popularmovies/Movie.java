package com.ratanachai.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;
import com.ratanachai.popularmovies.data.MovieContract.MovieEntry;

public class Movie implements Parcelable{
    private String id;
    private String title;
    private String posterPath;
    private String overview;
    private String releaseDate;
    private String voteAverage;
    private String voteCount;

    // PROJECTION for Content Provider Query
    static final String[] MOVIE_COLUMNS = {
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_TMDB_MOVIE_ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_POSTER_PATH,
            MovieEntry.COLUMN_OVERVIEW,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieEntry.COLUMN_VOTE_COUNT
    };
    static final int COL_MOVIE_ROW_ID = 0;
    static final int COL_TMDB_MOVIE_ID = 1;
    static final int COL_TITLE = 2;
    static final int COL_POSTER_PATH = 3;
    static final int COL_OVERVIEW = 4;
    static final int COL_RELEASE_DATE = 5;
    static final int COL_VOTE_AVERAGE = 6;
    static final int COL_VOTE_COUNT = 7;

    //Take Json string and create an instance
    public Movie (String id, String title, String poster, String overview, String releaseDate,
                  String voteAverage, String voteCount) {
        this.id = id;
        this.title = title;
        this.posterPath = poster;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
    }
    public String getPosterPath(){
        return posterPath;
    }
    public String getTitle(){
        return title;
    }
    public String[] getAll(){
        String[] all = {id, title, posterPath, overview, releaseDate, voteAverage, voteCount};
        return all;
    }

    /** Methods needed for implementing Parcelable
     * http://stackoverflow.com/questions/12503836/how-to-save-custom-arraylist-on-android-screen-rotate */

    private Movie(Parcel in){
        id = in.readString();
        title = in.readString();
        posterPath = in.readString();
        overview = in.readString();
        releaseDate = in.readString();
        voteAverage = in.readString();
        voteCount = in.readString();
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel out, int flags){
        out.writeString(id);
        out.writeString(title);
        out.writeString(posterPath);
        out.writeString(overview);
        out.writeString(releaseDate);
        out.writeString(voteAverage);
        out.writeString(voteCount);
    }
    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>(){
        public Movie createFromParcel(Parcel in){
            return new Movie(in);
        }
        public Movie[] newArray(int size){
            return new Movie[size];
        }
    };

}
