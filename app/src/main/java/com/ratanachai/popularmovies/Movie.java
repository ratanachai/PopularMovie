package com.ratanachai.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable{
    private String id;
    private String title;
    private String posterUrl;
    private String overview;
    private String userRating;
    private String releaseDate;

    //Take Json string and create an instance
    public Movie(String id, String title, String poster, String overview, String userRating, String releaseDate){
        this.id = id;
        this.title = title;
        this.posterUrl = poster;
        this.overview = overview;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
    }
    public String getPosterUrl(){
        return posterUrl;
    }
    public String[] getAll(){
        String[] all = {id, title, posterUrl, overview, userRating, releaseDate};
        return all;
    }

    /** Methods needed for implementing Parcelable
     * http://stackoverflow.com/questions/12503836/how-to-save-custom-arraylist-on-android-screen-rotate */

    private Movie(Parcel in){
        id = in.readString();
        title = in.readString();
        posterUrl = in.readString();
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
        out.writeString(posterUrl);
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
