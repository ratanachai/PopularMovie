package com.ratanachai.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;
import com.ratanachai.popularmovies.data.MovieContract.ReviewEntry;

public class Review implements Parcelable {
    private String id; // TMDB review ID
    private String author;
    private String content;
    private String url;

    /** PROJECTION for Content Provider */
    static final String[] REVIEW_COLUMNS = {
            ReviewEntry.TABLE_NAME + "." + ReviewEntry._ID,
            ReviewEntry.COLUMN_TMDB_REVIEW_ID,
            ReviewEntry.COLUMN_AUTHOR,
            ReviewEntry.COLUMN_CONTENT,
            ReviewEntry.COLUMN_URL
    };
    static final int COL_REVIEW_ROW_ID = 0;
    static final int COL_TMDB_REVIEW_ID= 1;
    static final int COL_AUTHOR = 2;
    static final int COL_CONTENT = 3;
    static final int COL_URL = 4;

    //Take Json string and create an instance
    public Review(String id, String author, String content, String url){
        this.id = id;
        this.author = author;
        this.content = content;
        this.url = url;
    }

    public String[] getAll(){
        String[] all = {id, author, content, url};
        return all;
    }
    public String getId(){return id;}
    public String getAuthor(){return author;}
    public String getContent(){return content;}
    public String getUrl(){return url;}

    /** Methods needed for implementing Parcelable
     * http://stackoverflow.com/questions/12503836/how-to-save-custom-arraylist-on-android-screen-rotate */

    private Review(Parcel in){
        id = in.readString();
        author = in.readString();
        content = in.readString();
        url = in.readString();
    }

    public int describeContents() {return 0; }
    public void writeToParcel(Parcel out, int flags){
        out.writeString(id);
        out.writeString(author);
        out.writeString(content);
        out.writeString(url);
    }
    public static final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>(){
        public Review createFromParcel(Parcel in){
            return new Review(in);
        }
        public Review[] newArray(int size){
            return new Review[size];
        }
    };

}
