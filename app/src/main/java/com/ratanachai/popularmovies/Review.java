package com.ratanachai.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class Review implements Parcelable {
    private String id;
    private String author;
    private String content;
    private String url;

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
    public String getAuthor(){return author;}
    public String getContent(){return content;}

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
