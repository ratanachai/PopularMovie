package com.ratanachai.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;
import com.ratanachai.popularmovies.data.MovieContract.MovieEntry;

public class Movie implements Parcelable{
    private String id;
    private String title;
    private String posterPath;
    private String overview;
    private String userRating;
    private String releaseDate;

    // PROJECTION for Content Provider Query
    static final String[] MOVIE_COLUMNS = {
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_TMDB_MOVIE_ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_POSTER_PATH,
            MovieEntry.COLUMN_OVERVIEW,
            MovieEntry.COLUMN_USER_RATING,
            MovieEntry.COLUMN_RELEASE_DATE
    };
    static final int COL_MOVIE_ROW_ID = 0;
    static final int COL_TMDB_MOVIE_ID = 1;
    static final int COL_TITLE = 2;
    static final int COL_POSTER_PATH = 3;
    static final int COL_OVERVIEW = 4;
    static final int COL_USER_RATING = 5;
    static final int COL_RELEASE_DATE = 6;

    //Take Json string and create an instance
    public Movie(String id, String title, String poster, String overview, String userRating, String releaseDate){
        this.id = id;
        this.title = title;
        this.posterPath = poster;
        this.overview = overview;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
    }
    public String getPosterPath(){
        return posterPath;
    }
    public String getTitle(){
        return title;
    }
    public String[] getAll(){
        String[] all = {id, title, posterPath, overview, userRating, releaseDate};
        return all;
    }

    /** Methods needed for implementing Parcelable
     * http://stackoverflow.com/questions/12503836/how-to-save-custom-arraylist-on-android-screen-rotate */

    private Movie(Parcel in){
        id = in.readString();
        title = in.readString();
        posterPath = in.readString();
        overview = in.readString();
        userRating = in.readString();
        releaseDate = in.readString();
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel out, int flags){
        out.writeString(id);
        out.writeString(title);
        out.writeString(posterPath);
        out.writeString(overview);
        out.writeString(userRating);
        out.writeString(releaseDate);
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
